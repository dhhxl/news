package com.news.service;

import com.news.crawler.NewsCrawler;
import com.news.model.entity.CrawlTask;
import com.news.model.entity.News;
import com.news.repository.CrawlTaskRepository;
import com.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬虫调度服务
 * 负责协调爬虫执行、去重、自动分类等
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final List<NewsCrawler> crawlers; // 自动注入所有NewsCrawler实现
    private final NewsRepository newsRepository;
    private final CrawlTaskRepository crawlTaskRepository;
    private final ClassificationService classificationService;

    /**
     * 执行单个爬虫任务
     */
    @Async
    @Transactional
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void executeCrawlTask(String sourceName, int maxCount) {
        log.info("Starting crawl task for source: {}, maxCount: {}", sourceName, maxCount);

        // 创建爬取任务记录
        CrawlTask task = CrawlTask.builder()
                .targetSource(sourceName)
                .build();
        task = crawlTaskRepository.save(task);
        task.markAsRunning();
        task = crawlTaskRepository.save(task);

        try {
            // 查找对应的爬虫
            NewsCrawler crawler = findCrawler(sourceName);
            if (crawler == null) {
                throw new IllegalArgumentException("Unknown source: " + sourceName);
            }

            // 执行爬取
            List<News> crawledNews = crawler.crawlNews(maxCount);
            log.info("Crawled {} news from {}", crawledNews.size(), sourceName);

            // 处理爬取的新闻
            int successCount = 0;
            int failCount = 0;

            for (News news : crawledNews) {
                try {
                    // 1. 去重检测
                    if (isDuplicate(news)) {
                        log.info("Duplicate news skipped: {}", news.getTitle());
                        failCount++;
                        continue;
                    }

                    // 2. 自动分类
                    if (news.getCategoryId() == null) {
                        Long categoryId = classificationService.classifyNews(news);
                        news.setCategoryId(categoryId);
                    }

                    // 3. 保存新闻
                    newsRepository.save(news);
                    successCount++;
                    log.info("Saved news: {}", news.getTitle());

                } catch (Exception e) {
                    log.error("Failed to save news {}: {}", news.getTitle(), e.getMessage());
                    failCount++;
                }
            }

            // 更新任务状态
            task.setSuccessCount(successCount);
            task.setFailCount(failCount);
            task.markAsSuccess();
            crawlTaskRepository.save(task);

            log.info("Crawl task completed for {}: success={}, fail={}", sourceName, successCount, failCount);

        } catch (Exception e) {
            log.error("Crawl task failed for {}: {}", sourceName, e.getMessage(), e);
            task.markAsFailed(e.getMessage());
            crawlTaskRepository.save(task);
        }
    }

    /**
     * 执行所有爬虫任务
     */
    @Async
    public void executeAllCrawlers(int maxCountPerSource) {
        log.info("Starting all crawlers, maxCount per source: {}", maxCountPerSource);
        
        for (NewsCrawler crawler : crawlers) {
            try {
                executeCrawlTask(crawler.getSourceName(), maxCountPerSource);
            } catch (Exception e) {
                log.error("Failed to execute crawler {}: {}", crawler.getSourceName(), e.getMessage());
            }
        }
    }

    /**
     * 查找指定来源的爬虫
     */
    private NewsCrawler findCrawler(String sourceName) {
        return crawlers.stream()
                .filter(c -> c.getSourceName().equals(sourceName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 去重检测
     * 根据标题完全匹配判断是否重复
     */
    private boolean isDuplicate(News news) {
        // 策略1：检查标题
        if (newsRepository.existsByTitle(news.getTitle())) {
            return true;
        }

        // 策略2：检查原始URL
        if (news.getOriginalUrl() != null && 
            newsRepository.existsByOriginalUrl(news.getOriginalUrl())) {
            return true;
        }

        return false;
    }

    /**
     * 获取所有可用的爬虫来源
     */
    public List<String> getAvailableSources() {
        List<String> sources = new ArrayList<>();
        for (NewsCrawler crawler : crawlers) {
            sources.add(crawler.getSourceName());
        }
        return sources;
    }

    /**
     * 测试爬虫连接
     */
    public Map<String, Boolean> testAllCrawlers() {
        Map<String, Boolean> results = new HashMap<>();
        for (NewsCrawler crawler : crawlers) {
            boolean isAvailable = crawler.testConnection();
            results.put(crawler.getSourceName(), isAvailable);
            log.info("Crawler {} test result: {}", crawler.getSourceName(), isAvailable);
        }
        return results;
    }

    /**
     * 获取爬取任务历史
     */
    public List<CrawlTask> getTaskHistory(int limit) {
        return crawlTaskRepository.findLatestTasks(
                org.springframework.data.domain.PageRequest.of(0, limit)
        ).getContent();
    }
}

