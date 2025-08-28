package org.vt.service;

import org.springframework.http.ResponseEntity;
import org.vt.config.mybatis.entity.User;
import org.vt.model.AuthResponse;
import org.vt.model.LoginRequest;
import org.vt.model.MessageResponse;
import org.vt.model.RegisterRequest;

import java.util.List;

public interface AuthenticationService {

    AuthResponse login(LoginRequest request);
    ResponseEntity<MessageResponse> register (RegisterRequest request);
    ResponseEntity<List<User>> getAllUser ();
}
