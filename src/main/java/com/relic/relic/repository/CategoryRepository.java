package com.relic.relic.repository;

import com.relic.relic.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String categoryName);
    boolean existsByName(String categoryName);
}
