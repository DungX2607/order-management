package com.center.waterorder.controller;

import com.center.waterorder.dto.CreateOrderRequest;
import com.center.waterorder.dto.OrderDto;
import com.center.waterorder.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @GetMapping("/my")
    public ResponseEntity<OrderDto> getMyOrder(Authentication authentication) {
        try {
            return ResponseEntity.ok(orderService.getMyOrder(authentication.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(authentication.getName(), request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(authentication.getName(), id, request));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String memberName) {
        System.out.println("=== CONTROLLER: getAllOrders called ===");
        System.out.println("CONTROLLER: status=" + status + ", memberName=" + memberName);
        try {
            List<OrderDto> result = orderService.getAllOrders(status, memberName);
            System.out.println("CONTROLLER: Success, returning " + result.size() + " orders");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("CONTROLLER: Exception caught: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @PatchMapping("/{id}/pickup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> togglePickup(@PathVariable Long id) {
        orderService.togglePickup(id);
        return ResponseEntity.ok().build();
    }
}
