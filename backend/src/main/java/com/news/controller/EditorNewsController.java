package com.news.controller;

import com.news.model.dto.NewsSubmitRequest;
import com.news.model.entity.News;
import com.news.model.entity.UploadedImage;
import com.news.service.EditorNewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 编辑新闻Controller
 */
@RestController
@RequestMapping("/api/editor/news")
@RequiredArgsConstructor
@Slf4j
public class EditorNewsController {

    private final EditorNewsService editorNewsService;
    private final com.news.service.AuditLogService auditLogService;
    private final com.news.service.UserService userService;

    /**
     * 创建新闻草稿
     */
    @PostMapping("/draft")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<News> createDraft(
            @Valid @RequestBody NewsSubmitRequest request,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        com.news.model.entity.User editor = userService.findById(editorId);
        News news = editorNewsService.createDraft(request, editorId);
        
        // 记录审计日志
        auditLogService.log(
            com.news.service.AuditLogService.OperationType.CREATE,
            com.news.service.AuditLogService.TargetEntity.NEWS,
            news.getId(),
            editorId,
            editor.getUsername(),
            "创建新闻草稿: " + news.getTitle()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(news);
    }

    /**
     * 提交新闻
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<News> submitNews(
            @Valid @RequestBody NewsSubmitRequest request,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        com.news.model.entity.User editor = userService.findById(editorId);
        News news = editorNewsService.submitNews(request, editorId);
        
        // 记录审计日志
        auditLogService.log(
            com.news.service.AuditLogService.OperationType.PUBLISH,
            com.news.service.AuditLogService.TargetEntity.NEWS,
            news.getId(),
            editorId,
            editor.getUsername(),
            "提交新闻: " + news.getTitle()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(news);
    }

    /**
     * 更新新闻
     */
    @PutMapping("/{newsId}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<News> updateNews(
            @PathVariable Long newsId,
            @Valid @RequestBody NewsSubmitRequest request,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        com.news.model.entity.User editor = userService.findById(editorId);
        News news = editorNewsService.updateNews(newsId, request, editorId);
        
        // 记录审计日志
        auditLogService.log(
            com.news.service.AuditLogService.OperationType.UPDATE,
            com.news.service.AuditLogService.TargetEntity.NEWS,
            news.getId(),
            editorId,
            editor.getUsername(),
            "更新新闻: " + news.getTitle()
        );
        
        return ResponseEntity.ok(news);
    }

    /**
     * 重新提交被退回的新闻
     */
    @PostMapping("/{newsId}/resubmit")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<News> resubmitNews(
            @PathVariable Long newsId,
            @Valid @RequestBody NewsSubmitRequest request,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        com.news.model.entity.User editor = userService.findById(editorId);
        News news = editorNewsService.resubmitNews(newsId, request, editorId);
        
        // 记录审计日志
        auditLogService.log(
            com.news.service.AuditLogService.OperationType.UPDATE,
            com.news.service.AuditLogService.TargetEntity.NEWS,
            news.getId(),
            editorId,
            editor.getUsername(),
            "重新提交新闻: " + news.getTitle()
        );
        
        return ResponseEntity.ok(news);
    }

    /**
     * 取消审核
     */
    @PostMapping("/{newsId}/cancel-review")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cancelReview(
            @PathVariable Long newsId,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        com.news.model.entity.User editor = userService.findById(editorId);
        editorNewsService.cancelReview(newsId, editorId);
        
        // 记录审计日志
        auditLogService.log(
            com.news.service.AuditLogService.OperationType.UPDATE,
            com.news.service.AuditLogService.TargetEntity.NEWS,
            newsId,
            editorId,
            editor.getUsername(),
            "取消审核"
        );
        
        return ResponseEntity.ok(Map.of("message", "审核已取消，新闻已恢复为草稿状态"));
    }

    /**
     * 获取编辑的新闻列表
     */
    @GetMapping("/my-news")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<News>> getMyNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = editorNewsService.getEditorNews(editorId, pageable);
        
        return ResponseEntity.ok(newsPage);
    }

    /**
     * 根据状态获取编辑的新闻
     */
    @GetMapping("/my-news/status/{status}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<News>> getMyNewsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = editorNewsService.getEditorNewsByStatus(editorId, status, pageable);
        
        return ResponseEntity.ok(newsPage);
    }

    /**
     * 获取新闻详情（编辑视图）
     */
    @GetMapping("/{newsId}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<News> getNewsForEdit(
            @PathVariable Long newsId,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        News news = editorNewsService.getNewsForEdit(newsId, editorId);
        
        return ResponseEntity.ok(news);
    }

    /**
     * 获取新闻关联的图片
     */
    @GetMapping("/{newsId}/images")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UploadedImage>> getNewsImages(@PathVariable Long newsId) {
        List<UploadedImage> images = editorNewsService.getNewsImages(newsId);
        return ResponseEntity.ok(images);
    }

    /**
     * 删除草稿
     */
    @DeleteMapping("/{newsId}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDraft(
            @PathVariable Long newsId,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        editorNewsService.deleteDraft(newsId, editorId);
        
        return ResponseEntity.ok(Map.of("message", "草稿删除成功"));
    }

    /**
     * 获取编辑员统计数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getEditorStatistics(
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        log.info("Editor {} requesting statistics", editorId);
        
        Map<String, Object> stats = editorNewsService.getEditorStatistics(editorId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取编辑员最近动态
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<News>> getEditorRecentNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        Long editorId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<News> newsPage = editorNewsService.getEditorRecentNews(editorId, pageable);
        return ResponseEntity.ok(newsPage);
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
