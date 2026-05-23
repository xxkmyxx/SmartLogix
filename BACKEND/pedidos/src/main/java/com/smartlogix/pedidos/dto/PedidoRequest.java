package com.smartlogix.pedidos.dto;

import lombok.Data;

import java.util.List;

@Data
public class PedidoRequest {
    private String clienteEmail;
    private String clienteNombre;
    private String observaciones;
    private List<DetallePedidoRequest> detalles;
}
