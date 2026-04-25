package com.center.waterorder.scheduler;

import com.center.waterorder.service.CycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CycleScheduler {
    
    private final CycleService cycleService;
    
    // Auto-open cycle every Thursday at 09:00 (bi-weekly - simplified)
    // Cron: second minute hour day month weekday
    @Scheduled(cron = "0 0 9 * * THU")
    public void autoOpenCycle() {
        try {
            log.info("Auto-opening cycle...");
            cycleService.openCycle();
        } catch (Exception e) {
            log.error("Failed to auto-open cycle", e);
        }
    }
    
    // Check and close expired cycles every minute
    @Scheduled(cron = "0 * * * * *")
    public void autoCloseCycles() {
        try {
            cycleService.autoCloseCycles();
        } catch (Exception e) {
            log.error("Failed to auto-close cycles", e);
        }
    }
}
