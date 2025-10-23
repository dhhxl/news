package com.news.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新闻审核请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsReviewRequest {

    @NotNull(message = "新闻ID不能为空")
    private Long newsId;

    @NotBlank(message = "审核动作不能为空")
    private String action; // APPROVE, REJECT, REQUEST_CHANGES

    @Size(max = 1000, message = "审核意见长度不能超过1000个字符")
    private String reviewComment;

    /**
     * 审核截止时间（可选，仅在分配审核时使用）
     */
    private String reviewDeadline;
}
