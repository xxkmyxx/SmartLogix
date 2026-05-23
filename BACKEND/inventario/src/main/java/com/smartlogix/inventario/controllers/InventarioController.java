package com.smartlogix.inventario.controllers;

import com.smartlogix.inventario.entities.Inventario;
import com.smartlogix.inventario.services.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Inventario por Bodega", description = "Stock por bodega y sincronización entre bodegas")
@RestController
@RequestMapping("/api/inventario/stock")
@CrossOrigin(origins = "http://localhost:5173")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @Operation(summary = "Ver stock de todos los productos en una bodega")
    @GetMapping("/bodega/{bodegaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<Inventario>> stockPorBodega(@PathVariable Long bodegaId) {
        try {
            return ResponseEntity.ok(inventarioService.findByBodega(bodegaId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Ver stock de un producto en todas las bodegas")
    @GetMapping("/bodegas/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<Inventario>> stockPorProducto(@PathVariable Long productoId) {
        try {
            return ResponseEntity.ok(inventarioService.findByProducto(productoId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Sincronizar stock de un producto entre dos bodegas")
    @PostMapping("/sincronizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> sincronizar(@RequestBody Map<String, Object> body, Authentication auth) {
        try {
            Long productoId = Long.valueOf(body.get("productoId").toString());
            Long origenId = Long.valueOf(body.get("bodegaOrigenId").toString());
            Long destinoId = Long.valueOf(body.get("bodegaDestinoId").toString());
            Integer cantidad = Integer.valueOf(body.get("cantidad").toString());
            String usuario = auth != null ? auth.getName() : "sistema";

            Inventario resultado = inventarioService.sincronizar(
                    productoId, origenId, destinoId, cantidad, usuario);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
