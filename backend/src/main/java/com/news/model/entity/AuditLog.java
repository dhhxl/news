package com.news.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作类型：CREATE, UPDATE, DELETE, VIEW
     */
    @Column(name = "operation_type", nullable = false, length = 20)
    private String operationType;

    /**
     * 目标实体：NEWS, CATEGORY, USER等
     */
    @Column(name = "target_entity", nullable = false, length = 50)
    private String targetEntity;

    /**
     * 目标实体ID
     */
    @Column(name = "target_entity_id", nullable = false)
    private Long targetEntityId;

    /**
     * 操作者ID
     */
    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    /**
     * 操作者用户名
     */
    @Column(name = "operator_username", nullable = false, length = 50)
    private String operatorUsername;

    /**
     * 操作详情（JSON格式）
     */
    @Column(name = "operation_details", columnDefinition = "TEXT")
    private String operationDetails;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * User-Agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 操作时间
     */
    @Column(name = "operation_time", nullable = false, updatable = false)
    private LocalDateTime operationTime;

    @PrePersist
    protected void onCreate() {
        if (operationTime == null) {
            operationTime = LocalDateTime.now();
        }
    }
}

