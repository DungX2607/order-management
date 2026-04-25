package com.center.waterorder.controller;

import com.center.waterorder.dto.MenuItemDto;
import com.center.waterorder.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {
    
    private final MenuItemService menuItemService;
    
    @GetMapping
    public ResponseEntity<List<MenuItemDto>> getAllMenuItems() {
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDto> createMenuItem(@RequestBody Map<String, Object> request) {
        Long categoryId = ((Number) request.get("categoryId")).longValue();
        String name = (String) request.get("name");
        Integer displayOrder = (Integer) request.get("displayOrder");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuItemService.createMenuItem(categoryId, name, displayOrder));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemDto> updateMenuItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Integer displayOrder = (Integer) request.get("displayOrder");
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, name, displayOrder));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
