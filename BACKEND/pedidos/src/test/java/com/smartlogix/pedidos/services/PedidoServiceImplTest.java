package com.smartlogix.pedidos.services;

import com.smartlogix.pedidos.dto.PedidoResponse;
import com.smartlogix.pedidos.entities.DetallePedido;
import com.smartlogix.pedidos.entities.EstadoPedido;
import com.smartlogix.pedidos.entities.Pedido;
import com.smartlogix.pedidos.repositories.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private InventarioClientService inventarioClient;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Pedido pedidoPendiente;

    @BeforeEach
    void setUp() {
        pedidoPendiente = Pedido.builder()
                .id(1L)
                .numeroPedido("PED-20250524120000")
                .clienteEmail("cliente@test.cl")
                .clienteNombre("Cliente Test")
                .estado(EstadoPedido.PENDIENTE)
                .total(50000.0)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .detalles(new ArrayList<>())
                .build();
    }

    @Test
    void buscarPorId_pedidoExistente_retornaResponse() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPendiente));

        PedidoResponse response = pedidoService.buscarPorId(1L);

        assertNotNull(response);
        assertEquals("PED-20250524120000", response.getNumeroPedido());
        assertEquals(EstadoPedido.PENDIENTE, response.getEstado());
    }

    @Test
    void buscarPorId_pedidoNoExistente_lanzaExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.buscarPorId(99L));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    @Test
    void buscarPorNumeroPedido_existente_retornaResponse() {
        when(pedidoRepository.findByNumeroPedido("PED-20250524120000"))
                .thenReturn(Optional.of(pedidoPendiente));

        PedidoResponse response = pedidoService.buscarPorNumeroPedido("PED-20250524120000");

        assertNotNull(response);
        assertEquals("cliente@test.cl", response.getClienteEmail());
    }

    @Test
    void listarTodos_retornaListaDeResponses() {
        when(pedidoRepository.findAll()).thenReturn(List.of(pedidoPendiente));

        List<PedidoResponse> lista = pedidoService.listarTodos();

        assertNotNull(lista);
        assertEquals(1, lista.size());
    }

    @Test
    void listarPorCliente_retornaListaFiltrada() {
        when(pedidoRepository.findByClienteEmail("cliente@test.cl"))
                .thenReturn(List.of(pedidoPendiente));

        List<PedidoResponse> lista = pedidoService.listarPorCliente("cliente@test.cl");

        assertEquals(1, lista.size());
        assertEquals("Cliente Test", lista.get(0).getClienteNombre());
    }

    @Test
    void cambiarEstado_dePendienteAEnProceso_exitoso() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPendiente));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.cambiarEstado(1L, EstadoPedido.CONFIRMADO);

        assertEquals(EstadoPedido.CONFIRMADO, response.getEstado());
        verify(pedidoRepository).save(pedidoPendiente);
    }

    @Test
    void cambiarEstado_pedidoEntregado_lanzaExcepcion() {
        pedidoPendiente.setEstado(EstadoPedido.ENTREGADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPendiente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.cambiarEstado(1L, EstadoPedido.CONFIRMADO));
        assertTrue(ex.getMessage().contains("ENTREGADO"));
    }

    @Test
    void cambiarEstado_pedidoCancelado_lanzaExcepcion() {
        pedidoPendiente.setEstado(EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPendiente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.cambiarEstado(1L, EstadoPedido.CONFIRMADO));
        assertTrue(ex.getMessage().contains("CANCELADO"));
    }

    @Test
    void cancelar_pedidoPendiente_cambiaEstadoACancelado() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPendiente));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        pedidoService.cancelar(1L);

        assertEquals(EstadoPedido.CANCELADO, pedidoPendiente.getEstado());
        verify(pedidoRepository).save(pedidoPendiente);
    }

    @Test
    void cancelar_pedidoEntregado_lanzaExcepcion() {
        pedidoPendiente.setEstado(EstadoPedido.ENTREGADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoPendiente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.cancelar(1L));
        assertTrue(ex.getMessage().contains("entregado"));
    }

    @Test
    void listarPorEstado_retornaListaFiltradaPorEstado() {
        when(pedidoRepository.findByEstado(EstadoPedido.PENDIENTE))
                .thenReturn(List.of(pedidoPendiente));

        List<PedidoResponse> lista = pedidoService.listarPorEstado(EstadoPedido.PENDIENTE);

        assertEquals(1, lista.size());
        assertEquals(EstadoPedido.PENDIENTE, lista.get(0).getEstado());
    }
}
