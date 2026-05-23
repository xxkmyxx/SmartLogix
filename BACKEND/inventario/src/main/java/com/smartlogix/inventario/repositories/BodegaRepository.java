package com.smartlogix.inventario.repositories;

import com.smartlogix.inventario.entities.Bodega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BodegaRepository extends JpaRepository<Bodega, Long> {

    List<Bodega> findByActivaTrue();
}
