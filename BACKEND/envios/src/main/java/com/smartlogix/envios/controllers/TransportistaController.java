package com.smartlogix.envios.controllers;

import com.smartlogix.envios.dto.TransportistaRequest;
import com.smartlogix.envios.entities.Transportista;
import com.smartlogix.envios.services.TransportistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transportistas")
@RequiredArgsConstructor
@Tag(name = "Transportistas", description = "Gestión de transportistas")
public class TransportistaController {

    private final TransportistaService transportistaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar nuevo transportista")
    public ResponseEntity<Transportista> crear(@RequestBody TransportistaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportistaService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar todos los transportistas")
    public ResponseEntity<List<Transportista>> listarTodos() {
        return ResponseEntity.ok(transportistaService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Buscar transportista por ID")
    public ResponseEntity<Transportista> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transportistaService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar transportista")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        transportistaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
