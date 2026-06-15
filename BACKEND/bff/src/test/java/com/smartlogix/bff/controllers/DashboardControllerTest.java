package com.smartlogix.bff.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartlogix.bff.dto.DashboardResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private DashboardController dashboardController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dashboardController, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(dashboardController, "inventarioUrl", "http://inventario");
        ReflectionTestUtils.setField(dashboardController, "pedidosUrl", "http://pedidos");
        lenient().when(request.getHeader("Authorization")).thenReturn(null);
    }

    @Test
    void dashboardFallback_retornaValoresNegativosConServiceUnavailable() {
        ResponseEntity<DashboardResponse> resp = dashboardController.dashboardFallback(request, new RuntimeException());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(-1, resp.getBody().getTotalProductos());
        assertEquals(-1, resp.getBody().getTotalPedidos());
    }

    @Test
    void getDashboard_agregaEstadosPedidos() throws Exception {
        String productos = "[{\"id\":1},{\"id\":2}]";
        String alertas = "[]";
        String pedidos = "[{\"estado\":\"PENDIENTE\"},{\"estado\":\"EN_TRANSITO\"},{\"estado\":\"ENTREGADO\"},{\"estado\":\"ENTREGADO\"}]";

        when(restTemplate.exchange(contains("productos"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(productos));
        when(restTemplate.exchange(contains("alertas"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(alertas));
        when(restTemplate.exchange(contains("pedidos"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(pedidos));

        ResponseEntity<DashboardResponse> resp = dashboardController.getDashboard(request);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        DashboardResponse body = resp.getBody();
        assertNotNull(body);
        assertEquals(2, body.getTotalProductos());
        assertEquals(4, body.getTotalPedidos());
        assertEquals(1, body.getPedidosPendientes());
        assertEquals(1, body.getPedidosEnTransito());
        assertEquals(2, body.getPedidosEntregados());
    }

    @Test
    void getDashboard_inventarioNoDisponible_sigueRetornandoPedidos() throws Exception {
        String pedidos = "[{\"estado\":\"PENDIENTE\"}]";

        when(restTemplate.exchange(contains("productos"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new RuntimeException("inventario down"));
        when(restTemplate.exchange(contains("alertas"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new RuntimeException("inventario down"));
        when(restTemplate.exchange(contains("pedidos"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(pedidos));

        ResponseEntity<DashboardResponse> resp = dashboardController.getDashboard(request);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(0, resp.getBody().getTotalProductos());
        assertEquals(1, resp.getBody().getTotalPedidos());
    }

    @Test
    void getDashboard_listaPedidosVacia_retornaCeros() throws Exception {
        when(restTemplate.exchange(contains("productos"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("[]"));
        when(restTemplate.exchange(contains("alertas"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("[]"));
        when(restTemplate.exchange(contains("pedidos"), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("[]"));

        ResponseEntity<DashboardResponse> resp = dashboardController.getDashboard(request);

        assertEquals(0, resp.getBody().getTotalPedidos());
        assertEquals(0, resp.getBody().getPedidosPendientes());
    }
}
