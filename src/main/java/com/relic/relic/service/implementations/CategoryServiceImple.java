package com.relic.relic.service.implementations;

import com.relic.relic.exception.AlreadyExistsException;
import com.relic.relic.exception.CustomException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.Category;
import com.relic.relic.repository.CategoryRepository;
import com.relic.relic.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImple implements CategoryService {
    private CategoryRepository categoryRepository;

    public CategoryServiceImple(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public String createCategory(Category category) {
        if (category.getName() == null) {
            throw new CustomException("All fields are required");
        }

        if (categoryRepository.existsByName(category.getName())) {
            throw new AlreadyExistsException("Category already exists");
        }

        categoryRepository.save(category);
        return "Category Created Succesfully";
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();
        if (allCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return allCategories;
    }

    @Override
    public String updateCategoryById(Long categoryId, Category category) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            throw new ResourceNotFoundException("Category not found");
        }
        if (category.getName() != null && category.getName().isEmpty()) {
            throw new CustomException("Category name cannot be empty");
        }
        if (category.getName() != null && categoryRepository.existsByName(category.getName())) {
            throw new AlreadyExistsException("Category already exists");
        }
        Category updatedCategory = optionalCategory.get();
        updatedCategory.setName(category.getName());
        categoryRepository.save(updatedCategory);
        return "Category Updated succesfully";
    }

    @Override
    public String deleteCategoryById(Long categoryId) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.delete(optionalCategory.get());
        return "Category Deleted Succesfully";
    }
}
