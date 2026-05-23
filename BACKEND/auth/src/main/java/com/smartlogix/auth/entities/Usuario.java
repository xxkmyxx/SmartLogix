package com.smartlogix.auth.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, unique = true, length = 80)
    private String email;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false, length = 15)
    private String rol; // ADMIN, OPERADOR, TRANSPORTISTA

    @Builder.Default
    private Boolean activo = true;

    @Builder.Default
    private Integer intentosFallidos = 0;

    private LocalDateTime fechaBloqueo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
