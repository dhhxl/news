package com.news.controller;

import com.news.model.entity.AuditLog;
import com.news.model.entity.User;
import com.news.service.AuditLogService;
import com.news.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理员控制器
 * 提供用户管理等管理员专用功能
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    /**
     * 创建新用户（管理员功能）
     */
    @PostMapping("/users")
    public ResponseEntity<UserInfo> createUser(
            @RequestBody CreateUserRequest request,
            org.springframework.security.core.Authentication authentication) {
        log.info("Admin creating user: {}", request.username());
        
        try {
            User user = userService.createUser(
                request.username(),
                request.password(),
                request.email(),
                request.role()
            );
            
            // 记录审计日志
            Long adminId = getUserIdFromAuth(authentication);
            User admin = userService.findById(adminId);
            auditLogService.log(
                AuditLogService.OperationType.CREATE,
                AuditLogService.TargetEntity.USER,
                user.getId(),
                adminId,
                admin.getUsername(),
                "创建用户: " + user.getUsername() + ", 角色: " + user.getRole()
            );
            
            UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getLastLoginAt()
            );
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("Failed to create user: {}", request.username(), e);
            throw e;
        }
    }
    
    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserIdFromAuth(org.springframework.security.core.Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.news.security.CustomUserDetailsService.CustomUserDetails) {
            return ((com.news.security.CustomUserDetailsService.CustomUserDetails) principal).getUserId();
        }
        throw new RuntimeException("无法获取用户ID");
    }

    /**
     * 用户信息DTO
     */
    public record UserInfo(
        Long id,
        String username,
        String email,
        String role,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime lastLoginAt
    ) {}

    /**
     * 创建用户请求DTO
     */
    public record CreateUserRequest(
        @jakarta.validation.constraints.NotBlank String username,
        @jakarta.validation.constraints.NotBlank String password,
        @jakarta.validation.constraints.Email String email,
        @jakarta.validation.constraints.Pattern(regexp = "^(USER|ADMIN|EDITOR)$", message = "角色必须是USER、ADMIN或EDITOR") String role
    ) {}

    // ==================== 审计日志相关API ====================

    /**
     * 获取审计日志列表
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String targetEntity,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs;

        if (startTime != null && endTime != null) {
            // 按时间范围查询
            logs = auditLogService.getLogsByTimeRange(startTime, endTime, pageable);
        } else if (operationType != null && !operationType.isEmpty()) {
            // 按操作类型查询
            logs = auditLogService.getLogsByOperationType(operationType, pageable);
        } else if (operatorId != null) {
            // 按操作者查询
            logs = auditLogService.getLogsByOperator(operatorId, pageable);
        } else {
            // 查询所有
            logs = auditLogService.getAllLogs(pageable);
        }

        return ResponseEntity.ok(logs);
    }

    /**
     * 获取指定实体的审计日志
     */
    @GetMapping("/audit-logs/{targetEntity}/{targetEntityId}")
    public ResponseEntity<Page<AuditLog>> getEntityAuditLogs(
            @PathVariable String targetEntity,
            @PathVariable Long targetEntityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogService.getLogsByTargetEntity(targetEntity, targetEntityId, pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 统计操作者的操作次数
     */
    @GetMapping("/audit-logs/stats/operator/{operatorId}")
    public ResponseEntity<Map<String, Long>> getOperatorStats(@PathVariable Long operatorId) {
        long count = auditLogService.countByOperator(operatorId);
        return ResponseEntity.ok(Map.of("operatorId", operatorId, "operationCount", count));
    }

    /**
     * 清理旧的审计日志
     */
    @DeleteMapping("/audit-logs/cleanup")
    public ResponseEntity<Map<String, String>> cleanupOldLogs(
            @RequestParam(defaultValue = "90") int daysToKeep
    ) {
        auditLogService.cleanupOldLogs(daysToKeep);
        return ResponseEntity.ok(Map.of("message", "清理完成，保留了最近 " + daysToKeep + " 天的日志"));
    }
}
