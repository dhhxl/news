package com.news.repository;

import com.news.model.entity.CrawlTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 爬取任务Repository
 */
@Repository
public interface CrawlTaskRepository extends JpaRepository<CrawlTask, Long> {

    /**
     * 根据来源和状态查询
     */
    List<CrawlTask> findByTargetSourceAndStatus(String targetSource, String status);

    /**
     * 根据状态查询
     */
    Page<CrawlTask> findByStatus(String status, Pageable pageable);

    /**
     * 根据来源查询
     */
    Page<CrawlTask> findByTargetSource(String targetSource, Pageable pageable);

    /**
     * 查询最近的任务
     */
    @Query("SELECT t FROM CrawlTask t ORDER BY t.createdAt DESC")
    Page<CrawlTask> findLatestTasks(Pageable pageable);

    /**
     * 查询指定时间范围内的任务
     */
    List<CrawlTask> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计成功任务数量
     */
    long countByStatus(String status);

    /**
     * 获取总成功采集数
     */
    @Query("SELECT SUM(t.successCount) FROM CrawlTask t WHERE t.status = 'SUCCESS'")
    Long getTotalSuccessCount();

    /**
     * 获取总失败数
     */
    @Query("SELECT SUM(t.failCount) FROM CrawlTask t")
    Long getTotalFailCount();
}

