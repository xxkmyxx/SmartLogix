package com.smartlogix.inventario.services;

import com.smartlogix.inventario.entities.Bodega;
import com.smartlogix.inventario.entities.Inventario;
import com.smartlogix.inventario.entities.MovimientoStock;
import com.smartlogix.inventario.entities.Producto;
import com.smartlogix.inventario.factories.ProductoFactory;
import com.smartlogix.inventario.repositories.BodegaRepository;
import com.smartlogix.inventario.repositories.InventarioRepository;
import com.smartlogix.inventario.repositories.MovimientoStockRepository;
import com.smartlogix.inventario.repositories.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final MovimientoStockRepository movimientoRepository;

    public InventarioServiceImpl(InventarioRepository inventarioRepository,
                                  ProductoRepository productoRepository,
                                  BodegaRepository bodegaRepository,
                                  MovimientoStockRepository movimientoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
        this.bodegaRepository = bodegaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    public List<Inventario> findByBodega(Long bodegaId) {
        Bodega bodega = bodegaRepository.findById(bodegaId)
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada: " + bodegaId));
        return inventarioRepository.findByBodega(bodega);
    }

    @Override
    public List<Inventario> findByProducto(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));
        return inventarioRepository.findByProducto(producto);
    }

    @Override
    @Transactional
    public Inventario sincronizar(Long productoId, Long bodegaOrigenId,
                                   Long bodegaDestinoId, Integer cantidad, String usuario) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));
        Bodega origen = bodegaRepository.findById(bodegaOrigenId)
                .orElseThrow(() -> new RuntimeException("Bodega origen no encontrada"));
        Bodega destino = bodegaRepository.findById(bodegaDestinoId)
                .orElseThrow(() -> new RuntimeException("Bodega destino no encontrada"));

        // Descontar del origen
        Inventario invOrigen = inventarioRepository.findByProductoAndBodega(producto, origen)
                .orElseThrow(() -> new RuntimeException("Sin stock en bodega origen"));
        if (invOrigen.getCantidad() < cantidad) {
            throw new RuntimeException("Stock insuficiente en bodega origen");
        }
        invOrigen.setCantidad(invOrigen.getCantidad() - cantidad);
        invOrigen.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(invOrigen);

        // Agregar al destino
        Optional<Inventario> invDestinoOpt = inventarioRepository.findByProductoAndBodega(producto, destino);
        Inventario invDestino = invDestinoOpt.orElse(new Inventario(null, producto, destino, 0, null));
        invDestino.setCantidad(invDestino.getCantidad() + cantidad);
        invDestino.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(invDestino);

        // Registrar movimientos de salida y entrada (Factory Method)
        MovimientoStock salida = ProductoFactory.crearMovimiento(
                producto, origen, "SALIDA", cantidad, "Sincronización entre bodegas", usuario);
        MovimientoStock entrada = ProductoFactory.crearMovimiento(
                producto, destino, "ENTRADA", cantidad, "Sincronización entre bodegas", usuario);
        movimientoRepository.save(salida);
        movimientoRepository.save(entrada);

        return invDestino;
    }
}
