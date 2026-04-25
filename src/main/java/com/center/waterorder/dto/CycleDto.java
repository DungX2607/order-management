package com.center.waterorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleDto {
    private Long id;
    private String status;
    private Long timeLeft; // seconds
    private LocalDateTime nextCycleTime;
}
