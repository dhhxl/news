package com.news.controller;

import com.news.model.entity.CrawlTask;
import com.news.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬虫管理Controller
 */
@RestController
@RequestMapping("/crawler")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // 所有爬虫管理需要管理员权限
public class CrawlerController {

    private final CrawlerService crawlerService;

    /**
     * 手动触发爬虫（指定来源）
     */
    @PostMapping("/trigger/{source}")
    public ResponseEntity<Map<String, String>> triggerCrawler(
            @PathVariable String source,
            @RequestParam(defaultValue = "10") int maxCount) {
        
        log.info("Manual trigger crawler for source: {}, maxCount: {}", source, maxCount);
        
        try {
            crawlerService.executeCrawlTask(source, maxCount);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Crawler task started for " + source);
            response.put("source", source);
            response.put("maxCount", String.valueOf(maxCount));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to trigger crawler: {}", e.getMessage());
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 触发所有爬虫
     */
    @PostMapping("/trigger/all")
    public ResponseEntity<Map<String, String>> triggerAllCrawlers(
            @RequestParam(defaultValue = "10") int maxCount) {
        
        log.info("Manual trigger all crawlers, maxCount: {}", maxCount);
        
        crawlerService.executeAllCrawlers(maxCount);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "All crawler tasks started");
        response.put("maxCount", String.valueOf(maxCount));
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有可用的爬虫来源
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getAvailableSources() {
        List<String> sources = crawlerService.getAvailableSources();
        return ResponseEntity.ok(sources);
    }

    /**
     * 测试所有爬虫连接
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Boolean>> testCrawlers() {
        log.info("Testing all crawlers...");
        Map<String, Boolean> results = crawlerService.testAllCrawlers();
        return ResponseEntity.ok(results);
    }

    /**
     * 获取爬取任务历史
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<CrawlTask>> getTaskHistory(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<CrawlTask> tasks = crawlerService.getTaskHistory(limit);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取爬虫状态统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCrawlerStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取最近的任务
        List<CrawlTask> recentTasks = crawlerService.getTaskHistory(10);
        
        int totalSuccess = 0;
        int totalFail = 0;
        
        for (CrawlTask task : recentTasks) {
            totalSuccess += task.getSuccessCount();
            totalFail += task.getFailCount();
        }
        
        stats.put("recentTasksCount", recentTasks.size());
        stats.put("totalSuccessCrawled", totalSuccess);
        stats.put("totalFailedCrawled", totalFail);
        stats.put("availableSources", crawlerService.getAvailableSources());
        
        return ResponseEntity.ok(stats);
    }
}

