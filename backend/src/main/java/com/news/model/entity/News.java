package com.news.model.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 新闻实体
 */
@Slf4j
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
     * 新闻配图URL（主图片，用于向后兼容和列表显示）
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 所有图片URL数组（JSON格式）
     */
    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls;

    /**
     * 分类ID
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * 分类名称（非持久化字段，用于前端显示）
     */
    @Transient
    private String categoryName;

    /**
     * 发布时间（草稿和待审核状态时为占位符时间）
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
     * 状态：PUBLISHED-已发布, DRAFT-草稿, ARCHIVED-已归档, PENDING-待审核, REJECTED-已退回, REVIEWING-审核中
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

    /**
     * 提交审核时间
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /**
     * 提交人ID
     */
    @Column(name = "submitted_by")
    private Long submittedBy;

    /**
     * 当前审核人ID
     */
    @Column(name = "current_reviewer")
    private Long currentReviewer;

    /**
     * 审核截止时间
     */
    @Column(name = "review_deadline")
    private LocalDateTime reviewDeadline;

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

    // 多图片处理的辅助方法
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取图片URL列表
     */
    public List<String> getImageUrlList() {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            List<String> result = new ArrayList<>();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                result.add(imageUrl);
            }
            return result;
        }
        
        try {
            return objectMapper.readValue(imageUrls, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse image URLs JSON: {}", imageUrls, e);
            List<String> result = new ArrayList<>();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                result.add(imageUrl);
            }
            return result;
        }
    }

    /**
     * 设置图片URL列表
     */
    public void setImageUrlList(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            this.imageUrls = null;
            this.imageUrl = null;
            return;
        }
        
        try {
            this.imageUrls = objectMapper.writeValueAsString(urls);
            this.imageUrl = urls.get(0); // 设置第一张图片为主图
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize image URLs: {}", urls, e);
            this.imageUrl = urls.get(0);
            this.imageUrls = null;
        }
    }

    /**
     * 添加图片URL
     */
    public void addImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        
        List<String> urls = getImageUrlList();
        if (!urls.contains(url)) {
            urls.add(url);
            setImageUrlList(urls);
        }
    }

    /**
     * 获取主图片URL（第一张图片）
     */
    public String getPrimaryImageUrl() {
        List<String> urls = getImageUrlList();
        return urls.isEmpty() ? null : urls.get(0);
    }

    /**
     * 获取图片数量
     */
    public int getImageCount() {
        return getImageUrlList().size();
    }

    /**
     * 用于JSON序列化的图片URL数组getter
     * 前端需要的是字符串数组而不是JSON字符串
     */
    @com.fasterxml.jackson.annotation.JsonProperty("imageUrls")
    public java.util.List<String> getImageUrlsForJson() {
        return getImageUrlList();
    }

    // 新闻状态常量
    public static class NewsStatus {
        public static final String PUBLISHED = "PUBLISHED";   // 已发布
        public static final String DRAFT = "DRAFT";           // 草稿
        public static final String ARCHIVED = "ARCHIVED";     // 已归档
        public static final String PENDING = "PENDING";       // 待审核
        public static final String REJECTED = "REJECTED";     // 已退回
        public static final String REVIEWING = "REVIEWING";   // 审核中
    }

    // 审核流程相关方法
    
    /**
     * 提交审核
     */
    public void submitForReview(Long submitterId) {
        this.status = NewsStatus.PENDING;
        this.submittedAt = LocalDateTime.now();
        this.submittedBy = submitterId;
        this.reviewDeadline = LocalDateTime.now().plusDays(3); // 3天审核期限
    }

    /**
     * 分配审核人
     */
    public void assignReviewer(Long reviewerId) {
        this.currentReviewer = reviewerId;
        this.status = NewsStatus.REVIEWING;
    }

    /**
     * 审核通过并发布
     */
    public void approveAndPublish() {
        this.status = NewsStatus.PUBLISHED;
        this.currentReviewer = null;
        this.publishTime = LocalDateTime.now();
    }

    /**
     * 审核退回
     */
    public void reject() {
        this.status = NewsStatus.REJECTED;
        this.currentReviewer = null;
    }

    /**
     * 下架新闻
     */
    public void archive() {
        this.status = NewsStatus.ARCHIVED;
    }

    /**
     * 检查是否可以编辑
     */
    public boolean canEdit() {
        return NewsStatus.DRAFT.equals(this.status) || NewsStatus.REJECTED.equals(this.status);
    }

    /**
     * 检查是否需要审核
     */
    public boolean needsReview() {
        return NewsStatus.PENDING.equals(this.status) || NewsStatus.REVIEWING.equals(this.status);
    }

    /**
     * 检查审核是否超时
     */
    public boolean isReviewOverdue() {
        return reviewDeadline != null && LocalDateTime.now().isAfter(reviewDeadline);
    }
}

