package com.news.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 爬取任务实体
 */
@Entity
@Table(name = "crawl_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 目标来源：SINA, CCTV, NETEASE
     */
    @Column(name = "target_source", nullable = false, length = 50)
    private String targetSource;

    /**
     * 任务状态：PENDING-等待中, RUNNING-运行中, SUCCESS-成功, FAILED-失败
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 成功采集数量
     */
    @Column(name = "success_count", nullable = false)
    private Integer successCount;

    /**
     * 失败数量
     */
    @Column(name = "fail_count", nullable = false)
    private Integer failCount;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (successCount == null) {
            successCount = 0;
        }
        if (failCount == null) {
            failCount = 0;
        }
    }

    /**
     * 标记任务开始
     */
    public void markAsRunning() {
        this.status = "RUNNING";
        this.startTime = LocalDateTime.now();
    }

    /**
     * 标记任务成功
     */
    public void markAsSuccess() {
        this.status = "SUCCESS";
        this.endTime = LocalDateTime.now();
    }

    /**
     * 标记任务失败
     */
    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    /**
     * 增加成功计数
     */
    public void incrementSuccess() {
        this.successCount++;
    }

    /**
     * 增加失败计数
     */
    public void incrementFail() {
        this.failCount++;
    }
}

