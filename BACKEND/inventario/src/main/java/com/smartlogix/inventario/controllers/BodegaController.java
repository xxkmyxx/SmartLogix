package com.smartlogix.inventario.controllers;

import com.smartlogix.inventario.entities.Bodega;
import com.smartlogix.inventario.services.BodegaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Bodegas", description = "Gestión de bodegas")
@RestController
@RequestMapping("/api/inventario/bodegas")
@CrossOrigin(origins = "http://localhost:5173")
public class BodegaController {

    private final BodegaService bodegaService;

    public BodegaController(BodegaService bodegaService) {
        this.bodegaService = bodegaService;
    }

    @Operation(summary = "Listar bodegas activas")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<List<Bodega>> listar() {
        return ResponseEntity.ok(bodegaService.findAll());
    }

    @Operation(summary = "Obtener bodega por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    public ResponseEntity<?> obtener(@PathVariable Long id) {
        return bodegaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registrar nueva bodega")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Bodega> crear(@RequestBody Bodega bodega) {
        bodega.setActiva(true);
        return ResponseEntity.status(HttpStatus.CREATED).body(bodegaService.save(bodega));
    }

    @Operation(summary = "Actualizar bodega")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Bodega datos) {
        return bodegaService.findById(id).map(b -> {
            b.setNombre(datos.getNombre());
            b.setDireccion(datos.getDireccion());
            b.setCapacidad(datos.getCapacidad());
            return ResponseEntity.ok(bodegaService.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Desactivar bodega")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivar(@PathVariable Long id) {
        try {
            bodegaService.desactivar(id);
            return ResponseEntity.ok("Bodega desactivada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
