package com.smartlogix.bff.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoResponse {
    private Long pedidoId;
    private String numeroPedido;
    private String clienteNombre;
    private String estadoPedido;
    private String fechaCreacion;
    private Double total;

    private Long envioId;
    private String estadoEnvio;
    private String nombreTransportista;
    private String fechaEstimada;
}
