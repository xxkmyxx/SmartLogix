package com.smartlogix.envios.services;

import com.smartlogix.envios.dto.TransportistaRequest;
import com.smartlogix.envios.entities.Transportista;
import com.smartlogix.envios.repositories.TransportistaRepository;
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
class TransportistaServiceImplTest {

    @Mock
    private TransportistaRepository transportistaRepository;

    @InjectMocks
    private TransportistaServiceImpl transportistaService;

    private Transportista transportista;

    @BeforeEach
    void setUp() {
        transportista = Transportista.builder()
                .id(1L)
                .nombre("María González")
                .telefono("987654321")
                .patente("XY9876")
                .build();
    }

    @Test
    void crear_debeRetornarTransportistaCreado() {
        TransportistaRequest request = new TransportistaRequest();
        request.setNombre("María González");
        request.setTelefono("987654321");
        request.setPatente("XY9876");

        when(transportistaRepository.save(any(Transportista.class))).thenReturn(transportista);

        Transportista resultado = transportistaService.crear(request);

        assertNotNull(resultado);
        assertEquals("María González", resultado.getNombre());
        assertEquals("XY9876", resultado.getPatente());
        verify(transportistaRepository, times(1)).save(any(Transportista.class));
    }

    @Test
    void listarTodos_debeRetornarLista() {
        when(transportistaRepository.findAll()).thenReturn(List.of(transportista));

        List<Transportista> resultado = transportistaService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("María González", resultado.get(0).getNombre());
    }

    @Test
    void buscarPorId_debeRetornarTransportista() {
        when(transportistaRepository.findById(1L)).thenReturn(Optional.of(transportista));

        Transportista resultado = transportistaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscarPorId_noExiste_lanzaExcepcion() {
        when(transportistaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> transportistaService.buscarPorId(99L));
    }

    @Test
    void eliminar_debeInvocarDelete() {
        doNothing().when(transportistaRepository).deleteById(1L);

        transportistaService.eliminar(1L);

        verify(transportistaRepository, times(1)).deleteById(1L);
    }
}
