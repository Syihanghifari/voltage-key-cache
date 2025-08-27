package org.vt.service;

import org.springframework.http.ResponseEntity;
import org.vt.model.AuthResponse;
import org.vt.model.LoginRequest;
import org.vt.model.MessageResponse;
import org.vt.model.RegisterRequest;

public interface AuthenticationService {

    AuthResponse login(LoginRequest request);
    ResponseEntity<MessageResponse> register (RegisterRequest request);
}
