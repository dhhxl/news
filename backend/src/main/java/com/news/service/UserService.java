package com.news.service;

import com.news.exception.ResourceNotFoundException;
import com.news.model.entity.User;
import com.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务
 * 处理用户相关业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "username", username));
    }

    /**
     * 根据ID查找用户
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", id));
    }

    /**
     * 创建新用户（注册用户，默认USER角色）
     */
    @Transactional
    public User createUser(String username, String password, String email) {
        return createUser(username, password, email, "USER");
    }

    /**
     * 创建新用户（可指定角色）
     */
    @Transactional
    public User createUser(String username, String password, String email, String role) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }

        // 创建用户
        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .email(email)
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created new user: {} with role: {}", username, role);
        return savedUser;
    }

    /**
     * 更新最后登录时间
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.updateLastLogin();
        userRepository.save(user);
        log.debug("Updated last login time for user: {}", user.getUsername());
    }

    /**
     * 验证密码
     */
    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, 
                               String newPassword) {
        User user = findById(userId);
        
        // 验证旧密码
        if (!validatePassword(user, oldPassword)) {
            throw new IllegalArgumentException("旧密码错误");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}

