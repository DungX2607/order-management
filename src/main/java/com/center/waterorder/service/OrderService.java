package com.center.waterorder.service;

import com.center.waterorder.dto.CreateOrderRequest;
import com.center.waterorder.dto.OrderDto;
import com.center.waterorder.model.Cycle;
import com.center.waterorder.model.MenuItem;
import com.center.waterorder.model.Order;
import com.center.waterorder.model.User;
import com.center.waterorder.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CycleRepository cycleRepository;
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    
    @Transactional(readOnly = true)
    public OrderDto getMyOrder(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Cycle cycle = cycleRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN")
                .orElseThrow(() -> new RuntimeException("No open cycle"));
        
        Order order = orderRepository.findByUserIdAndCycleId(user.getId(), cycle.getId())
                .orElseThrow(() -> new RuntimeException("No order found"));
        
        return toDto(order);
    }
    
    @Transactional
    public OrderDto createOrder(String username, CreateOrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Cycle cycle = cycleRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN")
                .orElseThrow(() -> new RuntimeException("No open cycle"));
        
        System.out.println("DEBUG: Creating order for user " + username + " in cycle " + cycle.getId());
        
        if (orderRepository.existsByUserIdAndCycleId(user.getId(), cycle.getId())) {
            throw new RuntimeException("You already have an order in this cycle");
        }
        
        Order order = new Order();
        order.setUserId(user.getId());
        order.setCycleId(cycle.getId());
        order.setMenuItemId(request.getMenuItemId());
        order.setNote(request.getNote());
        order.setPickedUp(false);
        
        Order saved = orderRepository.save(order);
        System.out.println("DEBUG: Order created with ID: " + saved.getId());
        
        return toDto(saved);
    }
    
    @Transactional
    public OrderDto updateOrder(String username, Long orderId, CreateOrderRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Cycle cycle = cycleRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN")
                .orElseThrow(() -> new RuntimeException("No open cycle"));
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUserId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own order");
        }
        
        if (!order.getCycleId().equals(cycle.getId())) {
            throw new RuntimeException("Cannot update order from closed cycle");
        }
        
        order.setMenuItemId(request.getMenuItemId());
        order.setNote(request.getNote());
        
        Order updated = orderRepository.save(order);
        return toDto(updated);
    }
    
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders(String status, String memberName) {
        System.out.println("=== ENTERING getAllOrders ===");
        System.out.println("DEBUG: status=" + status + ", memberName=" + memberName);
        
        // Use same logic as CycleService.getCurrentCycle()
        System.out.println("DEBUG: About to query for OPEN cycle...");
        Cycle cycle = cycleRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN")
                .orElse(null);
        
        System.out.println("DEBUG: OPEN cycle result: " + (cycle != null ? cycle.getId() : "null"));
        
        if (cycle == null) {
            // Try to find most recent CLOSED cycle
            System.out.println("DEBUG: About to query for CLOSED cycle...");
            cycle = cycleRepository.findFirstByStatusOrderByOpenedAtDesc("CLOSED")
                    .orElse(null);
            System.out.println("DEBUG: CLOSED cycle result: " + (cycle != null ? cycle.getId() : "null"));
        }
        
        if (cycle == null) {
            // No cycle at all, return empty list instead of throwing exception
            System.out.println("DEBUG: No cycle found, returning empty list");
            return List.of();
        }
        
        System.out.println("DEBUG: Found cycle ID: " + cycle.getId() + ", status: " + cycle.getStatus());
        
        Boolean pickedUp = null;
        if ("picked".equals(status)) {
            pickedUp = true;
        } else if ("unpicked".equals(status)) {
            pickedUp = false;
        }
        
        List<Order> orders = orderRepository.findByCycleIdAndPickedUp(cycle.getId(), pickedUp);
        System.out.println("DEBUG: Found " + orders.size() + " orders for cycle " + cycle.getId());
        
        List<OrderDto> dtos = orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        // Filter by member name or username if provided
        if (memberName != null && !memberName.trim().isEmpty()) {
            String searchTerm = memberName.toLowerCase();
            dtos = dtos.stream()
                    .filter(dto -> 
                        dto.getMemberName().toLowerCase().contains(searchTerm) ||
                        dto.getUsername().toLowerCase().contains(searchTerm)
                    )
                    .collect(Collectors.toList());
        }
        
        System.out.println("=== EXITING getAllOrders with " + dtos.size() + " orders ===");
        return dtos;
    }
    
    @Transactional
    public void togglePickup(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setPickedUp(!order.getPickedUp());
        if (order.getPickedUp()) {
            order.setPickedUpAt(LocalDateTime.now());
        } else {
            order.setPickedUpAt(null);
        }
        
        orderRepository.save(order);
    }
    
    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setMenuItemId(order.getMenuItemId());
        dto.setNote(order.getNote());
        dto.setPickedUp(order.getPickedUp());
        
        // Fetch related data
        userRepository.findById(order.getUserId()).ifPresent(user -> {
            dto.setUsername(user.getUsername());
            dto.setMemberName(user.getFullName());
        });
        
        menuItemRepository.findById(order.getMenuItemId()).ifPresent(menuItem -> {
            dto.setMenuItemName(menuItem.getName());
            categoryRepository.findById(menuItem.getCategoryId()).ifPresent(category ->
                dto.setCategoryName(category.getName())
            );
        });
        
        return dto;
    }
}
