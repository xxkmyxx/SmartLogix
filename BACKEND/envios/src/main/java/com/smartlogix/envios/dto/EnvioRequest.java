package com.smartlogix.envios.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EnvioRequest {
    private Long pedidoId;
    private Long transportistaId;
    private LocalDate fechaEstimada;
}
