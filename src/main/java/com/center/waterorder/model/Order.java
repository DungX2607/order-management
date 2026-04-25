package com.center.waterorder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "cycle_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "cycle_id", nullable = false)
    private Long cycleId;
    
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "is_picked_up", nullable = false)
    private Boolean pickedUp = false;
    
    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
