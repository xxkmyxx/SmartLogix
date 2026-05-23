package com.smartlogix.inventario.services;

import com.smartlogix.inventario.entities.Bodega;

import java.util.List;
import java.util.Optional;

public interface BodegaService {

    List<Bodega> findAll();

    Optional<Bodega> findById(Long id);

    Bodega save(Bodega bodega);

    void desactivar(Long id);
}
