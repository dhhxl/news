package com.news.service;

import com.news.exception.BusinessException;
import com.news.exception.ResourceNotFoundException;
import com.news.model.entity.Category;
import com.news.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 分类服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 创建分类
     */
    @Transactional
    public Category createCategory(Category category) {
        log.info("Creating category: {}", category.getName());

        // 检查名称是否已存在
        if (categoryRepository.existsByName(category.getName())) {
            throw new BusinessException("分类名称已存在: " + category.getName());
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", saved.getId());
        return saved;
    }

    /**
     * 更新分类
     */
    @Transactional
    public Category updateCategory(Long id, Category category) {
        log.info("Updating category with ID: {}", id);

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在，ID: " + id));

        // 如果修改了名称，检查新名称是否已存在
        if (!existing.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(category.getName())) {
                throw new BusinessException("分类名称已存在: " + category.getName());
            }
            existing.setName(category.getName());
        }

        existing.setDescription(category.getDescription());

        Category updated = categoryRepository.save(existing);
        log.info("Category updated successfully: {}", id);
        return updated;
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("分类不存在，ID: " + id);
        }

        // 注意：实际应用中应检查该分类下是否有新闻，有则不允许删除
        categoryRepository.deleteById(id);
        log.info("Category deleted successfully: {}", id);
    }

    /**
     * 根据ID获取分类
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在，ID: " + id));
    }

    /**
     * 根据名称获取分类
     */
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("分类不存在，名称: " + name));
    }

    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * 获取所有默认分类
     */
    public List<Category> getDefaultCategories() {
        return categoryRepository.findAllDefaultCategories();
    }

    /**
     * 获取所有自定义分类
     */
    public List<Category> getCustomCategories() {
        return categoryRepository.findAllCustomCategories();
    }

    /**
     * 根据创建者获取分类
     */
    public List<Category> getCategoriesByCreator(Long creatorId) {
        return categoryRepository.findByCreatedByOrderByCreatedAtDesc(creatorId);
    }
}

