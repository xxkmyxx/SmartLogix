package com.smartlogix.bff.controllers;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@Tag(name = "Inventario Proxy", description = "Redirige al microservicio de inventario")
public class InventarioProxyController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.inventario.url}")
    private String inventarioUrl;

    @CircuitBreaker(name = "inventario", fallbackMethod = "servicioNoDisponible")
    @RequestMapping(value = {"/api/inventario", "/api/inventario/**"})
    @Operation(summary = "Proxy hacia microservicio Inventario")
    public ResponseEntity<String> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) String body) {
        return forward(request, body, inventarioUrl);
    }

    public ResponseEntity<String> servicioNoDisponible(HttpServletRequest request,
                                                        String body, Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Servicio de inventario no disponible\"}");
    }

    private ResponseEntity<String> forward(HttpServletRequest request, String body, String baseUrl) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String targetUrl = baseUrl + uri + (query != null ? "?" + query : "");

        HttpHeaders headers = new HttpHeaders();
        String auth = request.getHeader("Authorization");
        if (auth != null) headers.set("Authorization", auth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        try {
            return restTemplate.exchange(targetUrl, method, entity, String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        }
    }
}
