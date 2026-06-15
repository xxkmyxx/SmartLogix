package com.smartlogix.pedidos.services;

import com.smartlogix.pedidos.dto.DetallePedidoRequest;
import com.smartlogix.pedidos.dto.PedidoRequest;
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
class PedidoServiceImplExtendedTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private InventarioClientService inventarioClient;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = Pedido.builder()
                .id(1L)
                .numeroPedido("PED-20260614160000")
                .clienteEmail("cliente@test.cl")
                .clienteNombre("Cliente Test")
                .estado(EstadoPedido.PENDIENTE)
                .total(10000.0)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .detalles(new ArrayList<>())
                .build();
    }

    @Test
    void buscarPorNumeroPedido_noExistente_lanzaExcepcion() {
        when(pedidoRepository.findByNumeroPedido("PED-999")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.buscarPorNumeroPedido("PED-999"));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    @Test
    void cancelar_pedidoCancelado_lanzaExcepcion() {
        pedido.setEstado(EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.cancelar(1L));
        assertTrue(ex.getMessage().contains("cancelado"));
    }

    @Test
    void cancelar_conDetalles_reponeSotck() {
        DetallePedido detalle = DetallePedido.builder()
                .id(1L)
                .pedido(pedido)
                .productoId(10L)
                .productoCodigo("PROD-001")
                .productoNombre("Producto A")
                .cantidad(5)
                .precioUnitario(2000.0)
                .subtotal(10000.0)
                .build();
        pedido.setDetalles(List.of(detalle));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioClient.ajustarStock(anyLong(), anyInt())).thenReturn(true);

        pedidoService.cancelar(1L);

        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        verify(inventarioClient).ajustarStock(10L, 5);
    }

    @Test
    void listarPorEstado_sinResultados_retornaListaVacia() {
        when(pedidoRepository.findByEstado(EstadoPedido.ENTREGADO)).thenReturn(List.of());
        List<PedidoResponse> resultado = pedidoService.listarPorEstado(EstadoPedido.ENTREGADO);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void crear_stockInsuficiente_lanzaExcepcion() {
        DetallePedidoRequest detalle = new DetallePedidoRequest();
        detalle.setProductoId(1L);
        detalle.setProductoCodigo("PROD-001");
        detalle.setProductoNombre("Producto A");
        detalle.setCantidad(100);
        detalle.setPrecioUnitario(500.0);

        PedidoRequest request = new PedidoRequest();
        request.setClienteEmail("cliente@test.cl");
        request.setClienteNombre("Cliente Test");
        request.setDetalles(List.of(detalle));

        when(inventarioClient.obtenerStockProducto(1L)).thenReturn(5);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.crear(request));
        assertTrue(ex.getMessage().contains("Stock insuficiente") || ex.getMessage().contains("stock"));
    }

    @Test
    void crear_servicioInventarioNoDisponible_lanzaExcepcion() {
        DetallePedidoRequest detalle = new DetallePedidoRequest();
        detalle.setProductoId(1L);
        detalle.setProductoCodigo("PROD-001");
        detalle.setProductoNombre("Producto A");
        detalle.setCantidad(5);
        detalle.setPrecioUnitario(500.0);

        PedidoRequest request = new PedidoRequest();
        request.setClienteEmail("cliente@test.cl");
        request.setClienteNombre("Cliente Test");
        request.setDetalles(List.of(detalle));

        when(inventarioClient.obtenerStockProducto(1L)).thenReturn(-1);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.crear(request));
        assertTrue(ex.getMessage().contains("no disponible") || ex.getMessage().contains("inventario"));
    }
}
