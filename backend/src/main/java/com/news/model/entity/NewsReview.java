package com.news.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新闻审核记录实体
 */
@Entity
@Table(name = "news_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 新闻ID
     */
    @Column(name = "news_id", nullable = false)
    private Long newsId;

    /**
     * 审核人ID
     */
    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    /**
     * 审核动作：SUBMIT-提交, APPROVE-通过, REJECT-拒绝, REQUEST_CHANGES-要求修改
     */
    @Column(name = "action", nullable = false, length = 20)
    private String action;

    /**
     * 审核后状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 审核意见
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    /**
     * 审核时间
     */
    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reviewedAt == null) {
            reviewedAt = LocalDateTime.now();
        }
    }

    // 审核动作常量
    public static class ReviewAction {
        public static final String SUBMIT = "SUBMIT";           // 提交
        public static final String APPROVE = "APPROVE";         // 通过
        public static final String REJECT = "REJECT";           // 拒绝
        public static final String REQUEST_CHANGES = "REQUEST_CHANGES"; // 要求修改
    }

    // 审核状态常量
    public static class ReviewStatus {
        public static final String PENDING = "PENDING";         // 待审核
        public static final String APPROVED = "APPROVED";       // 已通过
        public static final String REJECTED = "REJECTED";       // 已拒绝
    }

    /**
     * 创建提交审核记录
     */
    public static NewsReview createSubmitRecord(Long newsId, Long submitterId) {
        return NewsReview.builder()
                .newsId(newsId)
                .reviewerId(submitterId)
                .action(ReviewAction.SUBMIT)
                .status(ReviewStatus.PENDING)
                .reviewComment("提交审核")
                .build();
    }

    /**
     * 创建审核通过记录
     */
    public static NewsReview createApproveRecord(Long newsId, Long reviewerId, String comment) {
        return NewsReview.builder()
                .newsId(newsId)
                .reviewerId(reviewerId)
                .action(ReviewAction.APPROVE)
                .status(ReviewStatus.APPROVED)
                .reviewComment(comment)
                .build();
    }

    /**
     * 创建审核拒绝记录
     */
    public static NewsReview createRejectRecord(Long newsId, Long reviewerId, String comment) {
        return NewsReview.builder()
                .newsId(newsId)
                .reviewerId(reviewerId)
                .action(ReviewAction.REJECT)
                .status(ReviewStatus.REJECTED)
                .reviewComment(comment)
                .build();
    }
}
