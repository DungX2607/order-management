package com.center.waterorder.repository;

import com.center.waterorder.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUserIdAndCycleId(Long userId, Long cycleId);
    List<Order> findByCycleId(Long cycleId);
    boolean existsByUserIdAndCycleId(Long userId, Long cycleId);
    
    @Query("SELECT o FROM Order o WHERE o.cycleId = :cycleId " +
           "AND (:pickedUp IS NULL OR o.pickedUp = :pickedUp)")
    List<Order> findByCycleIdAndPickedUp(@Param("cycleId") Long cycleId, 
                                          @Param("pickedUp") Boolean pickedUp);
}
