package com.center.waterorder.service;

import com.center.waterorder.dto.CycleDto;
import com.center.waterorder.model.Cycle;
import com.center.waterorder.repository.CycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CycleService {
    
    private final CycleRepository cycleRepository;
    
    @Transactional(readOnly = true)
    public CycleDto getCurrentCycle() {
        Cycle cycle = cycleRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN")
                .orElse(null);
        
        if (cycle != null) {
            return toDtoWithTimeLeft(cycle);
        }
        
        // No open cycle, return closed status with next cycle time
        CycleDto dto = new CycleDto();
        dto.setStatus("CLOSED");
        dto.setTimeLeft(0L);
        dto.setNextCycleTime(calculateNextCycleTime());
        return dto;
    }
    
    @Transactional
    public CycleDto openCycle() {
        // Check if there's already an open cycle
        if (cycleRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN").isPresent()) {
            throw new RuntimeException("There is already an open cycle");
        }
        
        Cycle cycle = new Cycle();
        cycle.setStatus("OPEN");
        cycle.setOpenedAt(LocalDateTime.now());
        cycle.setScheduledCloseAt(LocalDateTime.now().plusHours(4));
        
        Cycle saved = cycleRepository.save(cycle);
        log.info("Cycle opened manually: {}", saved.getId());
        
        return toDtoWithTimeLeft(saved);
    }
    
    @Transactional
    public void closeCycle() {
        Cycle cycle = cycleRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN")
                .orElseThrow(() -> new RuntimeException("No open cycle to close"));
        
        cycle.setStatus("CLOSED");
        cycle.setClosedAt(LocalDateTime.now());
        
        cycleRepository.save(cycle);
        log.info("Cycle closed manually: {}", cycle.getId());
    }
    
    @Transactional
    public void autoCloseCycles() {
        List<Cycle> expiredCycles = cycleRepository.findByStatusAndScheduledCloseAtBefore(
                "OPEN", LocalDateTime.now());
        
        for (Cycle cycle : expiredCycles) {
            cycle.setStatus("CLOSED");
            cycle.setClosedAt(LocalDateTime.now());
            cycleRepository.save(cycle);
            log.info("Cycle auto-closed: {}", cycle.getId());
        }
    }
    
    private CycleDto toDtoWithTimeLeft(Cycle cycle) {
        CycleDto dto = new CycleDto();
        dto.setId(cycle.getId());
        dto.setStatus(cycle.getStatus());
        
        if ("OPEN".equals(cycle.getStatus())) {
            long secondsLeft = Duration.between(LocalDateTime.now(), cycle.getScheduledCloseAt()).getSeconds();
            dto.setTimeLeft(Math.max(0, secondsLeft));
        } else {
            dto.setTimeLeft(0L);
        }
        
        dto.setNextCycleTime(calculateNextCycleTime());
        return dto;
    }
    
    private LocalDateTime calculateNextCycleTime() {
        // Next Thursday at 09:00, every 2 weeks
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextThursday = now.with(java.time.DayOfWeek.THURSDAY).withHour(9).withMinute(0).withSecond(0);
        
        if (nextThursday.isBefore(now)) {
            nextThursday = nextThursday.plusWeeks(1);
        }
        
        // Make it bi-weekly (simplified - in production, track last cycle date)
        return nextThursday;
    }
}
