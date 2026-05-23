package com.smartlogix.pedidos.services;

import com.smartlogix.pedidos.dto.PedidoRequest;
import com.smartlogix.pedidos.dto.PedidoResponse;
import com.smartlogix.pedidos.entities.EstadoPedido;

import java.util.List;

public interface PedidoService {
    PedidoResponse crear(PedidoRequest request);
    PedidoResponse buscarPorId(Long id);
    PedidoResponse buscarPorNumeroPedido(String numeroPedido);
    List<PedidoResponse> listarTodos();
    List<PedidoResponse> listarPorCliente(String clienteEmail);
    List<PedidoResponse> listarPorEstado(EstadoPedido estado);
    PedidoResponse cambiarEstado(Long id, EstadoPedido nuevoEstado);
    void cancelar(Long id);
}
