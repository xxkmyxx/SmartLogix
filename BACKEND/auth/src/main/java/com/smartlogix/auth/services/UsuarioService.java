package com.smartlogix.auth.services;

import com.smartlogix.auth.entities.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> listarTodos();
    List<Usuario> listarActivos();
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorEmail(String email);
    Usuario actualizar(Long id, Usuario datosNuevos);
    void eliminar(Long id);
}
