package com.news.controller;

import com.news.security.CustomUserDetailsService;
import com.news.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞管理Controller
 */
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Slf4j
public class LikeController {

    private final LikeService likeService;

    /**
     * 点赞新闻（需要登录）
     */
    @PostMapping("/news/{newsId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> likeNews(
            @PathVariable Long newsId,
            Authentication authentication) {
        log.info("Like news request: {}", newsId);

        Long userId = getUserIdFromAuth(authentication);
        likeService.likeNews(newsId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "点赞成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 取消点赞（需要登录）
     */
    @DeleteMapping("/news/{newsId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> unlikeNews(
            @PathVariable Long newsId,
            Authentication authentication) {
        log.info("Unlike news request: {}", newsId);

        Long userId = getUserIdFromAuth(authentication);
        likeService.unlikeNews(newsId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "已取消点赞");
        return ResponseEntity.ok(response);
    }

    /**
     * 检查用户是否点赞了新闻（需要登录）
     */
    @GetMapping("/news/{newsId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkLikeStatus(
            @PathVariable Long newsId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        boolean liked = likeService.hasUserLiked(newsId, userId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", liked);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取新闻点赞数（公开）
     */
    @GetMapping("/news/{newsId}/count")
    public ResponseEntity<Map<String, Long>> getLikeCount(@PathVariable Long newsId) {
        long count = likeService.countNewsLikes(newsId);

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
}

