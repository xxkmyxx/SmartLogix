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
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final BodegaRepository bodegaRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoStockRepository movimientoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               BodegaRepository bodegaRepository,
                               InventarioRepository inventarioRepository,
                               MovimientoStockRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.bodegaRepository = bodegaRepository;
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    public List<Producto> findAll() {
        return productoRepository.findByActivoTrue();
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    @Transactional
    public Producto crear(String codigo, String nombre, String descripcion,
                          String categoria, Double precio,
                          Integer stockInicial, Integer stockMinimo) {
        if (codigo == null || codigo.isBlank()) {
            codigo = "PROD-" + System.currentTimeMillis();
        }
        Producto producto = ProductoFactory.crear(codigo, nombre, descripcion,
                categoria, precio,
                stockInicial != null ? stockInicial : 0,
                stockMinimo != null ? stockMinimo : 0);
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        existente.setNombre(datos.getNombre());
        existente.setDescripcion(datos.getDescripcion());
        existente.setCategoria(datos.getCategoria());
        existente.setPrecio(datos.getPrecio());
        if (datos.getStockActual() != null) existente.setStockActual(datos.getStockActual());
        existente.setStockMinimo(datos.getStockMinimo() != null ? datos.getStockMinimo() : existente.getStockMinimo());
        return productoRepository.save(existente);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    public List<Producto> findBajoStockMinimo() {
        return productoRepository.findProductosBajoStock();
    }

    @Override
    @Transactional
    public Producto actualizarStock(Long productoId, Long bodegaId,
                                    Integer cantidad, String tipo,
                                    String motivo, String usuarioResponsable) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));

        Bodega bodega = bodegaRepository.findById(bodegaId)
                .orElseThrow(() -> new RuntimeException("Bodega no encontrada: " + bodegaId));

        // Actualizar registro en tabla inventario por bodega
        Optional<Inventario> invOpt = inventarioRepository.findByProductoAndBodega(producto, bodega);
        Inventario inventario = invOpt.orElse(new Inventario(null, producto, bodega, 0, null));

        int nuevaCantidadBodega = inventario.getCantidad() + cantidad;
        if (nuevaCantidadBodega < 0) {
            throw new RuntimeException("Stock insuficiente en bodega para realizar la operación");
        }
        inventario.setCantidad(nuevaCantidadBodega);
        inventario.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario);

        // Recalcular stock total del producto sumando todas las bodegas
        Integer stockTotal = inventarioRepository.sumCantidadByProductoId(productoId);
        producto.setStockActual(stockTotal);
        productoRepository.save(producto);

        // Registrar movimiento (Factory Method)
        MovimientoStock movimiento = ProductoFactory.crearMovimiento(
                producto, bodega, tipo, cantidad, motivo, usuarioResponsable);
        movimientoRepository.save(movimiento);

        return producto;
    }

    @Override
    public List<MovimientoStock> historialMovimientos(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productoId));
        return movimientoRepository.findByProductoOrderByFechaMovimientoDesc(producto);
    }
}
