package com.smartlogix.pedidos.dto;

import lombok.Data;

@Data
public class DetallePedidoRequest {
    private Long productoId;
    private String productoCodigo;
    private String productoNombre;
    private Integer cantidad;
    private Double precioUnitario;
}
