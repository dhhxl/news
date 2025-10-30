package com.news.repository;

import com.news.model.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 新闻Repository
 */
@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    /**
     * 根据标题查找新闻
     */
    Optional<News> findByTitle(String title);

    /**
     * 检查标题是否存在
     */
    boolean existsByTitle(String title);

    /**
     * 根据原始URL查找
     */
    Optional<News> findByOriginalUrl(String originalUrl);

    /**
     * 检查URL是否存在
     */
    boolean existsByOriginalUrl(String originalUrl);

    /**
     * 根据分类ID分页查询已发布新闻
     */
    Page<News> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    /**
     * 根据状态分页查询新闻
     */
    Page<News> findByStatus(String status, Pageable pageable);

    /**
     * 根据来源分页查询新闻
     */
    Page<News> findBySourceWebsite(String sourceWebsite, Pageable pageable);

    /**
     * 智能排序查询（时间衰减 + 浏览量）
     * 使用自定义SQL实现智能排序
     */
    @Query(value = "SELECT * FROM news WHERE status = :status " +
           "ORDER BY (view_count / (1 + TIMESTAMPDIFF(HOUR, publish_time, NOW()) / 24)) DESC, " +
           "publish_time DESC",
           nativeQuery = true)
    Page<News> findByStatusWithSmartSort(@Param("status") String status, Pageable pageable);

    /**
     * 根据分类智能排序查询
     */
    @Query(value = "SELECT * FROM news WHERE category_id = :categoryId AND status = :status " +
           "ORDER BY (view_count / (1 + TIMESTAMPDIFF(HOUR, publish_time, NOW()) / 24)) DESC, " +
           "publish_time DESC",
           nativeQuery = true)
    Page<News> findByCategoryIdAndStatusWithSmartSort(
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            Pageable pageable
    );

    /**
     * 全文搜索（标题和内容）
     */
    @Query("SELECT n FROM News n WHERE " +
           "(n.title LIKE %:keyword% OR n.content LIKE %:keyword%) " +
           "AND n.status = :status " +
           "ORDER BY n.publishTime DESC")
    Page<News> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );

    /**
     * 增加浏览次数
     */
    @Modifying
    @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.id = :newsId")
    void incrementViewCount(@Param("newsId") Long newsId);

    /**
     * 获取热门新闻（最近7天浏览量最多）
     */
    @Query(value = "SELECT * FROM news WHERE status = 'PUBLISHED' " +
           "AND publish_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
           "ORDER BY view_count DESC",
           nativeQuery = true)
    Page<News> findHotNews(Pageable pageable);

    /**
     * 获取最新新闻
     */
    @Query("SELECT n FROM News n WHERE n.status = 'PUBLISHED' " +
           "ORDER BY n.publishTime DESC")
    Page<News> findLatestNews(Pageable pageable);

    /**
     * 根据时间范围查询
     */
    @Query("SELECT n FROM News n WHERE n.publishTime BETWEEN :startTime AND :endTime " +
           "AND n.status = :status " +
           "ORDER BY n.publishTime DESC")
    Page<News> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") String status,
            Pageable pageable
    );

    /**
     * 统计分类下的新闻数量
     */
    long countByCategoryIdAndStatus(Long categoryId, String status);

    /**
     * 统计总浏览量
     */
    @Query("SELECT SUM(n.viewCount) FROM News n WHERE n.status = 'PUBLISHED'")
    Long getTotalViewCount();

    // ========== 审核系统相关查询 ==========

    /**
     * 查找待审核的新闻
     */
    Page<News> findByStatusOrderBySubmittedAtAsc(String status, Pageable pageable);

    /**
     * 根据提交人查找新闻
     */
    Page<News> findBySubmittedByOrderBySubmittedAtDesc(Long submittedBy, Pageable pageable);

    /**
     * 根据当前审核人查找正在审核的新闻
     */
    Page<News> findByCurrentReviewerOrderBySubmittedAtAsc(Long currentReviewer, Pageable pageable);

    /**
     * 查找超时的待审核新闻
     */
    @Query("SELECT n FROM News n WHERE n.status IN ('PENDING', 'REVIEWING') " +
           "AND n.reviewDeadline < :now")
    List<News> findOverdueReviews(@Param("now") LocalDateTime now);

    /**
     * 根据状态和提交人查找新闻
     */
    @Query("SELECT n FROM News n WHERE n.submittedBy = :submittedBy " +
           "AND (:status IS NULL OR n.status = :status) " +
           "ORDER BY n.submittedAt DESC")
    Page<News> findBySubmittedByAndStatus(@Param("submittedBy") Long submittedBy, 
                                          @Param("status") String status, 
                                          Pageable pageable);

    /**
     * 统计用户各状态的新闻数量
     */
    @Query("SELECT n.status, COUNT(n) FROM News n WHERE n.submittedBy = :submittedBy GROUP BY n.status")
    List<Object[]> countBySubmittedByGroupByStatus(@Param("submittedBy") Long submittedBy);

    /**
     * 统计待审核新闻数量
     */
    Long countByStatus(String status);

    /**
     * 统计各审核人的待审核新闻数量
     */
    @Query("SELECT n.currentReviewer, COUNT(n) FROM News n WHERE n.status = 'REVIEWING' " +
           "GROUP BY n.currentReviewer")
    List<Object[]> countReviewingNewsByReviewer();

    /**
     * 查找编辑可以编辑的新闻（草稿和已退回）
     */
    @Query("SELECT n FROM News n WHERE n.createdBy = :editorId " +
           "AND n.status IN ('DRAFT', 'REJECTED') " +
           "ORDER BY n.updatedAt DESC")
    Page<News> findEditableNewsByEditor(@Param("editorId") Long editorId, Pageable pageable);

    /**
     * 查找所有需要审核的新闻（管理员视图）
     */
    @Query("SELECT n FROM News n WHERE n.status IN ('PENDING', 'REVIEWING') " +
           "ORDER BY n.submittedAt ASC")
    Page<News> findAllReviewableNews(Pageable pageable);

    /**
     * 统计指定用户创建的指定状态新闻数量
     */
    Long countByCreatedByAndStatus(Long createdBy, String status);

    /**
     * 根据创建者查找新闻，按更新时间降序排列
     */
    Page<News> findByCreatedByOrderByUpdatedAtDesc(Long createdBy, Pageable pageable);

    /**
     * 根据分类ID查询新闻
     */
    Page<News> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * 根据状态和分类ID查询新闻
     */
    Page<News> findByStatusAndCategoryId(String status, Long categoryId, Pageable pageable);

    /**
     * 按分类获取热门新闻
     */
    @Query(value = "SELECT * FROM news WHERE category_id = :categoryId AND status = 'PUBLISHED' " +
           "AND publish_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
           "ORDER BY view_count DESC",
           nativeQuery = true)
    Page<News> findHotNewsByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 管理员搜索新闻（支持所有状态）
     */
    @Query("SELECT n FROM News n WHERE " +
           "(n.title LIKE %:keyword% OR n.content LIKE %:keyword%) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "ORDER BY n.updatedAt DESC")
    Page<News> searchByKeywordForAdmin(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );
}

