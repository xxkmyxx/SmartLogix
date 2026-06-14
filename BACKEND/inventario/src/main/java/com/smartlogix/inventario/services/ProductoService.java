package com.smartlogix.inventario.services;

import com.smartlogix.inventario.entities.MovimientoStock;
import com.smartlogix.inventario.entities.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<Producto> findAll();

    Optional<Producto> findById(Long id);

    Producto crear(String codigo, String nombre, String descripcion,
                   String categoria, Double precio,
                   Integer stockInicial, Integer stockMinimo);

    Producto actualizar(Long id, Producto datos);

    void desactivar(Long id);

    List<Producto> findBajoStockMinimo();

    Producto actualizarStock(Long productoId, Long bodegaId,
                             Integer cantidad, String tipo,
                             String motivo, String usuarioResponsable);

    List<MovimientoStock> historialMovimientos(Long productoId);

    void ajustarStockInterno(Long productoId, int delta);
}
