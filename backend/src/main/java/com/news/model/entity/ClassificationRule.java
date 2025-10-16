package com.news.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类规则实体
 */
@Entity
@Table(name = "classification_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassificationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则类型：SOURCE-来源规则, KEYWORD-关键词规则
     */
    @Column(name = "rule_type", nullable = false, length = 20)
    private String ruleType;

    /**
     * 来源匹配模式（用于SOURCE类型规则）
     */
    @Column(name = "source_pattern", length = 255)
    private String sourcePattern;

    /**
     * 关键词列表（用于KEYWORD类型规则，逗号分隔）
     */
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    /**
     * 目标分类ID
     */
    @Column(name = "target_category_id", nullable = false)
    private Long targetCategoryId;

    /**
     * 创建者ID
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Column(name = "priority", nullable = false)
    private Integer priority;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isEnabled == null) {
            isEnabled = true;
        }
        if (priority == null) {
            priority = 100;
        }
        if (createdBy == null) {
            createdBy = 1L; // 默认为系统用户
        }
    }
}

