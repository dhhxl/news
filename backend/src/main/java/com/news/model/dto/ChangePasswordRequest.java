package com.news.model.dto;

import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}

