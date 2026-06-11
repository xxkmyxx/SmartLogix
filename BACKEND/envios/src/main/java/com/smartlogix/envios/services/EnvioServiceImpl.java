package com.smartlogix.envios.services;

import com.smartlogix.envios.dto.EnvioRequest;
import com.smartlogix.envios.dto.EnvioResponse;
import com.smartlogix.envios.entities.Envio;
import com.smartlogix.envios.entities.EstadoEnvio;
import com.smartlogix.envios.repositories.EnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnvioServiceImpl implements EnvioService {

    private final EnvioRepository envioRepository;

    @Override
    public EnvioResponse crear(EnvioRequest request) {
        Envio envio = Envio.builder()
                .pedidoId(request.getPedidoId())
                .transportistaId(request.getTransportistaId())
                .nombreTransportista(request.getNombreTransportista())
                .fechaEstimada(request.getFechaEstimada())
                .estado(EstadoEnvio.PENDIENTE)
                .build();
        return toResponse(envioRepository.save(envio));
    }

    @Override
    public EnvioResponse buscarPorId(Long id) {
        return toResponse(findEnvio(id));
    }

    @Override
    public EnvioResponse buscarPorPedidoId(Long pedidoId) {
        Envio envio = envioRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado para pedido id: " + pedidoId));
        return toResponse(envio);
    }

    @Override
    public List<EnvioResponse> listarTodos() {
        return envioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnvioResponse> listarPorEstado(EstadoEnvio estado) {
        return envioRepository.findByEstado(estado).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnvioResponse> listarPorTransportista(Long transportistaId) {
        return envioRepository.findByTransportistaId(transportistaId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EnvioResponse actualizarEstado(Long id, EstadoEnvio nuevoEstado) {
        Envio envio = findEnvio(id);
        envio.setEstado(nuevoEstado);
        return toResponse(envioRepository.save(envio));
    }

    private Envio findEnvio(Long id) {
        return envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con id: " + id));
    }

    private EnvioResponse toResponse(Envio envio) {
        return EnvioResponse.builder()
                .id(envio.getId())
                .pedidoId(envio.getPedidoId())
                .transportistaId(envio.getTransportistaId())
                .nombreTransportista(envio.getNombreTransportista())
                .estado(envio.getEstado())
                .fechaEstimada(envio.getFechaEstimada())
                .fechaCreacion(envio.getFechaCreacion())
                .build();
    }
}
