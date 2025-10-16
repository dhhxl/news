package com.news.crawler;

import com.news.model.entity.News;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽象爬虫基类
 * 提供通用的爬取逻辑和工具方法
 */
@Slf4j
public abstract class AbstractNewsCrawler implements NewsCrawler {

    protected static final int TIMEOUT = 10000; // 10秒超时
    protected static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * 获取列表页URL
     */
    protected abstract String getListPageUrl();

    /**
     * 解析列表页，提取新闻链接
     */
    protected abstract List<String> parseNewsLinks(Document doc, int maxCount);

    /**
     * 解析单篇新闻详情
     */
    protected abstract News parseNewsDetail(String url);

    @Override
    public List<News> crawlNews(int maxCount) {
        List<News> newsList = new ArrayList<>();
        
        try {
            log.info("Starting crawl from {}, max count: {}", getSourceName(), maxCount);
            
            // 1. 获取列表页
            Document listPage = fetchDocument(getListPageUrl());
            if (listPage == null) {
                log.error("Failed to fetch list page from {}", getSourceName());
                return newsList;
            }

            // 2. 提取新闻链接
            List<String> newsLinks = parseNewsLinks(listPage, maxCount);
            log.info("Found {} news links from {}", newsLinks.size(), getSourceName());

            // 3. 爬取每篇新闻详情
            for (String link : newsLinks) {
                try {
                    News news = parseNewsDetail(link);
                    if (news != null) {
                        news.setSourceWebsite(getSourceName());
                        news.setCrawlTime(LocalDateTime.now());
                        news.setClassificationMethod("AUTO");
                        newsList.add(news);
                        
                        // 避免请求过快
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    log.error("Failed to parse news from {}: {}", link, e.getMessage());
                }
                
                // 达到最大数量则停止
                if (newsList.size() >= maxCount) {
                    break;
                }
            }

            log.info("Successfully crawled {} news from {}", newsList.size(), getSourceName());
            
        } catch (Exception e) {
            log.error("Crawl error from {}: {}", getSourceName(), e.getMessage(), e);
        }

        return newsList;
    }

    @Override
    public boolean testConnection() {
        try {
            Document doc = fetchDocument(getListPageUrl());
            return doc != null;
        } catch (Exception e) {
            log.error("Connection test failed for {}: {}", getSourceName(), e.getMessage());
            return false;
        }
    }

    /**
     * 获取网页文档
     */
    protected Document fetchDocument(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();
        } catch (Exception e) {
            log.error("Failed to fetch document from {}: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 清理文本（去除多余空白）
     */
    protected String cleanText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    /**
     * 构建完整URL
     */
    protected String buildFullUrl(String baseUrl, String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) {
            return "";
        }
        if (relativeUrl.startsWith("http")) {
            return relativeUrl;
        }
        if (relativeUrl.startsWith("/")) {
            return baseUrl + relativeUrl;
        }
        return baseUrl + "/" + relativeUrl;
    }
}

