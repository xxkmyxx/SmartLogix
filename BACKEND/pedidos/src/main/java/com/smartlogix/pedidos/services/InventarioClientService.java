package com.smartlogix.pedidos.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Llama al microservicio de Inventario para verificar stock disponible.
 * Implementa el patrón Circuit Breaker (SRS - requisito arquitectónico).
 * Si Inventario no responde, el circuito se abre y se retorna un fallback
 * conservador (stock = 0), evitando que Pedidos quede bloqueado.
 */
@Service
public class InventarioClientService {

    private static final Logger log = LoggerFactory.getLogger(InventarioClientService.class);
    private static final String CB_NAME = "inventario";

    private final RestTemplate restTemplate;

    @Value("${services.inventario.url}")
    private String inventarioUrl;

    public InventarioClientService() {
        this.restTemplate = new RestTemplate();
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "stockFallback")
    public Integer obtenerStockProducto(Long productoId) {
        String url = inventarioUrl + "/api/inventario/stock/producto/" + productoId;
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response != null && response.containsKey("stockTotal")) {
            return ((Number) response.get("stockTotal")).intValue();
        }
        return 0;
    }

    public Integer stockFallback(Long productoId, Throwable t) {
        log.warn("Circuit Breaker activo para inventario. productoId={}, causa={}", productoId, t.getMessage());
        return -1;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "ajusteFallback")
    public boolean ajustarStock(Long productoId, int delta) {
        String url = inventarioUrl + "/api/inventario/stock/interno/ajuste";
        Map<String, Object> body = Map.of("productoId", productoId, "delta", delta);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean ajusteFallback(Long productoId, int delta, Throwable t) {
        log.warn("Circuit Breaker activo al ajustar stock. productoId={}, delta={}, causa={}", productoId, delta, t.getMessage());
        return false;
    }
}
