package com.relic.relic.service.implementations;

import com.relic.relic.dto.CategoryDto;
import com.relic.relic.exception.AlreadyExistsException;
import com.relic.relic.exception.CustomException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.Category;
import com.relic.relic.repository.CategoryRepository;
import com.relic.relic.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImple implements CategoryService {
    private CategoryRepository categoryRepository;
    private ModelMapper modelMapper;

    public CategoryServiceImple(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
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
    public List<CategoryDto> getAllCategoriesAdmin(
            String categoryName,
            String sortBy
    ) {
        List<Category> categories = categoryRepository.findAll();

        if (categoryName != null) {
            categories = categories.stream()
                    .filter(category -> category.getName().toLowerCase().contains(categoryName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "Most Active: High to Low":
                categories.sort(Comparator.comparingInt(category -> category.getProducts().size()));
                Collections.reverse(categories);
                break;
            case "Most Active: Low to High":
                categories.sort(Comparator.comparingInt(category -> category.getProducts().size()));
                break;
            case "Alphabetical Order: A-Z":
                categories.sort(Comparator.comparing(Category::getName));
                break;
            case "Alphabetical Order: Z-A":
                categories.sort(Comparator.comparing(Category::getName).reversed());
                break;
            default:
                throw new IllegalArgumentException("Invalid sorting option: " + sortBy);
        }

        return categories.stream()
                .map(category -> {
                    CategoryDto categoryDto = modelMapper.map(category, CategoryDto.class);
                    categoryDto.setProductCount(category.getProducts().size());
                    return categoryDto;
                })
                .toList();
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
        if(optionalCategory.get().getProducts().isEmpty()){
            throw new CustomException("Cannot delete category with associated products");
        }
        categoryRepository.delete(optionalCategory.get());
        return "Category Deleted Succesfully";
    }
    @Override
    public List<Category> getTopFiveActiveCategories() {
        return categoryRepository.findTopFiveActiveCategories();
    }
}
