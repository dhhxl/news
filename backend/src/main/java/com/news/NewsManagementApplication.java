package com.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 新闻管理系统主应用类
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableRetry
public class NewsManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsManagementApplication.class, args);
    }
}

