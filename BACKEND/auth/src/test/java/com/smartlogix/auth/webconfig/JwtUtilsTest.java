package com.smartlogix.auth.webconfig;

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

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getSecret()).thenReturn("X2@pQ!8zH#1kM$eY6%WnR3vG7*CdT4bU^aL0sJ9fB5h");
        lenient().when(jwtProperties.getExpiration()).thenReturn(86400000L);
    }

    @Test
    void generateToken_retornaTokenNoNulo() {
        String token = jwtUtils.generateToken("camila@smartlogix.cl", "ADMIN", 1L);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_tokenValido_retornaTrue() {
        String token = jwtUtils.generateToken("camila@smartlogix.cl", "ADMIN", 1L);
        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    void validateToken_tokenInvalido_retornaFalse() {
        assertFalse(jwtUtils.validateToken("token.invalido.xyz"));
    }

    @Test
    void getEmailFromToken_retornaEmailCorrecto() {
        String token = jwtUtils.generateToken("camila@smartlogix.cl", "ADMIN", 1L);
        assertEquals("camila@smartlogix.cl", jwtUtils.getEmailFromToken(token));
    }

    @Test
    void getRoleFromToken_retornaRolCorrecto() {
        String token = jwtUtils.generateToken("camila@smartlogix.cl", "ADMIN", 1L);
        assertEquals("ADMIN", jwtUtils.getRoleFromToken(token));
    }
}
