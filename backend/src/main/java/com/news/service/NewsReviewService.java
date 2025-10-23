package com.news.service;

import com.news.exception.BusinessException;
import com.news.model.dto.NewsReviewRequest;
import com.news.model.dto.NewsReviewResponse;
import com.news.model.entity.News;
import com.news.model.entity.NewsReview;
import com.news.model.entity.User;
import com.news.model.enums.UserRole;
import com.news.repository.NewsRepository;
import com.news.repository.NewsReviewRepository;
import com.news.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 新闻审核服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NewsReviewService {

    private final NewsRepository newsRepository;
    private final NewsReviewRepository newsReviewRepository;
    private final UserRepository userRepository;

    /**
     * 提交新闻审核
     */
    @Transactional
    public NewsReviewResponse submitForReview(Long newsId, Long submitterId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException("新闻不存在"));

        // 检查新闻状态
        if (!news.canEdit()) {
            throw new BusinessException("当前状态的新闻无法提交审核");
        }

        // 检查是否是创建者
        if (!news.getCreatedBy().equals(submitterId)) {
            throw new BusinessException("只能提交自己创建的新闻");
        }

        // 更新新闻状态
        news.submitForReview(submitterId);
        newsRepository.save(news);

        // 记录审核操作
        NewsReview review = NewsReview.createSubmitRecord(newsId, submitterId);
        newsReviewRepository.save(review);

        log.info("News {} submitted for review by user {}", newsId, submitterId);
        return buildNewsReviewResponse(news);
    }

    /**
     * 分配审核人
     */
    @Transactional
    public void assignReviewer(Long newsId, Long reviewerId, Long adminId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException("新闻不存在"));

        if (!News.NewsStatus.PENDING.equals(news.getStatus())) {
            throw new BusinessException("只能为待审核状态的新闻分配审核人");
        }

        // 检查审核人权限
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new BusinessException("审核人不存在"));

        UserRole role = UserRole.fromCode(reviewer.getRole());
        if (role == null || !role.canReviewNews()) {
            throw new BusinessException("该用户没有审核权限");
        }

        // 分配审核人
        news.assignReviewer(reviewerId);
        newsRepository.save(news);

        log.info("News {} assigned to reviewer {} by admin {}", newsId, reviewerId, adminId);
    }

    /**
     * 审核新闻
     */
    @Transactional
    public NewsReviewResponse reviewNews(NewsReviewRequest request, Long reviewerId) {
        News news = newsRepository.findById(request.getNewsId())
                .orElseThrow(() -> new BusinessException("新闻不存在"));

        // 检查审核权限
        if (!news.needsReview()) {
            throw new BusinessException("新闻当前状态不需要审核");
        }

        if (news.getCurrentReviewer() != null && !news.getCurrentReviewer().equals(reviewerId)) {
            throw new BusinessException("该新闻已被其他审核人处理");
        }

        NewsReview review;
        switch (request.getAction()) {
            case NewsReview.ReviewAction.APPROVE:
                news.approveAndPublish();
                review = NewsReview.createApproveRecord(news.getId(), reviewerId, request.getReviewComment());
                log.info("News {} approved by reviewer {}", news.getId(), reviewerId);
                break;

            case NewsReview.ReviewAction.REJECT:
                news.reject();
                review = NewsReview.createRejectRecord(news.getId(), reviewerId, request.getReviewComment());
                log.info("News {} rejected by reviewer {}", news.getId(), reviewerId);
                break;

            default:
                throw new BusinessException("不支持的审核动作: " + request.getAction());
        }

        newsRepository.save(news);
        newsReviewRepository.save(review);

        return buildNewsReviewResponse(news);
    }

    /**
     * 获取待审核新闻列表
     */
    public Page<NewsReviewResponse> getPendingReviews(Pageable pageable) {
        Page<News> newsPage = newsRepository.findByStatusOrderBySubmittedAtAsc(
                News.NewsStatus.PENDING, pageable);
        return newsPage.map(this::buildNewsReviewResponse);
    }

    /**
     * 获取正在审核的新闻列表
     */
    public Page<NewsReviewResponse> getReviewingNews(Long reviewerId, Pageable pageable) {
        Page<News> newsPage = newsRepository.findByCurrentReviewerOrderBySubmittedAtAsc(reviewerId, pageable);
        return newsPage.map(this::buildNewsReviewResponse);
    }

    /**
     * 获取所有需要审核的新闻（管理员视图）
     */
    public Page<NewsReviewResponse> getAllReviewableNews(Pageable pageable) {
        Page<News> newsPage = newsRepository.findAllReviewableNews(pageable);
        return newsPage.map(this::buildNewsReviewResponse);
    }

    /**
     * 获取用户提交的新闻列表
     */
    public Page<NewsReviewResponse> getUserSubmittedNews(Long submitterId, String status, Pageable pageable) {
        Page<News> newsPage = newsRepository.findBySubmittedByAndStatus(submitterId, status, pageable);
        return newsPage.map(this::buildNewsReviewResponse);
    }

    /**
     * 获取新闻审核历史
     */
    public List<NewsReview> getNewsReviewHistory(Long newsId) {
        return newsReviewRepository.findByNewsIdOrderByReviewedAtDesc(newsId);
    }

    /**
     * 处理超时审核
     */
    @Transactional
    public void handleOverdueReviews() {
        List<News> overdueNews = newsRepository.findOverdueReviews(LocalDateTime.now());
        for (News news : overdueNews) {
            // 将超时的新闻重新设为待审核状态
            news.setStatus(News.NewsStatus.PENDING);
            news.setCurrentReviewer(null);
            newsRepository.save(news);

            log.warn("News {} review is overdue, reset to PENDING status", news.getId());
        }
    }

    /**
     * 获取审核统计信息
     */
    public Map<String, Long> getReviewStats() {
        long pendingCount = newsRepository.countByStatus(News.NewsStatus.PENDING);
        long reviewingCount = newsRepository.countByStatus(News.NewsStatus.REVIEWING);
        return Map.of("pending", pendingCount, "reviewing", reviewingCount);
    }

    /**
     * 构建审核响应DTO
     */
    private NewsReviewResponse buildNewsReviewResponse(News news) {
        NewsReviewResponse response = NewsReviewResponse.fromNews(news);

        // 添加用户名信息
        if (news.getSubmittedBy() != null) {
            userRepository.findById(news.getSubmittedBy())
                    .ifPresent(user -> response.setSubmittedByUsername(user.getUsername()));
        }

        if (news.getCurrentReviewer() != null) {
            userRepository.findById(news.getCurrentReviewer())
                    .ifPresent(user -> response.setCurrentReviewerUsername(user.getUsername()));
        }

        // 添加审核历史
        List<NewsReview> reviews = getNewsReviewHistory(news.getId());
        response.addReviewHistory(reviews);

        return response;
    }
}
