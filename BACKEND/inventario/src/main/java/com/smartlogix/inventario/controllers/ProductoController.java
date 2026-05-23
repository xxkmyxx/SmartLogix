package com.smartlogix.inventario.controllers;

import com.smartlogix.inventario.dto.ProductoRequest;
import com.smartlogix.inventario.dto.StockUpdateRequest;
import com.smartlogix.inventario.entities.MovimientoStock;
import com.smartlogix.inventario.entities.Producto;
import com.smartlogix.inventario.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Productos", description = "Gestión de productos e inventario")
@RestController
@RequestMapping("/api/inventario")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Listar productos con niveles de stock")
    @GetMapping("/productos")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.findAll());
    }

    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/productos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        Optional<Producto> producto = productoService.findById(id);
        return producto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registrar nuevo producto")
    @PostMapping("/productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@RequestBody ProductoRequest request) {
        Producto producto = productoService.crear(
                request.getCodigo(), request.getNombre(), request.getDescripcion(),
                request.getCategoria(), request.getPrecio(),
                request.getStockInicial(), request.getStockMinimo());
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @Operation(summary = "Actualizar datos de un producto")
    @PutMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Producto datos) {
        try {
            return ResponseEntity.ok(productoService.actualizar(id, datos));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Desactivar producto")
    @DeleteMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try {
            productoService.desactivar(id);
            return ResponseEntity.ok("Producto desactivado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Actualizar stock de un producto en una bodega")
    @PutMapping("/productos/{id}/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<?> actualizarStock(@PathVariable Long id,
                                              @RequestBody StockUpdateRequest request,
                                              Authentication auth) {
        try {
            String usuario = auth != null ? auth.getName() : "sistema";
            Producto producto = productoService.actualizarStock(
                    id, request.getBodegaId(), request.getCantidad(),
                    request.getTipo(), request.getMotivo(), usuario);
            return ResponseEntity.ok(producto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Productos bajo stock mínimo (alertas)")
    @GetMapping("/alertas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Producto>> alertas() {
        return ResponseEntity.ok(productoService.findBajoStockMinimo());
    }

    @Operation(summary = "Consulta de stock para uso interno entre microservicios")
    @GetMapping("/stock/producto/{id}")
    public ResponseEntity<?> stockInterno(@PathVariable Long id) {
        return productoService.findById(id)
                .map(p -> ResponseEntity.ok(Map.of("stockTotal", p.getStockActual())))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Historial de movimientos de stock por producto")
    @GetMapping("/productos/{id}/movimientos")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<MovimientoStock>> historial(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.historialMovimientos(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
