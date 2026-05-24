package ${package}.${artifactId}.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EjemploServiceTest {

    private EjemploService service;

    @BeforeEach
    void setUp() {
        service = new EjemploServiceImpl();
    }

    @Test
    void obtenerEstado_debeRetornarMensaje() {
        String resultado = service.obtenerEstado();
        assertNotNull(resultado);
        assertFalse(resultado.isBlank());
    }
}
