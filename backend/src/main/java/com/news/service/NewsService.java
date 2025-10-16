package com.news.service;

import com.news.exception.BusinessException;
import com.news.exception.ResourceNotFoundException;
import com.news.model.entity.News;
import com.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 新闻服务
 */
@Service
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;
    private SummaryService summaryService;

    @Value("${summary.auto-generate:true}")
    private boolean autoGenerateSummary;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Autowired
    public void setSummaryService(@Lazy SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    /**
     * 创建新闻
     */
    @Transactional
    public News createNews(News news) {
        log.info("Creating news: {}", news.getTitle());

        // 检查标题是否已存在
        if (newsRepository.existsByTitle(news.getTitle())) {
            throw new BusinessException("新闻标题已存在: " + news.getTitle());
        }

        // 检查URL是否已存在（如果是爬取的新闻）
        if (news.getOriginalUrl() != null && newsRepository.existsByOriginalUrl(news.getOriginalUrl())) {
            throw new BusinessException("新闻URL已存在: " + news.getOriginalUrl());
        }

        News saved = newsRepository.save(news);
        log.info("News created successfully with ID: {}", saved.getId());

        // 自动生成摘要（异步）
        if (autoGenerateSummary && summaryService != null && "PUBLISHED".equals(saved.getStatus())) {
            log.info("Triggering async summary generation for news: {}", saved.getId());
            summaryService.generateSummaryAsync(saved.getId());
        }

        return saved;
    }

    /**
     * 更新新闻
     */
    @Transactional
    public News updateNews(Long id, News news) {
        log.info("Updating news with ID: {}", id);

        News existing = newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("新闻不存在，ID: " + id));

        // 如果修改了标题，检查新标题是否已存在
        if (!existing.getTitle().equals(news.getTitle())) {
            if (newsRepository.existsByTitle(news.getTitle())) {
                throw new BusinessException("新闻标题已存在: " + news.getTitle());
            }
            existing.setTitle(news.getTitle());
        }

        existing.setContent(news.getContent());
        existing.setCategoryId(news.getCategoryId());
        existing.setStatus(news.getStatus());

        News updated = newsRepository.save(existing);
        log.info("News updated successfully: {}", id);
        return updated;
    }

    /**
     * 删除新闻
     */
    @Transactional
    public void deleteNews(Long id) {
        log.info("Deleting news with ID: {}", id);

        if (!newsRepository.existsById(id)) {
            throw new ResourceNotFoundException("新闻不存在，ID: " + id);
        }

        newsRepository.deleteById(id);
        log.info("News deleted successfully: {}", id);
    }

    /**
     * 根据ID获取新闻
     */
    public News getNewsById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("新闻不存在，ID: " + id));
    }

    /**
     * 获取新闻并增加浏览次数
     */
    @Transactional
    public News getNewsWithViewIncrement(Long id) {
        News news = getNewsById(id);
        newsRepository.incrementViewCount(id);
        news.incrementViewCount(); // 更新内存中的对象
        log.info("Incremented view count for news: {}", id);
        return news;
    }

    /**
     * 分页获取所有已发布新闻（智能排序）
     */
    public Page<News> getPublishedNewsWithSmartSort(Pageable pageable) {
        return newsRepository.findByStatusWithSmartSort("PUBLISHED", pageable);
    }

    /**
     * 根据分类分页获取新闻（智能排序）
     */
    public Page<News> getNewsByCategoryWithSmartSort(Long categoryId, Pageable pageable) {
        return newsRepository.findByCategoryIdAndStatusWithSmartSort(
                categoryId, "PUBLISHED", pageable);
    }

    /**
     * 根据状态分页获取新闻
     */
    public Page<News> getNewsByStatus(String status, Pageable pageable) {
        return newsRepository.findByStatus(status, pageable);
    }

    /**
     * 根据来源分页获取新闻
     */
    public Page<News> getNewsBySource(String sourceWebsite, Pageable pageable) {
        return newsRepository.findBySourceWebsite(sourceWebsite, pageable);
    }

    /**
     * 搜索新闻（标题和内容）
     */
    public Page<News> searchNews(String keyword, Pageable pageable) {
        return newsRepository.searchByKeyword(keyword, "PUBLISHED", pageable);
    }

    /**
     * 获取热门新闻
     */
    public Page<News> getHotNews(Pageable pageable) {
        return newsRepository.findHotNews(pageable);
    }

    /**
     * 获取最新新闻
     */
    public Page<News> getLatestNews(Pageable pageable) {
        return newsRepository.findLatestNews(pageable);
    }

    /**
     * 根据时间范围获取新闻
     */
    public Page<News> getNewsByTimeRange(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {
        return newsRepository.findByTimeRange(startTime, endTime, "PUBLISHED", pageable);
    }

    /**
     * 统计分类下的新闻数量
     */
    public long countNewsByCategory(Long categoryId) {
        return newsRepository.countByCategoryIdAndStatus(categoryId, "PUBLISHED");
    }

    /**
     * 获取总浏览量
     */
    public Long getTotalViewCount() {
        Long total = newsRepository.getTotalViewCount();
        return total != null ? total : 0L;
    }

    /**
     * 发布新闻
     */
    @Transactional
    public News publishNews(Long id) {
        log.info("Publishing news with ID: {}", id);

        News news = getNewsById(id);
        news.setStatus("PUBLISHED");
        if (news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }

        News published = newsRepository.save(news);
        log.info("News published successfully: {}", id);
        return published;
    }

    /**
     * 归档新闻
     */
    @Transactional
    public News archiveNews(Long id) {
        log.info("Archiving news with ID: {}", id);

        News news = getNewsById(id);
        news.setStatus("ARCHIVED");

        News archived = newsRepository.save(news);
        log.info("News archived successfully: {}", id);
        return archived;
    }
}

