package com.news.model.dto;

import lombok.Data;

/**
 * 用户搜索请求
 */
@Data
public class UserSearchRequest {
    private String keyword;      // 搜索关键词（用户名、邮箱、姓名）
    private String role;         // 角色筛选
    private Boolean isEnabled;   // 状态筛选
    private Integer page = 0;    // 页码
    private Integer size = 10;   // 每页大小
}

