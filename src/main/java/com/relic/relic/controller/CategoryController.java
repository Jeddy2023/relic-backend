package com.relic.relic.controller;

import com.relic.relic.model.Category;
import com.relic.relic.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

//    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<String> createCategory(@RequestBody Category category) {
        String createdCategory = categoryService.createCategory(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getCategories() {
        return new ResponseEntity<>(categoryService.getAllCategories(), HttpStatus.OK);
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

}
