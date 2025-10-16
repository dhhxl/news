package com.news.controller;

import com.news.model.entity.Comment;
import com.news.security.CustomUserDetailsService;
import com.news.service.CommentService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论管理Controller
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    /**
     * 创建评论（需要登录）
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comment> createComment(
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        log.info("Create comment request for news: {}", request.getNewsId());

        Long userId = getUserIdFromAuth(authentication);
        
        Comment comment = commentService.createComment(
                request.getNewsId(),
                userId,
                request.getContent()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * 获取新闻的评论列表（公开）
     */
    @GetMapping("/news/{newsId}")
    public ResponseEntity<List<Comment>> getNewsComments(@PathVariable Long newsId) {
        List<Comment> comments = commentService.getNewsComments(newsId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 删除评论（作者或管理员）
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        log.info("Delete comment request: {}", commentId);

        Long userId = getUserIdFromAuth(authentication);
        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        commentService.deleteComment(commentId, userId, isAdmin);

        Map<String, String> response = new HashMap<>();
        response.put("message", "评论已删除");
        return ResponseEntity.ok(response);
    }

    /**
     * 更新评论状态（仅管理员）
     */
    @PutMapping("/{commentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Comment> updateCommentStatus(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        Comment updated = commentService.updateCommentStatus(commentId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * 统计新闻评论数（公开）
     */
    @GetMapping("/news/{newsId}/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable Long newsId) {
        long count = commentService.countNewsComments(newsId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserDetails) {
            return ((CustomUserDetailsService.CustomUserDetails) authentication.getPrincipal()).getUserId();
        }
        throw new RuntimeException("无法获取用户ID");
    }

    @Data
    static class CommentRequest {
        private Long newsId;
        private String content;
    }
}

