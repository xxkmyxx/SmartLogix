package com.smartlogix.inventario.factories;

import com.smartlogix.inventario.entities.Bodega;
import com.smartlogix.inventario.entities.MovimientoStock;
import com.smartlogix.inventario.entities.Producto;

import java.time.LocalDateTime;

public class ProductoFactory {

    public static Producto crear(String codigo, String nombre, String descripcion,
                                  String categoria, Double precio,
                                  Integer stockInicial, Integer stockMinimo) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setStockActual(stockInicial);
        producto.setStockMinimo(stockMinimo);
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
        return producto;
    }

    public static MovimientoStock crearMovimiento(Producto producto, Bodega bodega,
                                                   String tipo, Integer cantidad,
                                                   String motivo, String usuarioResponsable) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProducto(producto);
        movimiento.setBodega(bodega);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setUsuarioResponsable(usuarioResponsable);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        return movimiento;
    }
}
