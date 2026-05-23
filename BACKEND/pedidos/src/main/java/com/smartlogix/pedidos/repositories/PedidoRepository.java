package com.smartlogix.pedidos.repositories;

import com.smartlogix.pedidos.entities.EstadoPedido;
import com.smartlogix.pedidos.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
    List<Pedido> findByClienteEmail(String clienteEmail);
    List<Pedido> findByEstado(EstadoPedido estado);
    boolean existsByNumeroPedido(String numeroPedido);
}
