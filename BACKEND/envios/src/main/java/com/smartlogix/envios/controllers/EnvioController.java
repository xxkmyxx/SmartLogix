package com.smartlogix.envios.controllers;

import com.smartlogix.envios.dto.EnvioRequest;
import com.smartlogix.envios.dto.EnvioResponse;
import com.smartlogix.envios.entities.EstadoEnvio;
import com.smartlogix.envios.services.EnvioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
@Tag(name = "Envíos", description = "Gestión de envíos y seguimiento")
public class EnvioController {

    private final EnvioService envioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Crear un nuevo envío para un pedido")
    public ResponseEntity<EnvioResponse> crear(@RequestBody EnvioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar todos los envíos")
    public ResponseEntity<List<EnvioResponse>> listarTodos() {
        return ResponseEntity.ok(envioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Buscar envío por ID")
    public ResponseEntity<EnvioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(envioService.buscarPorId(id));
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Buscar envío por ID de pedido")
    public ResponseEntity<EnvioResponse> buscarPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(envioService.buscarPorPedidoId(pedidoId));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar envíos por estado")
    public ResponseEntity<List<EnvioResponse>> listarPorEstado(@PathVariable EstadoEnvio estado) {
        return ResponseEntity.ok(envioService.listarPorEstado(estado));
    }

    @GetMapping("/transportista/{transportistaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Listar envíos asignados a un transportista")
    public ResponseEntity<List<EnvioResponse>> listarPorTransportista(@PathVariable Long transportistaId) {
        return ResponseEntity.ok(envioService.listarPorTransportista(transportistaId));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Actualizar estado del envío (RECOGIDO, EN_TRANSITO, ENTREGADO, etc.)")
    public ResponseEntity<EnvioResponse> actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoEnvio estado) {
        return ResponseEntity.ok(envioService.actualizarEstado(id, estado));
    }
}
