package com.news.repository;

import com.news.model.entity.UploadedImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 上传图片Repository
 */
@Repository
public interface UploadedImageRepository extends JpaRepository<UploadedImage, Long> {

    /**
     * 根据上传用户ID查找图片
     */
    Page<UploadedImage> findByUploadedByOrderByUploadTimeDesc(Long uploadedBy, Pageable pageable);

    /**
     * 根据新闻ID查找关联的图片
     */
    List<UploadedImage> findByNewsIdOrderByUploadTimeAsc(Long newsId);

    /**
     * 根据存储文件名查找图片
     */
    Optional<UploadedImage> findByStoredName(String storedName);

    /**
     * 查找用户的未使用图片
     */
    List<UploadedImage> findByUploadedByAndIsUsedFalseOrderByUploadTimeDesc(Long uploadedBy);

    /**
     * 查找指定时间之前的未使用图片（用于清理）
     */
    @Query("SELECT ui FROM UploadedImage ui WHERE ui.isUsed = false AND ui.uploadTime < :before")
    List<UploadedImage> findUnusedImagesBefore(@Param("before") LocalDateTime before);

    /**
     * 统计用户上传的图片数量
     */
    Long countByUploadedBy(Long uploadedBy);

    /**
     * 统计用户上传的图片总大小
     */
    @Query("SELECT COALESCE(SUM(ui.fileSize), 0) FROM UploadedImage ui WHERE ui.uploadedBy = :uploadedBy")
    Long sumFileSizeByUploadedBy(@Param("uploadedBy") Long uploadedBy);

    /**
     * 查找指定ID列表的图片
     */
    List<UploadedImage> findByIdInOrderByUploadTimeAsc(List<Long> ids);

    /**
     * 根据MIME类型查找图片
     */
    List<UploadedImage> findByMimeTypeInOrderByUploadTimeDesc(List<String> mimeTypes);

    /**
     * 删除新闻关联的图片（将newsId设为null）
     */
    @Query("UPDATE UploadedImage ui SET ui.newsId = null WHERE ui.newsId = :newsId")
    void unassignFromNews(@Param("newsId") Long newsId);
}
