package com.news.service;

import com.news.exception.BusinessException;
import com.news.model.entity.UploadedImage;
import com.news.repository.UploadedImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 图片上传服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final UploadedImageRepository uploadedImageRepository;

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.upload.max-file-size:10485760}") // 10MB
    private Long maxFileSize;

    @Value("${app.upload.max-files-per-user:100}")
    private Integer maxFilesPerUser;

    /**
     * 上传单张图片
     */
    @Transactional
    public UploadedImage uploadImage(MultipartFile file, Long uploaderId) {
        // 验证文件
        validateImageFile(file);
        
        // 检查用户上传限制
        checkUserUploadLimit(uploaderId);

        try {
            // 生成存储文件名
            String storedName = generateStoredFileName(file.getOriginalFilename());
            
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 保存文件
            Path filePath = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 保存数据库记录
            UploadedImage uploadedImage = UploadedImage.builder()
                    .originalName(file.getOriginalFilename())
                    .storedName(storedName)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .uploadedBy(uploaderId)
                    .build();

            uploadedImage = uploadedImageRepository.save(uploadedImage);
            
            log.info("Image uploaded successfully: {} by user {}", storedName, uploaderId);
            return uploadedImage;
            
        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传图片
     */
    @Transactional
    public List<UploadedImage> uploadImages(List<MultipartFile> files, Long uploaderId) {
        if (files.size() > 10) {
            throw new BusinessException("单次最多只能上传10张图片");
        }

        return files.stream()
                .map(file -> uploadImage(file, uploaderId))
                .toList();
    }

    /**
     * 获取用户上传的图片
     */
    public Page<UploadedImage> getUserImages(Long userId, Pageable pageable) {
        return uploadedImageRepository.findByUploadedByOrderByUploadTimeDesc(userId, pageable);
    }

    /**
     * 获取用户未使用的图片
     */
    public List<UploadedImage> getUserUnusedImages(Long userId) {
        return uploadedImageRepository.findByUploadedByAndIsUsedFalseOrderByUploadTimeDesc(userId);
    }

    /**
     * 根据ID获取图片
     */
    public UploadedImage getImageById(Long imageId) {
        return uploadedImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException("图片不存在"));
    }

    /**
     * 根据存储文件名获取图片
     */
    public UploadedImage getImageByStoredName(String storedName) {
        return uploadedImageRepository.findByStoredName(storedName)
                .orElseThrow(() -> new BusinessException("图片不存在"));
    }

    /**
     * 获取图片文件资源
     */
    public Resource loadImageAsResource(String storedName) {
        try {
            UploadedImage image = getImageByStoredName(storedName);
            Path filePath = Paths.get(image.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("图片文件不存在或不可读");
            }
        } catch (MalformedURLException e) {
            log.error("Failed to load image as resource: {}", e.getMessage(), e);
            throw new BusinessException("图片加载失败");
        }
    }

    /**
     * 将图片关联到新闻
     */
    @Transactional
    public void associateImagesToNews(List<Long> imageIds, Long newsId, Long userId) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }

        List<UploadedImage> images = uploadedImageRepository.findByIdInOrderByUploadTimeAsc(imageIds);
        
        for (UploadedImage image : images) {
            // 检查权限
            if (!image.getUploadedBy().equals(userId)) {
                throw new BusinessException("无权使用该图片: " + image.getOriginalName());
            }
            
            // 标记为已使用
            image.markAsUsed(newsId);
        }
        
        uploadedImageRepository.saveAll(images);
        log.info("Associated {} images to news {}", images.size(), newsId);
    }

    /**
     * 删除图片
     */
    @Transactional
    public void deleteImage(Long imageId, Long userId) {
        UploadedImage image = getImageById(imageId);
        
        // 检查权限
        if (!image.getUploadedBy().equals(userId)) {
            throw new BusinessException("无权删除该图片");
        }
        
        // 检查是否被使用
        if (image.getIsUsed()) {
            throw new BusinessException("图片已被使用，无法删除");
        }
        
        try {
            // 删除文件
            Path filePath = Paths.get(image.getFilePath());
            Files.deleteIfExists(filePath);
            
            // 删除数据库记录
            uploadedImageRepository.delete(image);
            
            log.info("Image deleted: {} by user {}", image.getStoredName(), userId);
        } catch (IOException e) {
            log.error("Failed to delete image file: {}", e.getMessage(), e);
            throw new BusinessException("删除图片文件失败");
        }
    }

    /**
     * 清理未使用的图片（定期任务）
     */
    @Transactional
    public void cleanupUnusedImages(int daysOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);
        List<UploadedImage> unusedImages = uploadedImageRepository.findUnusedImagesBefore(cutoffTime);
        
        for (UploadedImage image : unusedImages) {
            try {
                Path filePath = Paths.get(image.getFilePath());
                Files.deleteIfExists(filePath);
                uploadedImageRepository.delete(image);
                log.info("Cleaned up unused image: {}", image.getStoredName());
            } catch (IOException e) {
                log.error("Failed to cleanup image: {}", image.getStoredName(), e);
            }
        }
        
        log.info("Cleaned up {} unused images older than {} days", unusedImages.size(), daysOld);
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小不能超过" + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String contentType = file.getContentType();
        if (!UploadedImage.SupportedImageTypes.isSupported(contentType)) {
            throw new BusinessException("不支持的文件类型: " + contentType);
        }
    }

    /**
     * 检查用户上传限制
     */
    private void checkUserUploadLimit(Long userId) {
        Long userImageCount = uploadedImageRepository.countByUploadedBy(userId);
        if (userImageCount >= maxFilesPerUser) {
            throw new BusinessException("用户上传图片数量已达上限: " + maxFilesPerUser);
        }
    }

    /**
     * 生成存储文件名
     */
    private String generateStoredFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
