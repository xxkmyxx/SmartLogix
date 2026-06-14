package com.smartlogix.pedidos.controllers;

import com.smartlogix.pedidos.dto.PedidoRequest;
import com.smartlogix.pedidos.dto.PedidoResponse;
import com.smartlogix.pedidos.entities.EstadoPedido;
import com.smartlogix.pedidos.services.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gestión de pedidos de clientes")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Listar todos los pedidos")
    public ResponseEntity<List<PedidoResponse>> listar() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener pedido por ID")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pedidoService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/public/numero/{numeroPedido}")
    @Operation(summary = "Buscar pedido por número (público, sin autenticación)")
    public ResponseEntity<?> buscarPorNumeroPublico(@PathVariable String numeroPedido) {
        try {
            return ResponseEntity.ok(pedidoService.buscarPorNumeroPedido(numeroPedido));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/numero/{numeroPedido}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Buscar pedido por número")
    public ResponseEntity<?> buscarPorNumero(@PathVariable String numeroPedido) {
        try {
            return ResponseEntity.ok(pedidoService.buscarPorNumeroPedido(numeroPedido));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cliente/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar pedidos de un cliente")
    public ResponseEntity<List<PedidoResponse>> listarPorCliente(@PathVariable String email) {
        return ResponseEntity.ok(pedidoService.listarPorCliente(email));
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Listar pedidos por estado")
    public ResponseEntity<List<PedidoResponse>> listarPorEstado(@PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.listarPorEstado(estado));
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Listar pedidos del cliente autenticado (portal B2B)")
    public ResponseEntity<List<PedidoResponse>> misPedidos(Authentication auth) {
        return ResponseEntity.ok(pedidoService.listarPorCliente(auth.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'CLIENTE')")
    @Operation(summary = "Crear nuevo pedido (verifica stock vía Circuit Breaker)")
    public ResponseEntity<?> crear(@RequestBody PedidoRequest request) {
        try {
            PedidoResponse pedido = pedidoService.crear(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Cambiar estado de un pedido")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id,
                                           @RequestParam EstadoPedido nuevoEstado) {
        try {
            return ResponseEntity.ok(pedidoService.cambiarEstado(id, nuevoEstado));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancelar pedido (solo ADMIN)")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        try {
            pedidoService.cancelar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
