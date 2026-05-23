package com.smartlogix.auth.controllers;

import com.smartlogix.auth.dto.LoginRequest;
import com.smartlogix.auth.dto.LoginResponse;
import com.smartlogix.auth.dto.RegisterRequest;
import com.smartlogix.auth.entities.Usuario;
import com.smartlogix.auth.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login, registro y validación de tokens JWT")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar nuevo usuario (solo ADMIN)")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Usuario usuario = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "id", usuario.getId(),
                        "email", usuario.getEmail(),
                        "nombre", usuario.getNombre(),
                        "rol", usuario.getRol()
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar si un token JWT es válido")
    public ResponseEntity<Map<String, Boolean>> validate(
            @RequestHeader("Authorization") String authHeader) {
        boolean valid = false;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            valid = authService.validateToken(token);
        }
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
