package com.news.repository;

import com.news.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论Repository
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据新闻ID分页查询评论
     */
    Page<Comment> findByNewsIdAndStatus(Long newsId, String status, Pageable pageable);

    /**
     * 根据新闻ID查询所有评论
     */
    List<Comment> findByNewsIdAndStatusOrderByCreatedAtDesc(Long newsId, String status);

    /**
     * 根据用户ID查询评论
     */
    Page<Comment> findByUserId(Long userId, Pageable pageable);

    /**
     * 统计新闻的评论数
     */
    long countByNewsIdAndStatus(Long newsId, String status);

    /**
     * 删除新闻的所有评论
     */
    void deleteByNewsId(Long newsId);
}

