package com.center.waterorder.controller;

import com.center.waterorder.dto.CategoryDto;
import com.center.waterorder.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Integer displayOrder = (Integer) request.get("displayOrder");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(name, displayOrder));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Integer displayOrder = (Integer) request.get("displayOrder");
        return ResponseEntity.ok(categoryService.updateCategory(id, name, displayOrder));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
