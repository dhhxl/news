package com.news.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 新闻提交请求DTO（编辑使用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsSubmitRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Size(max = 500, message = "URL长度不能超过500个字符")
    private String originalUrl;

    /**
     * 图片ID列表（关联上传的图片）
     */
    private List<Long> imageIds;

    /**
     * 提交类型：DRAFT-保存草稿, SUBMIT-提交审核
     */
    @NotBlank(message = "提交类型不能为空")
    private String submitType;

    /**
     * 提交说明（可选）
     */
    @Size(max = 500, message = "提交说明长度不能超过500个字符")
    private String submitNote;
}
