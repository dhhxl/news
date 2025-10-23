package com.news.service;

import com.news.ai.ZhipuAIClient;
import com.news.exception.ResourceNotFoundException;
import com.news.model.entity.News;
import com.news.model.entity.Summary;
import com.news.repository.NewsRepository;
import com.news.repository.SummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 摘要服务
 * 负责生成和管理新闻摘要
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final NewsRepository newsRepository;
    private final ZhipuAIClient zhipuAIClient;

    @Value("${zhipuai.model:glm-4}")
    private String aiModel;

    /**
     * 为新闻生成摘要
     */
    @Transactional
    public Summary generateSummary(Long newsId) {
        log.info("Generating summary for news ID: {}", newsId);

        // 检查新闻是否存在
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("新闻不存在，ID: " + newsId));

        // 检查是否已有摘要，如果有则先删除
        summaryRepository.findByNewsId(newsId).ifPresent(existingSummary -> {
            log.info("Summary already exists for news: {}, deleting old summary...", newsId);
            summaryRepository.delete(existingSummary);
            summaryRepository.flush(); // 强制立即执行删除
            log.info("Old summary deleted, will generate new one");
        });

        try {
            // 调用AI生成摘要
            String summaryText = zhipuAIClient.generateSummary(
                    news.getTitle(),
                    news.getContent()
            );

            // 保存摘要
            Summary summary = Summary.builder()
                    .newsId(newsId)
                    .summaryContent(summaryText)
                    .modelVersion(aiModel)
                    .status("SUCCESS")
                    .generatedAt(LocalDateTime.now())
                    .build();

            Summary saved = summaryRepository.save(summary);
            log.info("Summary generated successfully for news: {}", newsId);
            return saved;

        } catch (Exception e) {
            log.error("Failed to generate summary for news {}: {}", newsId, e.getMessage(), e);
            
            // 保存失败记录
            Summary failedSummary = Summary.builder()
                    .newsId(newsId)
                    .summaryContent("摘要生成失败: " + e.getMessage())
                    .modelVersion(aiModel)
                    .status("FAILED")
                    .generatedAt(LocalDateTime.now())
                    .build();
            
            return summaryRepository.save(failedSummary);
        }
    }

    /**
     * 异步生成摘要
     */
    @Async
    @Transactional
    public void generateSummaryAsync(Long newsId) {
        generateSummary(newsId);
    }

    /**
     * 获取新闻的摘要
     */
    public Summary getSummaryByNewsId(Long newsId) {
        return summaryRepository.findByNewsId(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("摘要不存在，新闻ID: " + newsId));
    }

    /**
     * 删除摘要
     */
    @Transactional
    public void deleteSummary(Long newsId) {
        if (!summaryRepository.existsByNewsId(newsId)) {
            throw new ResourceNotFoundException("摘要不存在，新闻ID: " + newsId);
        }
        summaryRepository.deleteByNewsId(newsId);
        log.info("Summary deleted for news: {}", newsId);
    }

    /**
     * 批量生成摘要
     * @param forceRegenerate 是否强制重新生成已有摘要
     */
    @Async("taskExecutor")
    public void generateSummariesForAllNews(boolean forceRegenerate) {
        log.info("Starting batch summary generation for all published news (forceRegenerate: {})", forceRegenerate);
        
        try {
            // 分页获取新闻，避免一次加载过多
            int pageSize = 20;
            int currentPage = 0;
            boolean hasMore = true;
            
            int successCount = 0;
            int skipCount = 0;
            int failCount = 0;
            
            while (hasMore) {
                // 每次处理一页
                var page = newsRepository.findByStatus("PUBLISHED", 
                        org.springframework.data.domain.PageRequest.of(currentPage, pageSize));
                
                var newsList = page.getContent();
                hasMore = page.hasNext();
                
                log.info("Processing page {} with {} news items", currentPage + 1, newsList.size());
                
                for (News news : newsList) {
                    try {
                        boolean hasSummary = summaryRepository.existsByNewsId(news.getId());
                        
                        if (!hasSummary || forceRegenerate) {
                            log.info("Generating summary for news {}: {}", news.getId(), news.getTitle());
                            generateSummary(news.getId());
                            successCount++;
                            
                            // 避免请求过快，释放资源
                            Thread.sleep(3000);
                        } else {
                            log.debug("Skipping news {} (already has summary)", news.getId());
                            skipCount++;
                        }
                    } catch (InterruptedException e) {
                        log.warn("Batch generation interrupted");
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Failed to generate summary for news {}: {}", 
                                news.getId(), e.getMessage());
                        failCount++;
                    }
                }
                
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Batch generation cancelled");
                    break;
                }
                
                currentPage++;
            }
            
            log.info("Batch summary generation completed: {} generated, {} skipped, {} failed", 
                    successCount, skipCount, failCount);
                    
        } catch (Exception e) {
            log.error("Batch summary generation failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查AI服务是否可用
     */
    public boolean isAIServiceAvailable() {
        return zhipuAIClient.isApiAvailable();
    }
}

