package com.smartlogix.auth.services;

import com.smartlogix.auth.dto.LoginRequest;
import com.smartlogix.auth.dto.LoginResponse;
import com.smartlogix.auth.dto.RegisterRequest;
import com.smartlogix.auth.entities.Usuario;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    Usuario register(RegisterRequest request);
    boolean validateToken(String token);
}
