package com.news.controller;

import com.news.model.dto.UserListDTO;
import com.news.model.dto.UserProfileDTO;
import com.news.model.entity.User;
import com.news.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理控制器（管理员专用）
 */
@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserService userService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isEnabled) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userService.getAllUsers(pageable);
        
        // 转换为DTO
        List<UserListDTO> users = userPage.getContent().stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("totalElements", userPage.getTotalElements());
        response.put("totalPages", userPage.getTotalPages());
        response.put("currentPage", userPage.getNumber());
        response.put("pageSize", userPage.getSize());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getUserDetail(@PathVariable Long userId) {
        User user = userService.findById(userId);
        UserProfileDTO dto = convertToProfileDTO(user);
        return ResponseEntity.ok(dto);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            log.info("User deleted by admin: {}", userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Failed to delete user: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/{userId}/toggle-status")
    public ResponseEntity<UserListDTO> toggleUserStatus(@PathVariable Long userId) {
        try {
            User user = userService.toggleUserStatus(userId);
            UserListDTO dto = convertToListDTO(user);
            log.info("User status toggled by admin: {} -> {}", userId, user.getIsEnabled());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.error("Failed to toggle user status: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度至少为6位");
        }
        
        userService.resetPassword(userId, newPassword);
        log.info("Password reset by admin for user: {}", userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        long totalUsers = userService.countUsers();
        long adminCount = userService.countUsersByRole("ADMIN");
        long editorCount = userService.countUsersByRole("EDITOR");
        long userCount = userService.countUsersByRole("USER");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalUsers);
        stats.put("admins", adminCount);
        stats.put("editors", editorCount);
        stats.put("users", userCount);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 转换为列表DTO
     */
    private UserListDTO convertToListDTO(User user) {
        return UserListDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .isEnabled(user.getIsEnabled())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * 转换为详情DTO
     */
    private UserProfileDTO convertToProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .isEnabled(user.getIsEnabled())
                .build();
    }
}

