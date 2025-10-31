package com.news.controller;

import com.news.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取综合统计数据
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOverviewStatistics() {
        log.info("Get overview statistics");
        Map<String, Object> stats = statisticsService.getOverviewStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取分类分布统计
     */
    @GetMapping("/category-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCategoryDistribution() {
        log.info("Get category distribution");
        Map<String, Object> distribution = statisticsService.getCategoryDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * 获取新闻来源分布统计
     */
    @GetMapping("/source-distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSourceDistribution() {
        log.info("Get source distribution");
        Map<String, Object> distribution = statisticsService.getSourceDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * 获取新闻趋势数据（最近7天）
     */
    @GetMapping("/news-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNewsTrend() {
        log.info("Get news trend");
        Map<String, Object> trend = statisticsService.getNewsTrend();
        return ResponseEntity.ok(trend);
    }

    /**
     * 获取浏览量趋势数据
     */
    @GetMapping("/view-trend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getViewTrend() {
        log.info("Get view trend");
        Map<String, Object> trend = statisticsService.getViewTrend();
        return ResponseEntity.ok(trend);
    }
}

