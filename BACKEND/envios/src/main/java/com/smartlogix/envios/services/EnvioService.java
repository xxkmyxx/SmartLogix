package com.smartlogix.envios.services;

import com.smartlogix.envios.dto.EnvioRequest;
import com.smartlogix.envios.dto.EnvioResponse;
import com.smartlogix.envios.entities.EstadoEnvio;

import java.util.List;

public interface EnvioService {
    EnvioResponse crear(EnvioRequest request);
    EnvioResponse buscarPorId(Long id);
    EnvioResponse buscarPorPedidoId(Long pedidoId);
    List<EnvioResponse> listarTodos();
    List<EnvioResponse> listarPorEstado(EstadoEnvio estado);
    List<EnvioResponse> listarPorTransportista(Long transportistaId);
    EnvioResponse actualizarEstado(Long id, EstadoEnvio nuevoEstado);
}
