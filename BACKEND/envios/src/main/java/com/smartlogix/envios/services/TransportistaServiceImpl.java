package com.smartlogix.envios.services;

import com.smartlogix.envios.dto.TransportistaRequest;
import com.smartlogix.envios.entities.Transportista;
import com.smartlogix.envios.repositories.TransportistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportistaServiceImpl implements TransportistaService {

    private final TransportistaRepository transportistaRepository;

    @Override
    public Transportista crear(TransportistaRequest request) {
        Transportista t = Transportista.builder()
                .nombre(request.getNombre())
                .telefono(request.getTelefono())
                .patente(request.getPatente())
                .build();
        return transportistaRepository.save(t);
    }

    @Override
    public List<Transportista> listarTodos() {
        return transportistaRepository.findAll();
    }

    @Override
    public Transportista buscarPorId(Long id) {
        return transportistaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transportista no encontrado con id: " + id));
    }

    @Override
    public void eliminar(Long id) {
        transportistaRepository.deleteById(id);
    }
}
