package com.smartlogix.bff.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private int totalProductos;
    private int alertasStock;
    private int totalPedidos;
    private int pedidosPendientes;
    private int pedidosEnTransito;
    private int pedidosEntregados;
}
