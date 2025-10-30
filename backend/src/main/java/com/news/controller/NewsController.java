package com.news.controller;

import com.news.model.dto.NewsCreateRequest;
import com.news.model.entity.News;
import com.news.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 新闻管理Controller
 */
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {

    private final NewsService newsService;

    /**
     * 创建新闻（仅管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<News> createNews(@Valid @RequestBody NewsCreateRequest request) {
        log.info("Create news request: {}", request.getTitle());
        
        // 转换DTO为Entity
        News news = News.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .sourceWebsite(request.getSourceWebsite())
                .originalUrl(request.getOriginalUrl())
                .categoryId(request.getCategoryId())
                .publishTime(request.getPublishTime() != null ? request.getPublishTime() : LocalDateTime.now())
                .status(request.getStatus() != null ? request.getStatus() : "PUBLISHED")
                .classificationMethod(request.getClassificationMethod() != null ? request.getClassificationMethod() : "MANUAL")
                .build();
        
        News created = newsService.createNews(news);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新新闻（仅管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<News> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody NewsCreateRequest request) {
        log.info("Update news request: {}", id);
        
        // 转换DTO为Entity
        News news = News.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .categoryId(request.getCategoryId())
                .status(request.getStatus())
                .build();
        
        News updated = newsService.updateNews(id, news);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除新闻（仅管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        log.info("Delete news request: {}", id);
        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID获取新闻（公开，自动增加浏览次数）
     */
    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        News news = newsService.getNewsWithViewIncrement(id);
        return ResponseEntity.ok(news);
    }

    /**
     * 获取新闻列表（支持排序、搜索和筛选）
     */
    @GetMapping
    public ResponseEntity<Page<News>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String status) {
        
        log.info("Get news list - page: {}, size: {}, categoryId: {}, keyword: {}, sortBy: {}, status: {}", 
                 page, size, categoryId, keyword, sortBy, status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage;

        // 如果有搜索关键词
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchStatus = (status != null && !status.isEmpty()) ? status : "PUBLISHED";
            newsPage = newsService.searchNews(keyword, pageable);
            return ResponseEntity.ok(newsPage);
        }

        // 根据排序方式选择查询方法
        if ("time".equalsIgnoreCase(sortBy)) {
            // 按时间排序（最新）
            if (categoryId != null) {
                newsPage = newsService.getLatestNewsByCategory(categoryId, pageable);
            } else {
                newsPage = newsService.getLatestNews(pageable);
            }
        } else if ("hot".equalsIgnoreCase(sortBy)) {
            // 按热度排序
            if (categoryId != null) {
                newsPage = newsService.getHotNewsByCategory(categoryId, pageable);
            } else {
                newsPage = newsService.getHotNews(pageable);
            }
        } else {
            // 默认智能排序
            if (categoryId != null) {
                newsPage = newsService.getNewsByCategoryWithSmartSort(categoryId, pageable);
            } else {
                newsPage = newsService.getPublishedNewsWithSmartSort(pageable);
            }
        }

        return ResponseEntity.ok(newsPage);
    }

    /**
     * 搜索新闻（公开）
     */
    @GetMapping("/search")
    public ResponseEntity<Page<News>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Search news with keyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsService.searchNews(keyword, pageable);
        return ResponseEntity.ok(newsPage);
    }

    /**
     * 获取热门新闻（公开）
     */
    @GetMapping("/hot")
    public ResponseEntity<Page<News>> getHotNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsService.getHotNews(pageable);
        return ResponseEntity.ok(newsPage);
    }

    /**
     * 获取最新新闻（公开）
     */
    @GetMapping("/latest")
    public ResponseEntity<Page<News>> getLatestNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsService.getLatestNews(pageable);
        return ResponseEntity.ok(newsPage);
    }

    /**
     * 根据来源获取新闻（公开）
     */
    @GetMapping("/source/{sourceWebsite}")
    public ResponseEntity<Page<News>> getNewsBySource(
            @PathVariable String sourceWebsite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishTime").descending());
        Page<News> newsPage = newsService.getNewsBySource(sourceWebsite, pageable);
        return ResponseEntity.ok(newsPage);
    }

    /**
     * 根据时间范围获取新闻（公开）
     */
    @GetMapping("/range")
    public ResponseEntity<Page<News>> getNewsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsService.getNewsByTimeRange(startTime, endTime, pageable);
        return ResponseEntity.ok(newsPage);
    }

    /**
     * 发布新闻（仅管理员）
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<News> publishNews(@PathVariable Long id) {
        log.info("Publish news request: {}", id);
        News published = newsService.publishNews(id);
        return ResponseEntity.ok(published);
    }

    /**
     * 归档新闻（仅管理员）
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<News> archiveNews(@PathVariable Long id) {
        log.info("Archive news request: {}", id);
        News archived = newsService.archiveNews(id);
        return ResponseEntity.ok(archived);
    }

    /**
     * 批量删除新闻（仅管理员）
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> batchDeleteNews(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        java.util.List<Object> rawIds = (java.util.List<Object>) request.get("ids");
        
        if (rawIds == null || rawIds.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "请提供要删除的新闻ID列表"));
        }
        
        // 转换为Long类型的ID列表
        java.util.List<Long> ids = rawIds.stream()
                .map(id -> {
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    } else {
                        return Long.parseLong(id.toString());
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        
        log.info("Batch delete news request: {} items", ids.size());
        
        try {
            int deletedCount = newsService.batchDeleteNews(ids);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("deletedCount", deletedCount);
            result.put("message", String.format("成功删除 %d 条新闻", deletedCount));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Batch delete failed", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "批量删除失败: " + e.getMessage()));
        }
    }

    /**
     * 获取统计信息（仅管理员）
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalViewCount", newsService.getTotalViewCount());
        return ResponseEntity.ok(stats);
    }

    /**
     * 管理员获取新闻列表（支持所有状态、排序和搜索）
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<News>> getAdminNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String status) {
        
        log.info("Admin get news list - page: {}, size: {}, categoryId: {}, keyword: {}, sortBy: {}, status: {}", 
                 page, size, categoryId, keyword, sortBy, status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage;

        // 管理员可以查看所有状态的新闻
        newsPage = newsService.getNewsListForAdmin(page, size, categoryId, keyword, sortBy, status);

        return ResponseEntity.ok(newsPage);
    }
}

