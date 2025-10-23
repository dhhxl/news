package com.news.repository;

import com.news.model.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 摘要Repository
 */
@Repository
public interface SummaryRepository extends JpaRepository<Summary, Long> {

    /**
     * 根据新闻ID查找摘要
     */
    Optional<Summary> findByNewsId(Long newsId);

    /**
     * 检查新闻是否已有摘要
     */
    boolean existsByNewsId(Long newsId);

    /**
     * 删除指定新闻的摘要
     */
    void deleteByNewsId(Long newsId);
}

