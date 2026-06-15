package com.smartlogix.bff.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtUtils jwtUtils;

    // Token generado con el mismo secret para pruebas (HMAC256, sub=camila@smartlogix.cl, role=ADMIN)
    private static final String SECRET = "X2@pQ!8zH#1kM$eY6%WnR3vG7*CdT4bU^aL0sJ9fB5h";

    // Token válido generado con el secret de prueba
    private String validToken;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getSecret()).thenReturn(SECRET);

        // Generar token usando com.auth0:java-jwt directamente
        validToken = com.auth0.jwt.JWT.create()
                .withSubject("camila@smartlogix.cl")
                .withClaim("role", "ADMIN")
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256(SECRET));
    }

    @Test
    void validateToken_tokenValido_retornaTrue() {
        assertTrue(jwtUtils.validateToken(validToken));
    }

    @Test
    void validateToken_tokenInvalido_retornaFalse() {
        assertFalse(jwtUtils.validateToken("token.invalido.xyz"));
    }

    @Test
    void validateToken_tokenVacio_retornaFalse() {
        assertFalse(jwtUtils.validateToken(""));
    }

    @Test
    void getEmailFromToken_retornaSubject() {
        assertEquals("camila@smartlogix.cl", jwtUtils.getEmailFromToken(validToken));
    }

    @Test
    void getRoleFromToken_retornaRol() {
        assertEquals("ADMIN", jwtUtils.getRoleFromToken(validToken));
    }
}
