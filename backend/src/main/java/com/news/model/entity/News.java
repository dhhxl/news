package com.news.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新闻实体
 */
@Entity
@Table(name = "news")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 新闻标题
     */
    @Column(name = "title", nullable = false, unique = true, length = 255)
    private String title;

    /**
     * 新闻内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 来源网站：SINA-新浪, CCTV-央视, NETEASE-网易, MANUAL-手动录入
     */
    @Column(name = "source_website", nullable = false, length = 100)
    private String sourceWebsite;

    /**
     * 原始URL
     */
    @Column(name = "original_url", nullable = false, unique = true, length = 500)
    private String originalUrl;

    /**
     * 新闻配图URL
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 分类ID
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 发布时间
     */
    @Column(name = "publish_time", nullable = false)
    private LocalDateTime publishTime;

    /**
     * 爬取时间
     */
    @Column(name = "crawl_time", nullable = false)
    private LocalDateTime crawlTime;

    /**
     * 创建者ID（手动录入时使用）
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 状态：PUBLISHED-已发布, DRAFT-草稿, ARCHIVED-已归档
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 分类方法：AUTO-自动分类, MANUAL-手动分类
     */
    @Column(name = "classification_method", nullable = false, length = 20)
    private String classificationMethod;

    /**
     * 浏览次数
     */
    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    /**
     * 点赞数
     */
    @Column(name = "like_count")
    private Integer likeCount;

    /**
     * 评论数
     */
    @Column(name = "comment_count")
    private Integer commentCount;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (crawlTime == null) {
            crawlTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "PUBLISHED";
        }
        if (classificationMethod == null) {
            classificationMethod = "AUTO";
        }
        if (viewCount == null) {
            viewCount = 0L;
        }
        if (likeCount == null) {
            likeCount = 0;
        }
        if (commentCount == null) {
            commentCount = 0;
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
}

