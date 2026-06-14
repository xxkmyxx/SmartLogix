package com.smartlogix.auth.services;

import com.smartlogix.auth.dto.LoginRequest;
import com.smartlogix.auth.dto.LoginResponse;
import com.smartlogix.auth.dto.RegisterRequest;
import com.smartlogix.auth.entities.Usuario;
import com.smartlogix.auth.repositories.UsuarioRepository;
import com.smartlogix.auth.webconfig.JwtProperties;
import com.smartlogix.auth.webconfig.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!usuario.getActivo()) {
            throw new RuntimeException("Cuenta desactivada. Contacte al administrador.");
        }

        // Verificar bloqueo por intentos fallidos (SRS HU-01, CA-03)
        if (usuario.getFechaBloqueo() != null) {
            LocalDateTime desbloqueo = usuario.getFechaBloqueo().plusMinutes(MINUTOS_BLOQUEO);
            if (LocalDateTime.now().isBefore(desbloqueo)) {
                long minutosRestantes = java.time.Duration.between(LocalDateTime.now(), desbloqueo).toMinutes() + 1;
                throw new RuntimeException("Cuenta bloqueada. Intente en " + minutosRestantes + " minuto(s).");
            } else {
                // Desbloquear automáticamente tras 15 minutos
                usuario.setFechaBloqueo(null);
                usuario.setIntentosFallidos(0);
            }
        }

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);
            if (intentos >= MAX_INTENTOS) {
                usuario.setFechaBloqueo(LocalDateTime.now());
            }
            usuarioRepository.save(usuario);
            throw new RuntimeException("Credenciales inválidas");
        }

        // Login exitoso — resetear contador
        usuario.setIntentosFallidos(0);
        usuario.setFechaBloqueo(null);
        usuarioRepository.save(usuario);

        String token = jwtUtils.generateToken(usuario.getEmail(), usuario.getRol(), usuario.getId());

        return LoginResponse.builder()
                .token(token)
                .tipo(jwtProperties.getPrefix())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .expiracion(jwtProperties.getExpiration())
                .build();
    }

    @Override
    @Transactional
    public Usuario register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado.");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .rol(request.getRol())
                .build();

        return usuarioRepository.save(usuario);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }
}
