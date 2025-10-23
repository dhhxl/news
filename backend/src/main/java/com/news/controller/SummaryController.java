package com.news.controller;

import com.news.model.entity.Summary;
import com.news.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 摘要管理Controller
 */
@RestController
@RequestMapping("/summaries")
@RequiredArgsConstructor
@Slf4j
public class SummaryController {

    private final SummaryService summaryService;

    /**
     * 为指定新闻生成摘要（管理员）
     */
    @PostMapping("/generate/{newsId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Summary> generateSummary(@PathVariable Long newsId) {
        log.info("Generate summary request for news: {}", newsId);
        Summary summary = summaryService.generateSummary(newsId);
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    /**
     * 获取新闻的摘要（公开）
     */
    @GetMapping("/news/{newsId}")
    public ResponseEntity<Summary> getSummaryByNewsId(@PathVariable Long newsId) {
        Summary summary = summaryService.getSummaryByNewsId(newsId);
        return ResponseEntity.ok(summary);
    }

    /**
     * 删除摘要（管理员）
     */
    @DeleteMapping("/news/{newsId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSummary(@PathVariable Long newsId) {
        log.info("Delete summary request for news: {}", newsId);
        summaryService.deleteSummary(newsId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 批量生成摘要（管理员）
     * @param forceRegenerate 是否强制重新生成已有摘要
     */
    @PostMapping("/generate/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generateBatchSummaries(
            @RequestParam(defaultValue = "false") boolean forceRegenerate) {
        log.info("Batch summary generation request, forceRegenerate: {}", forceRegenerate);
        
        summaryService.generateSummariesForAllNews(forceRegenerate);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "started");
        response.put("message", forceRegenerate ? 
                "批量摘要生成已启动（包括已有摘要，异步执行）" : 
                "批量摘要生成已启动（仅生成缺失摘要，异步执行）");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查AI服务状态（公开）
     */
    @GetMapping("/ai-status")
    public ResponseEntity<Map<String, Object>> checkAIStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean available = summaryService.isAIServiceAvailable();
        
        status.put("available", available);
        status.put("message", available ? "ZhipuAI API已配置" : "ZhipuAI API未配置，将使用模拟摘要");
        
        return ResponseEntity.ok(status);
    }
}

