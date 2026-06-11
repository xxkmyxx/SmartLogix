package com.smartlogix.envios.services;

import com.smartlogix.envios.dto.EnvioRequest;
import com.smartlogix.envios.dto.EnvioResponse;
import com.smartlogix.envios.entities.Envio;
import com.smartlogix.envios.entities.EstadoEnvio;
import com.smartlogix.envios.entities.Transportista;
import com.smartlogix.envios.repositories.EnvioRepository;
import com.smartlogix.envios.repositories.TransportistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceImplTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private TransportistaRepository transportistaRepository;

    @InjectMocks
    private EnvioServiceImpl envioService;

    private Transportista transportista;
    private Envio envio;

    @BeforeEach
    void setUp() {
        transportista = Transportista.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .telefono("912345678")
                .patente("ABC123")
                .build();

        envio = Envio.builder()
                .id(1L)
                .pedidoId(10L)
                .transportista(transportista)
                .estado(EstadoEnvio.PENDIENTE)
                .fechaEstimada(LocalDate.now().plusDays(2))
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void crear_debeRetornarEnvioCreado() {
        EnvioRequest request = new EnvioRequest();
        request.setPedidoId(10L);
        request.setTransportistaId(1L);
        request.setFechaEstimada(LocalDate.now().plusDays(2));

        when(transportistaRepository.findById(1L)).thenReturn(Optional.of(transportista));
        when(envioRepository.save(any(Envio.class))).thenReturn(envio);

        EnvioResponse response = envioService.crear(request);

        assertNotNull(response);
        assertEquals(10L, response.getPedidoId());
        assertEquals(EstadoEnvio.PENDIENTE, response.getEstado());
        assertEquals("Juan Pérez", response.getNombreTransportista());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    void crear_transportistaNoExiste_lanzaExcepcion() {
        EnvioRequest request = new EnvioRequest();
        request.setPedidoId(10L);
        request.setTransportistaId(99L);

        when(transportistaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> envioService.crear(request));
        verify(envioRepository, never()).save(any());
    }

    @Test
    void buscarPorId_debeRetornarEnvio() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        EnvioResponse response = envioService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(EstadoEnvio.PENDIENTE, response.getEstado());
    }

    @Test
    void buscarPorId_noExiste_lanzaExcepcion() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> envioService.buscarPorId(99L));
    }

    @Test
    void buscarPorPedidoId_debeRetornarEnvio() {
        when(envioRepository.findByPedidoId(10L)).thenReturn(Optional.of(envio));

        EnvioResponse response = envioService.buscarPorPedidoId(10L);

        assertNotNull(response);
        assertEquals(10L, response.getPedidoId());
    }

    @Test
    void buscarPorPedidoId_noExiste_lanzaExcepcion() {
        when(envioRepository.findByPedidoId(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> envioService.buscarPorPedidoId(99L));
    }

    @Test
    void listarTodos_debeRetornarLista() {
        when(envioRepository.findAll()).thenReturn(List.of(envio));

        List<EnvioResponse> resultado = envioService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals(EstadoEnvio.PENDIENTE, resultado.get(0).getEstado());
    }

    @Test
    void listarPorEstado_debeRetornarLista() {
        when(envioRepository.findByEstado(EstadoEnvio.PENDIENTE)).thenReturn(List.of(envio));

        List<EnvioResponse> resultado = envioService.listarPorEstado(EstadoEnvio.PENDIENTE);

        assertEquals(1, resultado.size());
    }

    @Test
    void listarPorTransportista_debeRetornarLista() {
        when(envioRepository.findByTransportistaId(1L)).thenReturn(List.of(envio));

        List<EnvioResponse> resultado = envioService.listarPorTransportista(1L);

        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombreTransportista());
    }

    @Test
    void actualizarEstado_debeActualizarCorrectamente() {
        Envio envioActualizado = Envio.builder()
                .id(1L)
                .pedidoId(10L)
                .transportista(transportista)
                .estado(EstadoEnvio.RECOGIDO)
                .fechaEstimada(LocalDate.now().plusDays(2))
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenReturn(envioActualizado);

        EnvioResponse response = envioService.actualizarEstado(1L, EstadoEnvio.RECOGIDO);

        assertEquals(EstadoEnvio.RECOGIDO, response.getEstado());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    void actualizarEstado_envioNoExiste_lanzaExcepcion() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> envioService.actualizarEstado(99L, EstadoEnvio.RECOGIDO));
    }
}
