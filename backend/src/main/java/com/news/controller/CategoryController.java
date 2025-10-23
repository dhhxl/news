package com.news.controller;

import com.news.model.entity.Category;
import com.news.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理Controller
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建分类（仅管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        log.info("Create category request: {}", category.getName());
        Category created = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新分类（仅管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category category) {
        log.info("Update category request: {}", id);
        Category updated = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除分类（仅管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.info("Delete category request: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID获取分类（公开）
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * 获取所有分类（公开）
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 获取所有默认分类（公开）
     */
    @GetMapping("/default")
    public ResponseEntity<List<Category>> getDefaultCategories() {
        List<Category> categories = categoryService.getDefaultCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 获取所有自定义分类（公开）
     */
    @GetMapping("/custom")
    public ResponseEntity<List<Category>> getCustomCategories() {
        List<Category> categories = categoryService.getCustomCategories();
        return ResponseEntity.ok(categories);
    }
}

