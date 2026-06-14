package com.smartlogix.auth.services;

import com.smartlogix.auth.dto.LoginRequest;
import com.smartlogix.auth.dto.LoginResponse;
import com.smartlogix.auth.dto.RegisterRequest;
import com.smartlogix.auth.entities.Usuario;
import com.smartlogix.auth.repositories.UsuarioRepository;
import com.smartlogix.auth.webconfig.JwtProperties;
import com.smartlogix.auth.webconfig.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioActivo;

    @BeforeEach
    void setUp() {
        usuarioActivo = Usuario.builder()
                .id(1L)
                .nombre("Camila Pino")
                .email("camila@smartlogix.cl")
                .contrasena("$2a$encoded")
                .rol("ADMIN")
                .activo(true)
                .intentosFallidos(0)
                .build();
    }

    private LoginRequest loginRequest(String email, String contrasena) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setContrasena(contrasena);
        return r;
    }

    private RegisterRequest registerRequest(String nombre, String email, String contrasena, String rol) {
        RegisterRequest r = new RegisterRequest();
        r.setNombre(nombre);
        r.setEmail(email);
        r.setContrasena(contrasena);
        r.setRol(rol);
        return r;
    }

    @Test
    void login_credencialesValidas_retornaToken() {
        LoginRequest request = loginRequest("camila@smartlogix.cl", "password123");

        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches(request.getContrasena(), usuarioActivo.getContrasena())).thenReturn(true);
        when(jwtUtils.generateToken(anyString(), anyString(), anyLong())).thenReturn("jwt-token-generado");
        when(jwtProperties.getPrefix()).thenReturn("Bearer");
        when(jwtProperties.getExpiration()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-generado", response.getToken());
        assertEquals("camila@smartlogix.cl", response.getEmail());
        assertEquals("ADMIN", response.getRol());
        verify(usuarioRepository, times(1)).save(usuarioActivo);
    }

    @Test
    void login_usuarioNoExiste_lanzaExcepcion() {
        LoginRequest request = loginRequest("noexiste@test.cl", "pass");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertEquals("Credenciales inválidas", ex.getMessage());
    }

    @Test
    void login_cuentaDesactivada_lanzaExcepcion() {
        usuarioActivo.setActivo(false);
        LoginRequest request = loginRequest("camila@smartlogix.cl", "password123");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuarioActivo));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("desactivada"));
    }

    @Test
    void login_contrasenaIncorrecta_incrementaIntentosFallidos() {
        LoginRequest request = loginRequest("camila@smartlogix.cl", "malaclave");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches(request.getContrasena(), usuarioActivo.getContrasena())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals(1, usuarioActivo.getIntentosFallidos());
        verify(usuarioRepository).save(usuarioActivo);
    }

    @Test
    void login_cincointentosFallidos_bloqueaCuenta() {
        usuarioActivo.setIntentosFallidos(4);
        LoginRequest request = loginRequest("camila@smartlogix.cl", "malaclave");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));

        assertNotNull(usuarioActivo.getFechaBloqueo());
        assertEquals(5, usuarioActivo.getIntentosFallidos());
    }

    @Test
    void login_cuentaBloqueadaReciente_lanzaExcepcion() {
        usuarioActivo.setFechaBloqueo(LocalDateTime.now().minusMinutes(5));
        LoginRequest request = loginRequest("camila@smartlogix.cl", "cualquier");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuarioActivo));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("bloqueada"));
    }

    @Test
    void login_bloqueoCaducado_desbloqueaYPermiteLogin() {
        usuarioActivo.setFechaBloqueo(LocalDateTime.now().minusMinutes(20));
        usuarioActivo.setIntentosFallidos(5);
        LoginRequest request = loginRequest("camila@smartlogix.cl", "password123");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(usuarioActivo));
        when(passwordEncoder.matches(request.getContrasena(), usuarioActivo.getContrasena())).thenReturn(true);
        when(jwtUtils.generateToken(anyString(), anyString(), anyLong())).thenReturn("token-ok");
        when(jwtProperties.getPrefix()).thenReturn("Bearer");
        when(jwtProperties.getExpiration()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNull(usuarioActivo.getFechaBloqueo());
        assertEquals(0, usuarioActivo.getIntentosFallidos());
    }

    @Test
    void register_emailNuevo_creaUsuario() {
        RegisterRequest request = registerRequest("Gonzalo", "gonzalo@smartlogix.cl", "pass", "OPERADOR");
        when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getContrasena())).thenReturn("$2a$encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = authService.register(request);

        assertNotNull(resultado);
        assertEquals("Gonzalo", resultado.getNombre());
        assertEquals("OPERADOR", resultado.getRol());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void register_emailDuplicado_lanzaExcepcion() {
        RegisterRequest request = registerRequest("Otro", "camila@smartlogix.cl", "pass", "ADMIN");
        when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertTrue(ex.getMessage().contains("ya está registrado"));
    }
}
