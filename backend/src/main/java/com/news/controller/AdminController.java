package com.news.controller;

import com.news.model.entity.User;
import com.news.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 * 提供用户管理等管理员专用功能
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    /**
     * 创建新用户（管理员功能）
     */
    @PostMapping("/users")
    public ResponseEntity<UserInfo> createUser(@RequestBody CreateUserRequest request) {
        log.info("Admin creating user: {}", request.username());
        
        try {
            User user = userService.createUser(
                request.username(),
                request.password(),
                request.email(),
                request.role()
            );
            
            UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getLastLoginAt()
            );
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("Failed to create user: {}", request.username(), e);
            throw e;
        }
    }

    /**
     * 用户信息DTO
     */
    public record UserInfo(
        Long id,
        String username,
        String email,
        String role,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime lastLoginAt
    ) {}

    /**
     * 创建用户请求DTO
     */
    public record CreateUserRequest(
        @jakarta.validation.constraints.NotBlank String username,
        @jakarta.validation.constraints.NotBlank String password,
        @jakarta.validation.constraints.Email String email,
        @jakarta.validation.constraints.Pattern(regexp = "^(USER|ADMIN)$", message = "角色必须是USER或ADMIN") String role
    ) {}
}
