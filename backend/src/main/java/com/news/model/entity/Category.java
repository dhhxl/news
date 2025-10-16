package com.news.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新闻分类实体
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类名称
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 分类描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 是否为默认分类：true-系统默认，false-用户自定义
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    /**
     * 创建者ID
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (isDefault == null) {
            isDefault = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

