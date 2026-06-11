package com.smartlogix.bff.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoResponse {
    private Long pedidoId;
    private String numeroPedido;
    private String clienteNombre;
    private String estadoPedido;
    private LocalDateTime fechaCreacion;
    private Double total;

    private Long envioId;
    private String estadoEnvio;
    private String nombreTransportista;
    private LocalDate fechaEstimada;
}
