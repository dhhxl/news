package com.news.service;

import com.news.exception.BusinessException;
import com.news.model.dto.NewsSubmitRequest;
import com.news.model.entity.News;
import com.news.model.entity.NewsReview;
import com.news.repository.NewsRepository;
import com.news.repository.NewsReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 编辑新闻服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EditorNewsService {

    private final NewsRepository newsRepository;
    private final NewsReviewRepository newsReviewRepository;

    /**
     * 创建新闻草稿
     */
    @Transactional
    public News createDraft(NewsSubmitRequest request, Long editorId) {
        validateNewsRequest(request);

        News news = News.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .categoryId(request.getCategoryId())
                .originalUrl(request.getOriginalUrl() != null && !request.getOriginalUrl().trim().isEmpty() 
                    ? request.getOriginalUrl().trim() : "MANUAL_" + System.currentTimeMillis())
                .sourceWebsite("MANUAL")
                .status(News.NewsStatus.DRAFT)
                .classificationMethod("MANUAL")
                .createdBy(editorId)
                .viewCount(0L)
                .publishTime(LocalDateTime.now()) // 设置占位符时间，真正发布时会更新
                .build();

        news = newsRepository.save(news);

        log.info("Draft created: {} by editor {}", news.getId(), editorId);
        return news;
    }

    /**
     * 更新新闻
     */
    @Transactional
    public News updateNews(Long newsId, NewsSubmitRequest request, Long editorId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException("新闻不存在"));

        // 检查编辑权限
        if (!news.getCreatedBy().equals(editorId)) {
            throw new BusinessException("只能编辑自己创建的新闻");
        }

        if (!news.canEdit()) {
            throw new BusinessException("当前状态的新闻无法编辑");
        }

        validateNewsRequest(request);

        // 更新基本信息
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setCategoryId(request.getCategoryId());
        news.setOriginalUrl(request.getOriginalUrl() != null && !request.getOriginalUrl().trim().isEmpty() 
            ? request.getOriginalUrl().trim() : null);

        // 处理图片更新
        if (request.getImageIds() != null) {
            // TODO: 图片处理逻辑待实现
        }

        news = newsRepository.save(news);
        log.info("News updated: {} by editor {}", newsId, editorId);
        return news;
    }

    /**
     * 提交新闻或保存草稿
     */
    @Transactional
    public News submitNews(NewsSubmitRequest request, Long editorId) {
        validateNewsRequest(request);

        News news = News.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .categoryId(request.getCategoryId())
                .originalUrl(request.getOriginalUrl() != null && !request.getOriginalUrl().trim().isEmpty() 
                    ? request.getOriginalUrl().trim() : "MANUAL_" + System.currentTimeMillis())
                .sourceWebsite("MANUAL")
                .status(News.NewsStatus.PENDING) // 直接提交审核
                .classificationMethod("MANUAL")
                .createdBy(editorId)
                .viewCount(0L)
                .publishTime(LocalDateTime.now()) // 设置占位符时间，真正发布时会更新
                .build();

        // 提交审核
        news.submitForReview(editorId);

        // 处理图片
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            // TODO: 图片处理逻辑待实现
        }

        News savedNews = newsRepository.save(news);
        
        // 创建审核记录
        NewsReview submitRecord = NewsReview.createSubmitRecord(savedNews.getId(), editorId);
        newsReviewRepository.save(submitRecord);
        log.info("Editor {} submitted news: {}", editorId, savedNews.getTitle());

        return savedNews;
    }

    /**
     * 重新提交被退回的新闻
     */
    @Transactional
    public News resubmitNews(Long newsId, NewsSubmitRequest request, Long editorId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException("新闻不存在"));

        if (!news.getCreatedBy().equals(editorId)) {
            throw new BusinessException("无权编辑此新闻");
        }

        if (!News.NewsStatus.REJECTED.equals(news.getStatus())) {
            throw new BusinessException("只有被退回的新闻才能重新提交");
        }

        // 更新内容
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setCategoryId(request.getCategoryId());
        news.setOriginalUrl(request.getOriginalUrl() != null && !request.getOriginalUrl().trim().isEmpty() 
            ? request.getOriginalUrl().trim() : null);

        // 处理图片
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            // TODO: 图片处理逻辑待实现
        }

        // 重新提交审核
        news.submitForReview(editorId);

        News updatedNews = newsRepository.save(news);
        
        // 创建审核记录
        NewsReview submitRecord = NewsReview.createSubmitRecord(newsId, editorId);
        newsReviewRepository.save(submitRecord);
        log.info("Editor {} resubmitted news {}: {}", editorId, newsId, updatedNews.getTitle());

        return updatedNews;
    }

    /**
     * 获取编辑创建的所有新闻列表（包括草稿和已提交的）
     */
    public Page<News> getEditorNews(Long editorId, Pageable pageable) {
        return newsRepository.findByCreatedByOrderByUpdatedAtDesc(editorId, pageable);
    }

    /**
     * 获取编辑的新闻（按状态）
     */
    public Page<News> getEditorNewsByStatus(Long editorId, String status, Pageable pageable) {
        return newsRepository.findBySubmittedByAndStatus(editorId, status, pageable);
    }

    /**
     * 删除草稿
     */
    @Transactional
    public void deleteDraft(Long newsId, Long editorId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException("新闻不存在"));

        // 检查权限
        if (!news.getCreatedBy().equals(editorId)) {
            throw new BusinessException("只能删除自己创建的新闻");
        }

        // 只能删除草稿状态的新闻
        if (!News.NewsStatus.DRAFT.equals(news.getStatus())) {
            throw new BusinessException("只能删除草稿状态的新闻");
        }

        // 删除新闻
        newsRepository.delete(news);
        log.info("Draft deleted: {} by editor {}", newsId, editorId);
    }

    /**
     * 获取新闻详情（编辑视图）
     */
    public News getNewsForEdit(Long newsId, Long editorId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException("新闻不存在"));

        // 检查权限
        if (!news.getCreatedBy().equals(editorId)) {
            throw new BusinessException("只能查看自己创建的新闻");
        }

        return news;
    }


    /**
     * 验证新闻请求
     */
    private void validateNewsRequest(NewsSubmitRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException("内容不能为空");
        }
        
        if (request.getCategoryId() == null) {
            throw new BusinessException("分类不能为空");
        }

        // 检查标题重复（排除自身）
        newsRepository.findByTitle(request.getTitle().trim())
                .ifPresent(existingNews -> {
                    throw new BusinessException("标题已存在，请使用其他标题");
                });
    }


    /**
     * 获取编辑员统计数据
     */
    public Map<String, Object> getEditorStatistics(Long editorId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 初始化所有状态的计数
        stats.put("draftCount", 0L);
        stats.put("pendingCount", 0L);
        stats.put("reviewingCount", 0L);
        stats.put("publishedCount", 0L);
        stats.put("rejectedCount", 0L);
        
        // 统计草稿数量（按创建者统计）
        long draftCount = newsRepository.countByCreatedByAndStatus(editorId, News.NewsStatus.DRAFT);
        stats.put("draftCount", draftCount);
        
        // 统计其他状态数量（按提交者统计）
        List<Object[]> statusCounts = newsRepository.countBySubmittedByGroupByStatus(editorId);
        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            
            switch (status) {
                case News.NewsStatus.PENDING:
                    stats.put("pendingCount", count);
                    break;
                case News.NewsStatus.REVIEWING:
                    stats.put("reviewingCount", count);
                    break;
                case News.NewsStatus.PUBLISHED:
                    stats.put("publishedCount", count);
                    break;
                case News.NewsStatus.REJECTED:
                    stats.put("rejectedCount", count);
                    break;
            }
        }
        
        // 计算总阅读量（仅已发布的新闻）
        Long totalViews = newsRepository.findBySubmittedByAndStatus(editorId, News.NewsStatus.PUBLISHED, Pageable.unpaged())
                .getContent()
                .stream()
                .mapToLong(News::getViewCount)
                .sum();
        
        stats.put("totalViews", totalViews);
        
        log.info("Editor {} statistics: {}", editorId, stats);
        return stats;
    }

    /**
     * 编辑员取消审核（仅待审核状态）
     */
    @Transactional
    public void cancelReview(Long newsId, Long editorId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new BusinessException("新闻不存在"));

        if (!news.getCreatedBy().equals(editorId)) {
            throw new BusinessException("您无权取消此新闻的审核");
        }

        if (!News.NewsStatus.PENDING.equals(news.getStatus())) {
            throw new BusinessException("只有待审核状态的新闻才能取消审核");
        }

        // 将状态改回草稿
        news.setStatus(News.NewsStatus.DRAFT);
        news.setSubmittedAt(null);
        news.setSubmittedBy(null);
        news.setCurrentReviewer(null);
        news.setReviewDeadline(null);

        newsRepository.save(news);
        log.info("Editor {} cancelled review for news {}: {}", editorId, newsId, news.getTitle());
    }

    /**
     * 获取编辑员最近动态（最近更新的新闻）
     */
    public Page<News> getEditorRecentNews(Long editorId, Pageable pageable) {
        return newsRepository.findBySubmittedByOrderBySubmittedAtDesc(editorId, pageable);
    }

}
