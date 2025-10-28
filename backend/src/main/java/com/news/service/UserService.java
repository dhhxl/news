package com.news.service;

import com.news.exception.ResourceNotFoundException;
import com.news.model.entity.User;
import com.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * 更新个人资料
     */
    @Transactional
    public User updateProfile(Long userId, String fullName, String email, String phone) {
        User user = findById(userId);
        
        // 如果邮箱改变，检查是否已被其他用户使用
        if (email != null && !email.equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("该邮箱已被其他用户使用");
            }
            user.setEmail(email);
        }
        
        if (fullName != null) {
            user.setFullName(fullName);
        }
        
        if (phone != null) {
            user.setPhone(phone);
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Updated profile for user: {}", user.getUsername());
        return updatedUser;
    }

    /**
     * 更新用户头像
     */
    @Transactional
    public User updateAvatar(Long userId, String avatarUrl) {
        User user = findById(userId);
        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);
        log.info("Updated avatar for user: {}", user.getUsername());
        return updatedUser;
    }

    // ==================== 管理员功能 ====================

    /**
     * 获取所有用户（分页）
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 搜索用户（支持用户名、邮箱、姓名搜索）
     */
    public Page<User> searchUsers(String keyword, String role, Boolean isEnabled, Pageable pageable) {
        // 这里可以使用Specification进行复杂查询
        // 简化版本：先返回所有用户，前端处理筛选
        return userRepository.findAll(pageable);
    }

    /**
     * 获取所有用户列表（不分页）
     */
    public List<User> getAllUsersList() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        
        // 不能删除管理员账户
        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("不能删除管理员账户");
        }
        
        userRepository.deleteById(userId);
        log.info("Deleted user: {} (ID: {})", user.getUsername(), userId);
    }

    /**
     * 禁用/启用用户
     */
    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = findById(userId);
        
        // 不能禁用管理员账户
        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("不能禁用管理员账户");
        }
        
        user.setIsEnabled(!user.getIsEnabled());
        User updatedUser = userRepository.save(user);
        log.info("Toggled user status: {} -> {}", user.getUsername(), user.getIsEnabled());
        return updatedUser;
    }

    /**
     * 重置用户密码（管理员功能）
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = findById(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset for user: {}", user.getUsername());
    }

    /**
     * 统计用户数量
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * 按角色统计用户数量
     */
    public long countUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(u -> role.equals(u.getRole()))
                .count();
    }
}

