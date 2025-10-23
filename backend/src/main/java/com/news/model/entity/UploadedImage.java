package com.news.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 上传图片记录实体
 */
@Entity
@Table(name = "uploaded_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false)
    private String originalName;

    /**
     * 存储文件名
     */
    @Column(name = "stored_name", nullable = false)
    private String storedName;

    /**
     * 文件存储路径
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 文件大小(字节)
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * MIME类型
     */
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    /**
     * 上传用户ID
     */
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    /**
     * 关联的新闻ID(可为空)
     */
    @Column(name = "news_id")
    private Long newsId;

    /**
     * 上传时间
     */
    @Column(name = "upload_time", nullable = false, updatable = false)
    private LocalDateTime uploadTime;

    /**
     * 是否已被使用
     */
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    @PrePersist
    protected void onCreate() {
        if (uploadTime == null) {
            uploadTime = LocalDateTime.now();
        }
        if (isUsed == null) {
            isUsed = false;
        }
    }

    /**
     * 获取完整的访问URL
     */
    public String getAccessUrl() {
        return "/api/images/" + this.storedName;
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension() {
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf("."));
        }
        return "";
    }

    /**
     * 检查是否是图片文件
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * 标记为已使用
     */
    public void markAsUsed(Long newsId) {
        this.isUsed = true;
        this.newsId = newsId;
    }

    /**
     * 获取人类可读的文件大小
     */
    public String getHumanReadableSize() {
        if (fileSize == null) return "0 B";
        
        long bytes = fileSize;
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    // 支持的图片类型常量
    public static class SupportedImageTypes {
        public static final String JPEG = "image/jpeg";
        public static final String PNG = "image/png";
        public static final String GIF = "image/gif";
        public static final String WEBP = "image/webp";
        public static final String BMP = "image/bmp";

        public static final String[] ALL_TYPES = {JPEG, PNG, GIF, WEBP, BMP};

        public static boolean isSupported(String mimeType) {
            if (mimeType == null) return false;
            for (String type : ALL_TYPES) {
                if (type.equals(mimeType)) {
                    return true;
                }
            }
            return false;
        }
    }
}
