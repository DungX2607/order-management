package com.center.waterorder.repository;

import com.center.waterorder.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findAllByOrderByDisplayOrderAsc();
    List<MenuItem> findByCategoryId(Long categoryId);
    long countByCategoryId(Long categoryId);
}
