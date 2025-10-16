package com.news.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建新闻请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotBlank(message = "来源网站不能为空")
    private String sourceWebsite;

    @NotBlank(message = "原始URL不能为空")
    @Size(max = 500, message = "URL长度不能超过500个字符")
    private String originalUrl;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private LocalDateTime publishTime;

    private String status; // 可选，默认为PUBLISHED

    private String classificationMethod; // 可选，默认为AUTO
}

