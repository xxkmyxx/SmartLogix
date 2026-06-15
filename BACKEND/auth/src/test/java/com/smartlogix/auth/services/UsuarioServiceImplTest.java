package com.smartlogix.auth.services;

import com.smartlogix.auth.entities.Usuario;
import com.smartlogix.auth.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Camila Pino")
                .email("camila@smartlogix.cl")
                .contrasena("$2a$encoded")
                .rol("ADMIN")
                .activo(true)
                .build();
    }

    @Test
    void listarTodos_retornaLista() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        List<Usuario> resultado = usuarioService.listarTodos();
        assertEquals(1, resultado.size());
        assertEquals("Camila Pino", resultado.get(0).getNombre());
    }

    @Test
    void listarActivos_retornaSoloActivos() {
        when(usuarioRepository.findByActivoTrue()).thenReturn(List.of(usuario));
        List<Usuario> resultado = usuarioService.listarActivos();
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getActivo());
    }

    @Test
    void buscarPorId_existente_retornaUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        Optional<Usuario> resultado = usuarioService.buscarPorId(1L);
        assertTrue(resultado.isPresent());
        assertEquals("camila@smartlogix.cl", resultado.get().getEmail());
    }

    @Test
    void buscarPorId_noExistente_retornaVacio() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Usuario> resultado = usuarioService.buscarPorId(99L);
        assertFalse(resultado.isPresent());
    }

    @Test
    void buscarPorEmail_existente_retornaUsuario() {
        when(usuarioRepository.findByEmail("camila@smartlogix.cl")).thenReturn(Optional.of(usuario));
        Optional<Usuario> resultado = usuarioService.buscarPorEmail("camila@smartlogix.cl");
        assertTrue(resultado.isPresent());
        assertEquals("ADMIN", resultado.get().getRol());
    }

    @Test
    void actualizar_usuarioExistente_actualizaDatos() {
        Usuario datosNuevos = Usuario.builder()
                .nombre("Camila Actualizada")
                .rol("OPERADOR")
                .activo(false)
                .build();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.actualizar(1L, datosNuevos);

        assertEquals("Camila Actualizada", resultado.getNombre());
        assertEquals("OPERADOR", resultado.getRol());
        assertFalse(resultado.getActivo());
    }

    @Test
    void actualizar_usuarioNoExistente_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.actualizar(99L, new Usuario()));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    @Test
    void eliminar_usuarioExistente_eliminaCorrectamente() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        assertDoesNotThrow(() -> usuarioService.eliminar(1L));
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    void eliminar_usuarioNoExistente_lanzaExcepcion() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> usuarioService.eliminar(99L));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }
}
