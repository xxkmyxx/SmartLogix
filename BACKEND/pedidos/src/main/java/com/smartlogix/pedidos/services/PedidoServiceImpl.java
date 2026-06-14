package com.smartlogix.pedidos.services;

import com.smartlogix.pedidos.dto.DetallePedidoRequest;
import com.smartlogix.pedidos.dto.PedidoRequest;
import com.smartlogix.pedidos.dto.PedidoResponse;
import com.smartlogix.pedidos.entities.DetallePedido;
import com.smartlogix.pedidos.entities.EstadoPedido;
import com.smartlogix.pedidos.entities.Pedido;
import com.smartlogix.pedidos.repositories.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private InventarioClientService inventarioClient;

    @Override
    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        // Verificar stock con Circuit Breaker antes de crear el pedido
        for (DetallePedidoRequest detalle : request.getDetalles()) {
            Integer stock = inventarioClient.obtenerStockProducto(detalle.getProductoId());
            if (stock == -1) {
                throw new RuntimeException(
                    "Servicio de inventario no disponible. Intente más tarde.");
            }
            if (stock < detalle.getCantidad()) {
                throw new RuntimeException(
                    "Stock insuficiente para el producto: " + detalle.getProductoCodigo()
                    + ". Disponible: " + stock + ", solicitado: " + detalle.getCantidad());
            }
        }

        String numeroPedido = generarNumeroPedido();

        List<DetallePedido> detalles = new ArrayList<>();
        double total = 0.0;

        Pedido pedido = Pedido.builder()
                .numeroPedido(numeroPedido)
                .clienteEmail(request.getClienteEmail())
                .clienteNombre(request.getClienteNombre())
                .observaciones(request.getObservaciones())
                .total(0.0)
                .build();

        for (DetallePedidoRequest dr : request.getDetalles()) {
            double subtotal = dr.getCantidad() * dr.getPrecioUnitario();
            total += subtotal;
            detalles.add(DetallePedido.builder()
                    .pedido(pedido)
                    .productoId(dr.getProductoId())
                    .productoCodigo(dr.getProductoCodigo())
                    .productoNombre(dr.getProductoNombre())
                    .cantidad(dr.getCantidad())
                    .precioUnitario(dr.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build());
        }

        pedido.setTotal(total);
        pedido.setDetalles(detalles);

        Pedido guardado = pedidoRepository.save(pedido);

        // Descontar stock por cada producto del pedido
        for (DetallePedidoRequest dr : request.getDetalles()) {
            inventarioClient.ajustarStock(dr.getProductoId(), -dr.getCantidad());
        }

        return toResponse(guardado);
    }

    @Override
    public PedidoResponse buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        return toResponse(pedido);
    }

    @Override
    public PedidoResponse buscarPorNumeroPedido(String numeroPedido) {
        Pedido pedido = pedidoRepository.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + numeroPedido));
        return toResponse(pedido);
    }

    @Override
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoResponse> listarPorCliente(String clienteEmail) {
        return pedidoRepository.findByClienteEmail(clienteEmail).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoResponse> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO
                || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new RuntimeException(
                "No se puede cambiar el estado de un pedido " + pedido.getEstado());
        }

        pedido.setEstado(nuevoEstado);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public void cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new RuntimeException("No se puede cancelar un pedido ya entregado.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new RuntimeException("El pedido ya está cancelado.");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);

        // Reponer stock al cancelar
        for (DetallePedido d : pedido.getDetalles()) {
            inventarioClient.ajustarStock(d.getProductoId(), d.getCantidad());
        }
    }

    private String generarNumeroPedido() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "PED-" + timestamp;
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<PedidoResponse.DetalleResponse> detallesResp = pedido.getDetalles().stream()
                .map(d -> PedidoResponse.DetalleResponse.builder()
                        .productoId(d.getProductoId())
                        .productoCodigo(d.getProductoCodigo())
                        .productoNombre(d.getProductoNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return PedidoResponse.builder()
                .id(pedido.getId())
                .numeroPedido(pedido.getNumeroPedido())
                .clienteEmail(pedido.getClienteEmail())
                .clienteNombre(pedido.getClienteNombre())
                .estado(pedido.getEstado())
                .fechaCreacion(pedido.getFechaCreacion())
                .fechaActualizacion(pedido.getFechaActualizacion())
                .total(pedido.getTotal())
                .observaciones(pedido.getObservaciones())
                .detalles(detallesResp)
                .build();
    }
}
