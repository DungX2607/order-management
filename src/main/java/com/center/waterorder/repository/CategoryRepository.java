package com.center.waterorder.repository;

import com.center.waterorder.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByDisplayOrderAsc();
    boolean existsByName(String name);
}
