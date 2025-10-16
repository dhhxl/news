package com.news.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String refreshToken;
    private String username;
    private String role;
    private Long userId;

    /**
     * 创建成功响应
     */
    public static LoginResponse of(String token, String refreshToken, 
                                   Long userId, String username, String role) {
        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(userId)
                .username(username)
                .role(role)
                .build();
    }
}

