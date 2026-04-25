package com.center.waterorder.controller;

import com.center.waterorder.dto.LoginRequest;
import com.center.waterorder.dto.LoginResponse;
import com.center.waterorder.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // JWT is stateless, logout is handled on client side
        return ResponseEntity.ok().build();
    }
}
