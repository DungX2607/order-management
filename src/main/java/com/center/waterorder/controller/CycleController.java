package com.center.waterorder.controller;

import com.center.waterorder.dto.CycleDto;
import com.center.waterorder.service.CycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cycles")
@RequiredArgsConstructor
public class CycleController {
    
    private final CycleService cycleService;
    
    @GetMapping("/current")
    public ResponseEntity<CycleDto> getCurrentCycle() {
        return ResponseEntity.ok(cycleService.getCurrentCycle());
    }
    
    @PostMapping("/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CycleDto> openCycle() {
        return ResponseEntity.ok(cycleService.openCycle());
    }
    
    @PostMapping("/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> closeCycle() {
        cycleService.closeCycle();
        return ResponseEntity.ok().build();
    }
}
