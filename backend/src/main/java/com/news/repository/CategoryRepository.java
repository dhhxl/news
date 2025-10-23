package com.news.repository;

import com.news.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类Repository
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根据名称查找分类
     */
    Optional<Category> findByName(String name);

    /**
     * 检查名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据是否默认查找分类
     */
    List<Category> findByIsDefault(Boolean isDefault);

    /**
     * 查找所有默认分类
     */
    @Query("SELECT c FROM Category c WHERE c.isDefault = true ORDER BY c.createdAt ASC")
    List<Category> findAllDefaultCategories();

    /**
     * 查找所有自定义分类
     */
    @Query("SELECT c FROM Category c WHERE c.isDefault = false ORDER BY c.createdAt DESC")
    List<Category> findAllCustomCategories();

    /**
     * 根据创建者查找分类
     */
    List<Category> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
}

