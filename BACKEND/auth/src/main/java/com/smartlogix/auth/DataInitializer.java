package com.smartlogix.auth;

import com.smartlogix.auth.entities.Usuario;
import com.smartlogix.auth.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .nombre("Admin")
                    .email("admin@smartlogix.cl")
                    .contrasena(passwordEncoder.encode("admin123"))
                    .rol("ADMIN")
                    .build();
            usuarioRepository.save(admin);
            System.out.println("Usuario admin creado: admin@smartlogix.cl / admin123");
        }
    }
}
