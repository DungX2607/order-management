package com.center.waterorder;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    
    @Test
    public void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456aA@";
        String hashedPassword = encoder.encode(rawPassword);
        
        System.out.println("========================================");
        System.out.println("Raw password: " + rawPassword);
        System.out.println("BCrypt hash: " + hashedPassword);
        System.out.println("========================================");
        
        // Verify it works
        boolean matches = encoder.matches(rawPassword, hashedPassword);
        System.out.println("Verification matches: " + matches);
        
        // Test with the old hash from data.sql
        String oldHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean oldMatches = encoder.matches(rawPassword, oldHash);
        System.out.println("Old hash matches: " + oldMatches);
    }
}
