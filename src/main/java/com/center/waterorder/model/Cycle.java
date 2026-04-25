package com.center.waterorder.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cycle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 10)
    private String status = "OPEN";
    
    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @Column(name = "scheduled_close_at", nullable = false)
    private LocalDateTime scheduledCloseAt;
    
    @PrePersist
    protected void onCreate() {
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
        if (scheduledCloseAt == null) {
            scheduledCloseAt = openedAt.plusHours(4);
        }
    }
}
