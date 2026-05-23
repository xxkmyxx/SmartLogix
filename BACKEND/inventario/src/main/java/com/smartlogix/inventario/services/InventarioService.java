package com.smartlogix.inventario.services;

import com.smartlogix.inventario.entities.Inventario;

import java.util.List;

public interface InventarioService {

    List<Inventario> findByBodega(Long bodegaId);

    List<Inventario> findByProducto(Long productoId);

    Inventario sincronizar(Long productoId, Long bodegaOrigenId, Long bodegaDestinoId, Integer cantidad, String usuario);
}
