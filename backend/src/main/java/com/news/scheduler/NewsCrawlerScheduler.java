package com.news.scheduler;

import com.news.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 新闻爬虫定时任务调度器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsCrawlerScheduler {

    private final CrawlerService crawlerService;

    @Value("${crawler.schedule.enabled:true}")
    private boolean scheduleEnabled;

    @Value("${crawler.max-count-per-source:10}")
    private int maxCountPerSource;

    /**
     * 定时爬取任务
     * 每小时执行一次
     */
    @Scheduled(cron = "${crawler.schedule.cron:0 0 * * * ?}")
    public void scheduledCrawl() {
        if (!scheduleEnabled) {
            log.info("Crawler schedule is disabled");
            return;
        }

        log.info("=== Scheduled crawl task started ===");
        crawlerService.executeAllCrawlers(maxCountPerSource);
        log.info("=== Scheduled crawl task completed ===");
    }

    /**
     * 每天凌晨2点执行完整爬取
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyFullCrawl() {
        if (!scheduleEnabled) {
            return;
        }

        log.info("=== Daily full crawl started ===");
        crawlerService.executeAllCrawlers(50); // 每日任务爬取更多
        log.info("=== Daily full crawl completed ===");
    }
}

