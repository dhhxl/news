package com.news.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 根路径控制器
 * 提供API基本信息和健康检查
 */
@RestController
@RequestMapping("/")
public class HomeController {

    @Value("${app.name:News Management System}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    /**
     * 根路径 - 返回API基本信息
     */
    @GetMapping
    public Map<String, Object> home() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", appName);
        info.put("version", appVersion);
        info.put("status", "running");
        info.put("timestamp", LocalDateTime.now());
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("categories", "/categories");
        endpoints.put("news", "/news");
        endpoints.put("auth", "/auth/login");
        endpoints.put("health", "/actuator/health");
        
        info.put("endpoints", endpoints);
        info.put("message", "欢迎使用新闻管理系统 API");
        
        return info;
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        return status;
    }
}

