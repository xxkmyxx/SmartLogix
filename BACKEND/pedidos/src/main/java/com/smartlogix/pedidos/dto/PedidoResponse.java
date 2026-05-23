package com.smartlogix.pedidos.dto;

import com.smartlogix.pedidos.entities.EstadoPedido;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PedidoResponse {
    private Long id;
    private String numeroPedido;
    private String clienteEmail;
    private String clienteNombre;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Double total;
    private String observaciones;
    private List<DetalleResponse> detalles;

    @Data
    @Builder
    public static class DetalleResponse {
        private Long productoId;
        private String productoCodigo;
        private String productoNombre;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}
