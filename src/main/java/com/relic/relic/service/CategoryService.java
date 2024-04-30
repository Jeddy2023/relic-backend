package com.relic.relic.service;

import com.relic.relic.model.Category;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService {
    String createCategory(Category category);
    List<Category> getAllCategories();
    String updateCategoryById(Long categoryId, Category category);
    String deleteCategoryById(Long categoryId);
}
