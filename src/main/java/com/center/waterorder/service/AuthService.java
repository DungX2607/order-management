package com.center.waterorder.service;

import com.center.waterorder.config.JwtUtil;
import com.center.waterorder.dto.LoginRequest;
import com.center.waterorder.dto.LoginResponse;
import com.center.waterorder.dto.RegisterRequest;
import com.center.waterorder.dto.UserDto;
import com.center.waterorder.exception.UsernameAlreadyExistsException;
import com.center.waterorder.model.User;
import com.center.waterorder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Transactional
    public UserDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username đã tồn tại");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole("MEMBER");
        user.setActive(true);
        
        User saved = userRepository.save(user);
        return toDto(saved);
    }
    
    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setActive(user.getActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
