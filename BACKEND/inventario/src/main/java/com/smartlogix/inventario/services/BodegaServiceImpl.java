package com.smartlogix.inventario.services;

import com.smartlogix.inventario.entities.Bodega;
import com.smartlogix.inventario.repositories.BodegaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BodegaServiceImpl implements BodegaService {

    private final BodegaRepository bodegaRepository;

    public BodegaServiceImpl(BodegaRepository bodegaRepository) {
        this.bodegaRepository = bodegaRepository;
    }

    @Override
    public List<Bodega> findAll() {
        return bodegaRepository.findByActivaTrue();
    }

    @Override
    public Optional<Bodega> findById(Long id) {
        return bodegaRepository.findById(id);
    }

    @Override
    @Transactional
    public Bodega save(Bodega bodega) {
        return bodegaRepository.save(bodega);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Bodega bodega = bodegaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada: " + id));
        bodega.setActiva(false);
        bodegaRepository.save(bodega);
    }
}
