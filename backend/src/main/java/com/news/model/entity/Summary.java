package com.news.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI摘要实体
 */
@Entity
@Table(name = "summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的新闻ID
     */
    @Column(name = "news_id", nullable = false, unique = true)
    private Long newsId;

    /**
     * 摘要内容
     */
    @Column(name = "summary_content", nullable = false, columnDefinition = "TEXT")
    private String summaryContent;

    /**
     * 生成时间
     */
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    /**
     * AI模型版本
     */
    @Column(name = "model_version", length = 50)
    private String modelVersion;

    /**
     * 生成状态：SUCCESS-成功, FAILED-失败, PENDING-处理中
     */
    @Column(name = "status", length = 20)
    private String status;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }
}

