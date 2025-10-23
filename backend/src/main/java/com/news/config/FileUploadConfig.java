package com.news.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 文件上传配置
 */
@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class FileUploadConfig implements WebMvcConfigurer {

    private String path = "uploads";
    private Long maxFileSize = 10485760L; // 10MB
    private Integer maxFilesPerUser = 100;
    private String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"};

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置图片访问路径
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:" + path + "/");
    }

    // Getters and setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public Integer getMaxFilesPerUser() {
        return maxFilesPerUser;
    }

    public void setMaxFilesPerUser(Integer maxFilesPerUser) {
        this.maxFilesPerUser = maxFilesPerUser;
    }

    public String[] getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(String[] allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
}
