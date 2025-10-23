package com.news.model.dto;

import com.news.model.entity.News;
import com.news.model.entity.NewsReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻审核响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsReviewResponse {

    private Long newsId;
    private String title;
    private String content;
    private String status;
    private Long categoryId;
    private String categoryName;
    
    // 提交信息
    private LocalDateTime submittedAt;
    private Long submittedBy;
    private String submittedByUsername;
    
    // 审核信息
    private Long currentReviewer;
    private String currentReviewerUsername;
    private LocalDateTime reviewDeadline;
    private Boolean isOverdue;
    
    // 图片信息
    private String imageUrl;
    private List<String> imageUrls;
    private Integer imageCount;
    
    // 审核历史
    private List<ReviewHistoryItem> reviewHistory;
    
    // 统计信息
    private Long viewCount;
    private Integer likeCount;
    private Integer commentCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 审核历史项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewHistoryItem {
        private String action;
        private String status;
        private String reviewComment;
        private LocalDateTime reviewedAt;
        private String reviewerUsername;
    }

    /**
     * 从News实体创建响应DTO
     */
    public static NewsReviewResponse fromNews(News news) {
        return NewsReviewResponse.builder()
                .newsId(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .status(news.getStatus())
                .categoryId(news.getCategoryId())
                .submittedAt(news.getSubmittedAt())
                .submittedBy(news.getSubmittedBy())
                .currentReviewer(news.getCurrentReviewer())
                .reviewDeadline(news.getReviewDeadline())
                .isOverdue(news.isReviewOverdue())
                .imageUrl(news.getImageUrl())
                .imageUrls(news.getImageUrlsForJson())
                .imageCount(news.getImageCount())
                .viewCount(news.getViewCount())
                .likeCount(news.getLikeCount())
                .commentCount(news.getCommentCount())
                .createdAt(news.getCrawlTime())
                .updatedAt(news.getUpdatedAt())
                .build();
    }

    /**
     * 添加审核历史
     */
    public void addReviewHistory(List<NewsReview> reviews) {
        this.reviewHistory = reviews.stream()
                .map(review -> ReviewHistoryItem.builder()
                        .action(review.getAction())
                        .status(review.getStatus())
                        .reviewComment(review.getReviewComment())
                        .reviewedAt(review.getReviewedAt())
                        .build())
                .toList();
    }
}
