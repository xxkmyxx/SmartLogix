package com.smartlogix.envios.services;

import com.smartlogix.envios.dto.TransportistaRequest;
import com.smartlogix.envios.entities.Transportista;

import java.util.List;

public interface TransportistaService {
    Transportista crear(TransportistaRequest request);
    List<Transportista> listarTodos();
    Transportista buscarPorId(Long id);
    void eliminar(Long id);
}
