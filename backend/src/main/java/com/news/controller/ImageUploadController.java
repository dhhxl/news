package com.news.controller;

import com.news.model.entity.UploadedImage;
import com.news.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 图片上传Controller
 * 
 * 注意：context-path 已经是 /api，所以这里只需要 /images
 * 完整路径：/api (context-path) + /images = /api/images
 */
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Slf4j
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    /**
     * 上传单张图片
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<UploadedImage> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        UploadedImage uploadedImage = imageUploadService.uploadImage(file, userId);
        
        return ResponseEntity.ok(uploadedImage);
    }

    /**
     * 批量上传图片
     */
    @PostMapping("/upload/batch")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UploadedImage>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        List<UploadedImage> uploadedImages = imageUploadService.uploadImages(files, userId);
        
        return ResponseEntity.ok(uploadedImages);
    }

    /**
     * 获取用户上传的图片列表
     */
    @GetMapping("/my-images")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<UploadedImage>> getMyImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<UploadedImage> images = imageUploadService.getUserImages(userId, pageable);
        
        return ResponseEntity.ok(images);
    }

    /**
     * 获取用户未使用的图片
     */
    @GetMapping("/unused")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UploadedImage>> getUnusedImages(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<UploadedImage> images = imageUploadService.getUserUnusedImages(userId);
        return ResponseEntity.ok(images);
    }

    /**
     * 获取图片详情
     */
    @GetMapping("/details/{imageId}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<UploadedImage> getImageDetails(@PathVariable Long imageId) {
        UploadedImage image = imageUploadService.getImageById(imageId);
        return ResponseEntity.ok(image);
    }

    /**
     * 访问图片文件（公开访问）
     */
    @GetMapping("/file/{storedName}")
    public ResponseEntity<Resource> getImageFile(@PathVariable String storedName) {
        try {
            UploadedImage image = imageUploadService.getImageByStoredName(storedName);
            Resource resource = imageUploadService.loadImageAsResource(storedName);
            
            // URL编码中文文件名
            String encodedFilename = URLEncoder.encode(image.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to load image: {}", storedName, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteImage(
            @PathVariable Long imageId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuth(authentication);
        imageUploadService.deleteImage(imageId, userId);
        
        return ResponseEntity.ok(Map.of("message", "图片删除成功"));
    }

    /**
     * 关联图片到新闻
     */
    @PostMapping("/associate-to-news")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> associateToNews(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        @SuppressWarnings("unchecked")
        List<Integer> imageIds = (List<Integer>) request.get("imageIds");
        Long newsId = Long.valueOf(request.get("newsId").toString());
        Long userId = getUserIdFromAuth(authentication);
        
        List<Long> longImageIds = imageIds.stream()
                .map(Integer::longValue)
                .toList();
        
        imageUploadService.associateImagesToNews(longImageIds, newsId, userId);
        
        return ResponseEntity.ok(Map.of("message", "图片关联成功"));
    }

    /**
     * 清理未使用的图片（管理员功能）
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cleanupUnusedImages(
            @RequestParam(defaultValue = "30") int daysOld) {
        
        imageUploadService.cleanupUnusedImages(daysOld);
        return ResponseEntity.ok(Map.of("message", "清理完成"));
    }

    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.news.security.CustomUserDetailsService.CustomUserDetails) {
            return ((com.news.security.CustomUserDetailsService.CustomUserDetails) principal).getUserId();
        }
        throw new RuntimeException("无法获取用户ID");
    }
}
