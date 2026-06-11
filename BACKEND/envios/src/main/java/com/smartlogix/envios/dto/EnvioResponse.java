package com.smartlogix.envios.dto;

import com.smartlogix.envios.entities.EstadoEnvio;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvioResponse {
    private Long id;
    private Long pedidoId;
    private Long transportistaId;
    private String nombreTransportista;
    private String patenteTransportista;
    private EstadoEnvio estado;
    private LocalDate fechaEstimada;
    private LocalDateTime fechaCreacion;
}
