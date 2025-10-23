package com.news.model.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    ADMIN("ADMIN", "管理员", "系统管理员，具有最高权限"),
    EDITOR("EDITOR", "编辑", "新闻编辑，可以创建和编辑新闻"),
    USER("USER", "普通用户", "普通用户，可以浏览和评论新闻");

    private final String code;
    private final String name;
    private final String description;

    UserRole(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取角色
     */
    public static UserRole fromCode(String code) {
        if (code == null) return null;
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 检查是否是管理员
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * 检查是否是编辑
     */
    public boolean isEditor() {
        return this == EDITOR;
    }

    /**
     * 检查是否是普通用户
     */
    public boolean isUser() {
        return this == USER;
    }

    /**
     * 检查是否有新闻创建权限
     */
    public boolean canCreateNews() {
        return this == ADMIN || this == EDITOR;
    }

    /**
     * 检查是否有新闻审核权限
     */
    public boolean canReviewNews() {
        return this == ADMIN;
    }

    /**
     * 检查是否有新闻发布权限
     */
    public boolean canPublishNews() {
        return this == ADMIN;
    }

    /**
     * 检查是否有用户管理权限
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }
}
