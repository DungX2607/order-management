package com.center.waterorder.service;

import com.center.waterorder.dto.CategoryDto;
import com.center.waterorder.model.Category;
import com.center.waterorder.repository.CategoryRepository;
import com.center.waterorder.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CategoryDto createCategory(String name, Integer displayOrder) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Category name already exists");
        }
        
        Category category = new Category();
        category.setName(name);
        category.setDisplayOrder(displayOrder);
        
        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }
    
    @Transactional
    public CategoryDto updateCategory(Long id, String name, Integer displayOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setName(name);
        category.setDisplayOrder(displayOrder);
        
        Category updated = categoryRepository.save(category);
        return toDto(updated);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        
        long itemCount = menuItemRepository.countByCategoryId(id);
        if (itemCount > 0) {
            throw new RuntimeException("Cannot delete category with existing menu items");
        }
        
        categoryRepository.deleteById(id);
    }
    
    private CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDisplayOrder(category.getDisplayOrder());
        return dto;
    }
}
