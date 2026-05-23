package com.smartlogix.inventario.repositories;

import com.smartlogix.inventario.entities.Bodega;
import com.smartlogix.inventario.entities.Inventario;
import com.smartlogix.inventario.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByBodega(Bodega bodega);

    List<Inventario> findByProducto(Producto producto);

    Optional<Inventario> findByProductoAndBodega(Producto producto, Bodega bodega);

    // Suma total de stock de un producto en todas las bodegas
    @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM Inventario i WHERE i.producto.id = :productoId")
    Integer sumCantidadByProductoId(@Param("productoId") Long productoId);
}
