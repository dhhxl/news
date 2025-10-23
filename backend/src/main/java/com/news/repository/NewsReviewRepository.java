package com.news.repository;

import com.news.model.entity.NewsReview;
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
 * 新闻审核记录Repository
 */
@Repository
public interface NewsReviewRepository extends JpaRepository<NewsReview, Long> {

    /**
     * 根据新闻ID查找所有审核记录，按时间倒序
     */
    List<NewsReview> findByNewsIdOrderByReviewedAtDesc(Long newsId);

    /**
     * 根据审核人ID查找审核记录
     */
    Page<NewsReview> findByReviewerIdOrderByReviewedAtDesc(Long reviewerId, Pageable pageable);

    /**
     * 根据状态查找审核记录
     */
    Page<NewsReview> findByStatusOrderByReviewedAtDesc(String status, Pageable pageable);

    /**
     * 查找新闻的最新审核记录
     */
    Optional<NewsReview> findFirstByNewsIdOrderByReviewedAtDesc(Long newsId);

    /**
     * 根据动作和时间范围查找审核记录
     */
    @Query("SELECT nr FROM NewsReview nr WHERE nr.action = :action AND nr.reviewedAt BETWEEN :startTime AND :endTime ORDER BY nr.reviewedAt DESC")
    List<NewsReview> findByActionAndTimeRange(@Param("action") String action, 
                                              @Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计审核人的审核数量
     */
    @Query("SELECT COUNT(nr) FROM NewsReview nr WHERE nr.reviewerId = :reviewerId AND nr.action IN :actions")
    Long countByReviewerIdAndActions(@Param("reviewerId") Long reviewerId, @Param("actions") List<String> actions);

    /**
     * 查找待审核的新闻审核记录
     */
    @Query("SELECT nr FROM NewsReview nr WHERE nr.status = 'PENDING' ORDER BY nr.reviewedAt ASC")
    List<NewsReview> findPendingReviews();

    /**
     * 查找超时的审核记录
     */
    @Query("SELECT DISTINCT nr.newsId FROM NewsReview nr " +
           "WHERE nr.status = 'PENDING' AND nr.reviewedAt < :timeoutBefore")
    List<Long> findOverdueReviewNewsIds(@Param("timeoutBefore") LocalDateTime timeoutBefore);

    /**
     * 删除新闻相关的所有审核记录
     */
    void deleteByNewsId(Long newsId);
}
