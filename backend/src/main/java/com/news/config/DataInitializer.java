package com.news.config;

import com.news.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 系统启动时创建默认管理员账户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }

    /**
     * 初始化默认管理员账户
     */
    private void initializeDefaultAdmin() {
        try {
            // 检查是否已存在管理员账户
            if (!userService.existsByUsername("admin")) {
                // 创建默认管理员账户
                userService.createUser(
                    "admin", 
                    "admin123", 
                    "admin@news.com", 
                    "ADMIN"
                );
                log.info("Default admin account created: username=admin, password=admin123");
                log.warn("Please change the default admin password after first login!");
            } else {
                log.info("Admin account already exists, skipping initialization");
            }
        } catch (Exception e) {
            log.error("Failed to initialize default admin account", e);
        }
    }
}
