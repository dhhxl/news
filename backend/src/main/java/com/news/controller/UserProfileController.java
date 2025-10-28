package com.news.controller;

import com.news.model.dto.*;
import com.news.model.entity.User;
import com.news.service.ImageUploadService;
import com.news.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户个人资料控制器
 */
@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final ImageUploadService imageUploadService;

    /**
     * 获取当前用户个人资料
     */
    @GetMapping
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        UserProfileDTO profile = convertToDTO(user);
        return ResponseEntity.ok(profile);
    }

    /**
     * 更新个人资料
     */
    @PutMapping
    public ResponseEntity<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        User user = userService.findByUsername(userDetails.getUsername());
        
        User updatedUser = userService.updateProfile(
                user.getId(),
                request.getFullName(),
                request.getEmail(),
                request.getPhone()
        );
        
        UserProfileDTO profile = convertToDTO(updatedUser);
        return ResponseEntity.ok(profile);
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        try {
            User user = userService.findByUsername(userDetails.getUsername());
            
            // 上传图片
            var uploadedImage = imageUploadService.uploadImage(file, user.getId());
            
            // 更新用户头像URL
            User updatedUser = userService.updateAvatar(user.getId(), uploadedImage.getAccessUrl());
            
            log.info("Avatar uploaded successfully for user: {}", user.getUsername());
            return ResponseEntity.ok(updatedUser.getAvatarUrl());
        } catch (Exception e) {
            log.error("Failed to upload avatar", e);
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordRequest request) {
        User user = userService.findByUsername(userDetails.getUsername());
        
        // 验证密码长度
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("新密码长度至少为6位");
        }
        
        userService.changePassword(
                user.getId(),
                request.getOldPassword(),
                request.getNewPassword()
        );
        
        log.info("Password changed successfully for user: {}", user.getUsername());
        return ResponseEntity.ok().build();
    }

    /**
     * 转换User为UserProfileDTO
     */
    private UserProfileDTO convertToDTO(User user) {
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

