package com.news.service;

import com.news.model.entity.AuditLog;
import com.news.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计日志服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 记录审计日志（异步）
     */
    @Async
    @Transactional
    public void log(String operationType, String targetEntity, Long targetEntityId,
                    Long operatorId, String operatorUsername, String operationDetails) {
        try {
            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            String ipAddress = getClientIpAddress(request);
            String userAgent = getUserAgent(request);

            AuditLog auditLog = AuditLog.builder()
                    .operationType(operationType)
                    .targetEntity(targetEntity)
                    .targetEntityId(targetEntityId)
                    .operatorId(operatorId)
                    .operatorUsername(operatorUsername)
                    .operationDetails(operationDetails)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log recorded: {} {} by {}", operationType, targetEntity, operatorUsername);
        } catch (Exception e) {
            log.error("Failed to record audit log", e);
        }
    }

    /**
     * 简化的记录方法
     */
    public void log(String operationType, String targetEntity, Long targetEntityId,
                    Long operatorId, String operatorUsername) {
        log(operationType, targetEntity, targetEntityId, operatorId, operatorUsername, null);
    }

    /**
     * 获取所有审计日志
     */
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findLatestLogs(pageable);
    }

    /**
     * 根据目标实体查询
     */
    public Page<AuditLog> getLogsByTargetEntity(String targetEntity, Long targetEntityId, Pageable pageable) {
        return auditLogRepository.findByTargetEntityAndTargetEntityId(targetEntity, targetEntityId, pageable);
    }

    /**
     * 根据操作者查询
     */
    public Page<AuditLog> getLogsByOperator(Long operatorId, Pageable pageable) {
        return auditLogRepository.findByOperatorId(operatorId, pageable);
    }

    /**
     * 根据操作类型查询
     */
    public Page<AuditLog> getLogsByOperationType(String operationType, Pageable pageable) {
        return auditLogRepository.findByOperationType(operationType, pageable);
    }

    /**
     * 根据时间范围查询
     */
    public Page<AuditLog> getLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByTimeRange(startTime, endTime, pageable);
    }

    /**
     * 统计操作者的操作次数
     */
    public long countByOperator(Long operatorId) {
        return auditLogRepository.countByOperatorId(operatorId);
    }

    /**
     * 清理旧日志（保留最近N天的日志）
     */
    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysToKeep);
        auditLogRepository.deleteByOperationTimeBefore(threshold);
        log.info("Cleaned up audit logs older than {} days", daysToKeep);
    }

    /**
     * 获取当前HTTP请求
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "Unknown";
    }

    /**
     * 获取User-Agent
     */
    private String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "Unknown";
        }
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }

    /**
     * 审计日志操作类型常量
     */
    public static class OperationType {
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String VIEW = "VIEW";
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String REVIEW = "REVIEW";
        public static final String PUBLISH = "PUBLISH";
        public static final String ARCHIVE = "ARCHIVE";
    }

    /**
     * 目标实体类型常量
     */
    public static class TargetEntity {
        public static final String NEWS = "NEWS";
        public static final String CATEGORY = "CATEGORY";
        public static final String USER = "USER";
        public static final String COMMENT = "COMMENT";
        public static final String RULE = "CLASSIFICATION_RULE";
        public static final String CRAWLER_TASK = "CRAWLER_TASK";
    }
}

