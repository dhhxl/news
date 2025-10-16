package com.news.controller;

import com.news.model.dto.LoginRequest;
import com.news.model.dto.LoginResponse;
import com.news.model.entity.User;
import com.news.security.CustomUserDetailsService;
import com.news.security.JwtTokenProvider;
import com.news.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、登出和用户信息查询
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        
        log.info("Register attempt for user: {}", registerRequest.username());

        try {
            // 创建用户
            User user = userService.createUser(
                    registerRequest.username(),
                    registerRequest.password(),
                    registerRequest.email()
            );

            // 自动登录
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            registerRequest.username(),
                            registerRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成Token
            String token = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            // 构建响应
            LoginResponse response = LoginResponse.of(
                    token,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getRole()
            );

            log.info("Registration successful for user: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Registration failed for user: {}", registerRequest.username(), e);
            throw e;
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // 设置认证上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息
            CustomUserDetailsService.CustomUserDetails userDetails = 
                (CustomUserDetailsService.CustomUserDetails) 
                    authentication.getPrincipal();
            User user = userDetails.getUser();

            // 生成Token
            String token = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider
                .generateRefreshToken(user.getUsername());

            // 更新最后登录时间
            userService.updateLastLogin(user.getId());

            // 构建响应
            LoginResponse response = LoginResponse.of(
                    token,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getRole()
            );

            log.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Login failed for user: {}", 
                     loginRequest.getUsername(), e);
            throw e;
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();
        
        if (authentication != null) {
            String username = authentication.getName();
            log.info("User logout: {}", username);
            SecurityContextHolder.clearContext();
        }
        
        return ResponseEntity.ok().build();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext()
            .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        CustomUserDetailsService.CustomUserDetails userDetails = 
            (CustomUserDetailsService.CustomUserDetails) 
                authentication.getPrincipal();
        User user = userDetails.getUser();

        UserInfo userInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        return ResponseEntity.ok(userInfo);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @RequestBody TokenRefreshRequest request) {
        
        String refreshToken = request.refreshToken();
        
        try {
            // 验证刷新Token
            if (!jwtTokenProvider.isValidTokenFormat(refreshToken)) {
                return ResponseEntity.status(401).build();
            }

            String username = jwtTokenProvider.extractUsername(refreshToken);
            
            // 生成新的访问Token
            String newToken = jwtTokenProvider.generateToken(username);
            
            TokenRefreshResponse response = new TokenRefreshResponse(newToken);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * 用户信息DTO
     */
    public record UserInfo(Long id, String username, String role) {}

    /**
     * 注册请求DTO
     */
    public record RegisterRequest(String username, String password, String email) {}

    /**
     * Token刷新请求DTO
     */
    public record TokenRefreshRequest(String refreshToken) {}

    /**
     * Token刷新响应DTO
     */
    public record TokenRefreshResponse(String token) {}
}

