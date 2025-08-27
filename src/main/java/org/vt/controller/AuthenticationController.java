package org.vt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vt.model.*;
import org.vt.service.AuthenticationService;

@RestController
@RequestMapping(value = "/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody RegisterRequest request) {
        return authenticationService.register(request);
    }
}
