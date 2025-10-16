package com.news.service;

import com.news.exception.ResourceNotFoundException;
import com.news.model.entity.Like;
import com.news.repository.LikeRepository;
import com.news.repository.NewsRepository;
import com.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    /**
     * 点赞新闻
     */
    @Transactional
    public void likeNews(Long newsId, Long userId) {
        log.info("User {} liking news {}", userId, newsId);

        // 验证新闻是否存在
        if (!newsRepository.existsById(newsId)) {
            throw new ResourceNotFoundException("新闻不存在，ID: " + newsId);
        }

        // 验证用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在，ID: " + userId);
        }

        // 检查是否已点赞
        if (likeRepository.existsByNewsIdAndUserId(newsId, userId)) {
            log.info("User {} already liked news {}", userId, newsId);
            return;
        }

        Like like = Like.builder()
                .newsId(newsId)
                .userId(userId)
                .build();

        likeRepository.save(like);
        log.info("Like created successfully");
    }

    /**
     * 取消点赞
     */
    @Transactional
    public void unlikeNews(Long newsId, Long userId) {
        log.info("User {} unliking news {}", userId, newsId);

        if (likeRepository.existsByNewsIdAndUserId(newsId, userId)) {
            likeRepository.deleteByNewsIdAndUserId(newsId, userId);
            log.info("Like removed successfully");
        }
    }

    /**
     * 检查用户是否点赞了新闻
     */
    public boolean hasUserLiked(Long newsId, Long userId) {
        return likeRepository.existsByNewsIdAndUserId(newsId, userId);
    }

    /**
     * 统计新闻点赞数
     */
    public long countNewsLikes(Long newsId) {
        return likeRepository.countByNewsId(newsId);
    }
}

