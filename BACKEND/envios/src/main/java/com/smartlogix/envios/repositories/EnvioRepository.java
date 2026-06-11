package com.smartlogix.envios.repositories;

import com.smartlogix.envios.entities.Envio;
import com.smartlogix.envios.entities.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByPedidoId(Long pedidoId);
    List<Envio> findByEstado(EstadoEnvio estado);
    List<Envio> findByTransportistaId(Long transportistaId);
}
