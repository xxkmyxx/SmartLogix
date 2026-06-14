package com.smartlogix.bff.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.bff.dto.SeguimientoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Seguimiento Público", description = "Consulta de estado de pedido sin autenticación")
public class SeguimientoController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${services.pedidos.url}")
    private String pedidosUrl;

    @Value("${services.envios.url}")
    private String enviosUrl;

    @GetMapping("/seguimiento/{numeroPedido}")
    @Operation(summary = "Consultar estado de pedido y envío por número de pedido")
    public ResponseEntity<?> consultar(@PathVariable String numeroPedido) {
        try {
            String pedidoJson = restTemplate.getForObject(
                    pedidosUrl + "/api/pedidos/public/numero/" + numeroPedido, String.class);

            JsonNode pedido = objectMapper.readTree(pedidoJson);

            if (pedido.has("error")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Pedido no encontrado"));
            }

            SeguimientoResponse.SeguimientoResponseBuilder builder = SeguimientoResponse.builder()
                    .pedidoId(pedido.get("id").asLong())
                    .numeroPedido(pedido.get("numeroPedido").asText())
                    .clienteNombre(pedido.get("clienteNombre").asText())
                    .estadoPedido(pedido.get("estado").asText())
                    .fechaCreacion(pedido.get("fechaCreacion").asText())
                    .total(pedido.get("total").asDouble());

            try {
                Long pedidoId = pedido.get("id").asLong();
                String envioJson = restTemplate.getForObject(
                        enviosUrl + "/api/envios/public/pedido/" + pedidoId, String.class);

                if (envioJson != null && !envioJson.equals("null")) {
                    JsonNode envio = objectMapper.readTree(envioJson);
                    builder.envioId(envio.get("id").asLong())
                           .estadoEnvio(envio.get("estado").asText())
                           .nombreTransportista(envio.get("nombreTransportista").asText("—"));

                    if (!envio.get("fechaEstimada").isNull()) {
                        builder.fechaEstimada(envio.get("fechaEstimada").asText());
                    }
                }
            } catch (Exception ignored) {
            }

            return ResponseEntity.ok(builder.build());

        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Pedido no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al consultar el pedido"));
        }
    }
}
