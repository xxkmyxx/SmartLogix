package com.smartlogix.inventario.repositories;

import com.smartlogix.inventario.entities.MovimientoStock;
import com.smartlogix.inventario.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    List<MovimientoStock> findByProductoOrderByFechaMovimientoDesc(Producto producto);
}
