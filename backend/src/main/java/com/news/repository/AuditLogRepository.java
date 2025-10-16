package com.news.repository;

import com.news.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 根据目标实体和实体ID查询
     */
    Page<AuditLog> findByTargetEntityAndTargetEntityId(
            String targetEntity,
            Long targetEntityId,
            Pageable pageable
    );

    /**
     * 根据操作者查询
     */
    Page<AuditLog> findByOperatorId(Long operatorId, Pageable pageable);

    /**
     * 根据操作类型查询
     */
    Page<AuditLog> findByOperationType(String operationType, Pageable pageable);

    /**
     * 查询指定时间范围的日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.operationTime DESC")
    Page<AuditLog> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    /**
     * 查询最近的审计日志
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.operationTime DESC")
    Page<AuditLog> findLatestLogs(Pageable pageable);

    /**
     * 统计操作者的操作次数
     */
    long countByOperatorId(Long operatorId);

    /**
     * 删除指定时间之前的日志（用于日志清理）
     */
    void deleteByOperationTimeBefore(LocalDateTime threshold);
}

