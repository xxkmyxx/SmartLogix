package com.smartlogix.bff.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.bff.dto.DashboardResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Agrega datos de inventario y pedidos para el frontend")
public class DashboardController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${services.inventario.url}")
    private String inventarioUrl;

    @Value("${services.pedidos.url}")
    private String pedidosUrl;

    @GetMapping
    @CircuitBreaker(name = "inventario", fallbackMethod = "dashboardFallback")
    @Operation(summary = "Resumen general del sistema para la pantalla principal")
    public ResponseEntity<DashboardResponse> getDashboard(HttpServletRequest request) throws Exception {
        HttpHeaders headers = buildHeaders(request);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Productos
        int totalProductos = 0;
        try {
            ResponseEntity<String> productosResp = restTemplate.exchange(
                    inventarioUrl + "/api/inventario/productos", HttpMethod.GET, entity, String.class);
            JsonNode productos = objectMapper.readTree(productosResp.getBody());
            totalProductos = productos.isArray() ? productos.size() : 0;
        } catch (Exception ignored) {}

        // Alertas de stock bajo
        int alertasStock = 0;
        try {
            ResponseEntity<String> alertasResp = restTemplate.exchange(
                    inventarioUrl + "/api/inventario/alertas", HttpMethod.GET, entity, String.class);
            JsonNode alertas = objectMapper.readTree(alertasResp.getBody());
            alertasStock = alertas.isArray() ? alertas.size() : 0;
        } catch (Exception ignored) {}

        // Pedidos
        ResponseEntity<String> pedidosResp = restTemplate.exchange(
                pedidosUrl + "/api/pedidos", HttpMethod.GET, entity, String.class);
        JsonNode pedidos = objectMapper.readTree(pedidosResp.getBody());

        int totalPedidos = 0, pendientes = 0, enTransito = 0, entregados = 0;
        if (pedidos.isArray()) {
            totalPedidos = pedidos.size();
            for (JsonNode p : pedidos) {
                String estado = p.path("estado").asText("");
                switch (estado) {
                    case "PENDIENTE" -> pendientes++;
                    case "EN_TRANSITO" -> enTransito++;
                    case "ENTREGADO" -> entregados++;
                }
            }
        }

        DashboardResponse response = DashboardResponse.builder()
                .totalProductos(totalProductos)
                .alertasStock(alertasStock)
                .totalPedidos(totalPedidos)
                .pedidosPendientes(pendientes)
                .pedidosEnTransito(enTransito)
                .pedidosEntregados(entregados)
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<DashboardResponse> dashboardFallback(HttpServletRequest request, Exception e) {
        DashboardResponse vacio = DashboardResponse.builder()
                .totalProductos(-1)
                .alertasStock(-1)
                .totalPedidos(-1)
                .pedidosPendientes(-1)
                .pedidosEnTransito(-1)
                .pedidosEntregados(-1)
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(vacio);
    }

    private HttpHeaders buildHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        String auth = request.getHeader("Authorization");
        if (auth != null) headers.set("Authorization", auth);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}
