package com.center.waterorder.service;

import com.center.waterorder.dto.MenuItemDto;
import com.center.waterorder.model.MenuItem;
import com.center.waterorder.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    
    private final MenuItemRepository menuItemRepository;
    
    @Transactional(readOnly = true)
    public List<MenuItemDto> getAllMenuItems() {
        return menuItemRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MenuItemDto createMenuItem(Long categoryId, String name, Integer displayOrder) {
        MenuItem menuItem = new MenuItem();
        menuItem.setCategoryId(categoryId);
        menuItem.setName(name);
        menuItem.setDisplayOrder(displayOrder);
        menuItem.setActive(true);
        
        MenuItem saved = menuItemRepository.save(menuItem);
        return toDto(saved);
    }
    
    @Transactional
    public MenuItemDto updateMenuItem(Long id, String name, Integer displayOrder) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        
        menuItem.setName(name);
        menuItem.setDisplayOrder(displayOrder);
        
        MenuItem updated = menuItemRepository.save(menuItem);
        return toDto(updated);
    }
    
    @Transactional
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new RuntimeException("Menu item not found");
        }
        menuItemRepository.deleteById(id);
    }
    
    private MenuItemDto toDto(MenuItem menuItem) {
        MenuItemDto dto = new MenuItemDto();
        dto.setId(menuItem.getId());
        dto.setCategoryId(menuItem.getCategoryId());
        dto.setName(menuItem.getName());
        dto.setDisplayOrder(menuItem.getDisplayOrder());
        dto.setActive(menuItem.getActive());
        return dto;
    }
}
