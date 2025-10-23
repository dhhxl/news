package com.news.service;

import com.news.exception.ResourceNotFoundException;
import com.news.model.entity.Comment;
import com.news.model.entity.User;
import com.news.repository.CommentRepository;
import com.news.repository.NewsRepository;
import com.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    /**
     * 创建评论
     */
    @Transactional
    public Comment createComment(Long newsId, Long userId, String content) {
        log.info("Creating comment for news: {}, user: {}", newsId, userId);

        // 验证新闻是否存在
        if (!newsRepository.existsById(newsId)) {
            throw new ResourceNotFoundException("新闻不存在，ID: " + newsId);
        }

        // 验证用户是否存在
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("用户不存在，ID: " + userId);
        }

        Comment comment = Comment.builder()
                .newsId(newsId)
                .userId(userId)
                .content(content)
                .status("APPROVED") // 默认审核通过
                .build();

        Comment saved = commentRepository.save(comment);
        log.info("Comment created successfully: {}", saved.getId());
        
        return saved;
    }

    /**
     * 获取新闻的评论列表（带用户信息）
     */
    public List<Comment> getNewsComments(Long newsId) {
        List<Comment> comments = commentRepository.findByNewsIdAndStatusOrderByCreatedAtDesc(
                newsId, "APPROVED");
        
        // 填充用户名
        comments.forEach(comment -> {
            userRepository.findById(comment.getUserId())
                    .ifPresent(user -> comment.setUsername(user.getUsername()));
        });
        
        return comments;
    }

    /**
     * 分页获取新闻评论
     */
    public Page<Comment> getNewsCommentsPaged(Long newsId, Pageable pageable) {
        return commentRepository.findByNewsIdAndStatus(newsId, "APPROVED", pageable);
    }

    /**
     * 获取用户的评论列表
     */
    public Page<Comment> getUserComments(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable);
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在，ID: " + commentId));

        // 只有评论作者或管理员可以删除
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new RuntimeException("无权删除此评论");
        }

        commentRepository.delete(comment);
        log.info("Comment deleted: {}", commentId);
    }

    /**
     * 统计新闻评论数
     */
    public long countNewsComments(Long newsId) {
        return commentRepository.countByNewsIdAndStatus(newsId, "APPROVED");
    }

    /**
     * 更新评论状态（管理员）
     */
    @Transactional
    public Comment updateCommentStatus(Long commentId, String status) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("评论不存在，ID: " + commentId));

        comment.setStatus(status);
        return commentRepository.save(comment);
    }
}

