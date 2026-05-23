package com.smartlogix.inventario.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos_stock")
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "bodega_id")
    private Bodega bodega;

    // ENTRADA | SALIDA | AJUSTE
    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Integer cantidad;

    private String motivo;

    @Column(name = "usuario_responsable")
    private String usuarioResponsable;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;
}
