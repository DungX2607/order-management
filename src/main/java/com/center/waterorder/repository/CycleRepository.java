package com.center.waterorder.repository;

import com.center.waterorder.model.Cycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, Long> {
    
    @Query("SELECT c FROM Cycle c WHERE c.status = :status ORDER BY c.openedAt DESC")
    Optional<Cycle> findFirstByStatusOrderByOpenedAtDesc(@Param("status") String status);
    
    List<Cycle> findByStatusAndScheduledCloseAtBefore(String status, LocalDateTime time);
}
