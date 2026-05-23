package com.smartlogix.inventario.dto;

import lombok.Data;

@Data
public class StockUpdateRequest {
    private Integer cantidad;
    private String tipo;   // ENTRADA | SALIDA | AJUSTE
    private String motivo;
    private Long bodegaId;
}
