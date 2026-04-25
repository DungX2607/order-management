package com.center.waterorder.service;

import com.center.waterorder.config.JwtUtil;
import com.center.waterorder.dto.LoginRequest;
import com.center.waterorder.dto.LoginResponse;
import com.center.waterorder.model.User;
import com.center.waterorder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        if (!user.getActive()) {
            throw new RuntimeException("Account is disabled");
        }
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getFullName());
        
        return new LoginResponse(token, user.getRole(), user.getFullName());
    }
}
