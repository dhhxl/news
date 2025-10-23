package com.news.controller;

import com.news.model.dto.NewsReviewRequest;
import com.news.model.dto.NewsReviewResponse;
import com.news.model.entity.NewsReview;
import com.news.service.NewsReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 新闻审核Controller
 */
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@Slf4j
public class NewsReviewController {

    private final NewsReviewService newsReviewService;

    /**
     * 提交新闻审核（编辑使用）
     */
    @PostMapping("/submit/{newsId}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<NewsReviewResponse> submitForReview(
            @PathVariable Long newsId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        NewsReviewResponse response = newsReviewService.submitForReview(newsId, userId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 分配审核人（管理员使用）
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> assignReviewer(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Long newsId = Long.valueOf(request.get("newsId").toString());
        Long reviewerId = Long.valueOf(request.get("reviewerId").toString());
        Long adminId = getUserIdFromAuth(authentication);
        
        newsReviewService.assignReviewer(newsId, reviewerId, adminId);
        
        return ResponseEntity.ok(Map.of("message", "审核人分配成功"));
    }

    /**
     * 审核新闻（管理员使用）
     */
    @PostMapping("/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NewsReviewResponse> reviewNews(
            @Valid @RequestBody NewsReviewRequest request,
            Authentication authentication) {
        
        Long reviewerId = getUserIdFromAuth(authentication);
        NewsReviewResponse response = newsReviewService.reviewNews(request, reviewerId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取待审核新闻列表（管理员使用）
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NewsReviewResponse>> getPendingReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsReviewResponse> reviews = newsReviewService.getPendingReviews(pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * 获取正在审核的新闻列表（管理员使用）
     */
    @GetMapping("/reviewing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NewsReviewResponse>> getReviewingNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long reviewerId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsReviewResponse> reviews = newsReviewService.getReviewingNews(reviewerId, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * 获取所有需要审核的新闻（管理员视图）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NewsReviewResponse>> getAllReviewableNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsReviewResponse> reviews = newsReviewService.getAllReviewableNews(pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * 获取用户提交的新闻列表（编辑使用）
     */
    @GetMapping("/my-submissions")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<NewsReviewResponse>> getMySubmissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<NewsReviewResponse> reviews = newsReviewService.getUserSubmittedNews(userId, status, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * 获取新闻审核历史
     */
    @GetMapping("/{newsId}/history")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<List<NewsReview>> getReviewHistory(@PathVariable Long newsId) {
        List<NewsReview> history = newsReviewService.getNewsReviewHistory(newsId);
        return ResponseEntity.ok(history);
    }

    /**
     * 获取审核统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getReviewStats() {
        Map<String, Long> stats = newsReviewService.getReviewStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 处理超时审核（系统任务）
     */
    @PostMapping("/handle-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> handleOverdueReviews() {
        newsReviewService.handleOverdueReviews();
        return ResponseEntity.ok(Map.of("message", "超时审核处理完成"));
    }

    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.news.security.CustomUserDetailsService.CustomUserDetails) {
            return ((com.news.security.CustomUserDetailsService.CustomUserDetails) principal).getUserId();
        }
        throw new RuntimeException("无法获取用户ID");
    }
}
