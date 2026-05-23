package com.smartlogix.inventario.dto;

import lombok.Data;

@Data
public class ProductoRequest {
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;
    private Double precio;
    private Integer stockInicial;
    private Integer stockMinimo;
}
