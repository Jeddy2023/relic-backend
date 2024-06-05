package com.relic.relic.controller;

import com.relic.relic.dto.CategoryDto;
import com.relic.relic.model.Category;
import com.relic.relic.model.Product;
import com.relic.relic.service.CategoryService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<String> createCategory(@RequestBody Category category) {
        String createdCategory = categoryService.createCategory(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategoriesWithOpenBiddingProductCounts() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDto> categoryDTOs = categories.stream()
                .map(this::convertToDTOWithOpenBiddingProducts)
                .collect(Collectors.toList());

        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    private CategoryDto convertToDTOWithOpenBiddingProducts(Category category) {
        List<Product> openBiddingProducts = category.getProducts().stream()
                .filter(product -> !product.isBiddingClosed())
                .collect(Collectors.toList());

        CategoryDto categoryDTO = modelMapper.map(category, CategoryDto.class);
        categoryDTO.setProductCount(openBiddingProducts.size());
        return categoryDTO;
    }


    @PatchMapping("/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable("id") Long categoryId, @RequestBody @Valid Category category) {
        String updatedCategory = categoryService.updateCategoryById(categoryId, category);
        return new ResponseEntity<>(updatedCategory,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable("id") Long categoryId) {
        String deletedCategory = categoryService.deleteCategoryById(categoryId);
        return new ResponseEntity<>(deletedCategory,HttpStatus.OK);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<CategoryDto>> getAllCategoriesAdmin(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false, defaultValue = "Default Sorting") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<CategoryDto> categories = categoryService.getAllCategoriesAdmin(categoryName, sortBy);

        int start = Math.min(page * size, categories.size());
        int end = Math.min((page + 1) * size, categories.size());
        List<CategoryDto> categoryPage = categories.subList(start, end);

        return new ResponseEntity<>(categoryPage, HttpStatus.OK);
    }

    @GetMapping("/top-five-active")
    public ResponseEntity<List<Category>> getTopFiveActiveCategories() {
        List<Category> topFiveActiveCategories = categoryService.getTopFiveActiveCategories();
        return new ResponseEntity<>(topFiveActiveCategories, HttpStatus.OK);
    }

}
