package com.news.repository;

import com.news.model.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 点赞Repository
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    /**
     * 查找用户对新闻的点赞
     */
    Optional<Like> findByNewsIdAndUserId(Long newsId, Long userId);

    /**
     * 检查用户是否点赞了新闻
     */
    boolean existsByNewsIdAndUserId(Long newsId, Long userId);

    /**
     * 统计新闻的点赞数
     */
    long countByNewsId(Long newsId);

    /**
     * 删除点赞
     */
    void deleteByNewsIdAndUserId(Long newsId, Long userId);

    /**
     * 删除新闻的所有点赞
     */
    void deleteByNewsId(Long newsId);
}

