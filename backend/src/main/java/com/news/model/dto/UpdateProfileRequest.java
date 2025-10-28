package com.news.model.dto;

import lombok.Data;

/**
 * 更新个人资料请求
 */
@Data
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private String phone;
}

