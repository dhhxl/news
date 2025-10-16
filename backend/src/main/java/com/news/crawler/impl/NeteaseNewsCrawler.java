package com.news.crawler.impl;

import com.news.crawler.AbstractNewsCrawler;
import com.news.model.entity.News;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 网易新闻爬虫
 */
@Component
@Slf4j
public class NeteaseNewsCrawler extends AbstractNewsCrawler {

    private static final String BASE_URL = "https://news.163.com";
    private static final String LIST_PAGE_URL = "https://news.163.com/";

    @Override
    public String getSourceName() {
        return "NETEASE";
    }

    @Override
    protected String getListPageUrl() {
        return LIST_PAGE_URL;
    }

    @Override
    protected List<String> parseNewsLinks(Document doc, int maxCount) {
        List<String> links = new ArrayList<>();
        
        try {
            // 方案1: 从主页抓取
            Elements newsElements = doc.select("a[href*=news.163.com]");
            
            for (Element element : newsElements) {
                String href = element.attr("href");
                
                // 放宽过滤条件：只要是news.163.com的html页面即可
                if (href.contains("news.163.com") && 
                    href.endsWith(".html") &&
                    href.contains("20") &&  // 包含年份标识
                    !href.contains("javascript") &&
                    !links.contains(href)) {
                    
                    String fullUrl = href.startsWith("http") ? href : buildFullUrl(BASE_URL, href);
                    links.add(fullUrl);
                    
                    if (links.size() >= maxCount) {
                        break;
                    }
                }
            }
            
            // 方案2: 如果主页链接不够，访问分类页
            if (links.size() < maxCount) {
                log.info("Main page only has {} links, trying category pages...", links.size());
                
                String[] categoryPages = {
                    "https://news.163.com/domestic/",     // 国内
                    "https://news.163.com/world/",        // 国际
                    "https://news.163.com/society/",      // 社会
                    "https://money.163.com/"              // 财经
                };
                
                for (String categoryUrl : categoryPages) {
                    if (links.size() >= maxCount) break;
                    
                    try {
                        Document categoryDoc = fetchDocument(categoryUrl);
                        if (categoryDoc != null) {
                            Elements categoryNews = categoryDoc.select("a[href*=163.com]");
                            
                            for (Element element : categoryNews) {
                                String href = element.attr("abs:href");
                                
                                if (href.contains("163.com") && 
                                    href.endsWith(".html") &&
                                    href.contains("20") &&
                                    !href.contains("javascript") &&
                                    !links.contains(href)) {
                                    links.add(href);
                                    
                                    if (links.size() >= maxCount) {
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch from category page {}: {}", categoryUrl, e.getMessage());
                    }
                }
            }
            
            log.info("Parsed {} news links from Netease (from main page and category pages)", links.size());
            
        } catch (Exception e) {
            log.error("Failed to parse news links from Netease: {}", e.getMessage());
        }

        return links;
    }

    @Override
    protected News parseNewsDetail(String url) {
        try {
            Document doc = fetchDocument(url);
            if (doc == null) {
                return null;
            }

            // 提取标题
            Element titleElement = doc.selectFirst("h1.post_title, h1");
            if (titleElement == null) {
                log.warn("No title found for {}", url);
                return null;
            }
            String title = cleanText(titleElement.text());

            // 提取内容
            Element contentElement = doc.selectFirst("div.post_body, div.post_text");
            if (contentElement == null) {
                log.warn("No content found for {}", url);
                return null;
            }
            
            // 清理内容
            contentElement.select("script, style, .ep-source").remove();
            String content = cleanText(contentElement.text());

            // 提取发布时间
            LocalDateTime publishTime = LocalDateTime.now();
            Element timeElement = doc.selectFirst("div.post_time_source, span.post_time");
            if (timeElement != null) {
                try {
                    String timeText = timeElement.text();
                    publishTime = parseDateTime(timeText);
                } catch (Exception e) {
                    log.warn("Failed to parse publish time: {}", e.getMessage());
                }
            }

            return News.builder()
                    .title(title)
                    .content(content)
                    .originalUrl(url)
                    .publishTime(publishTime)
                    .status("PUBLISHED")
                    .viewCount(0L)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse news detail from {}: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * 解析时间字符串
     */
    private LocalDateTime parseDateTime(String timeText) {
        try {
            // 网易时间格式示例：2025-10-10 15:30:00
            timeText = timeText.replaceAll("来源：.*", "").trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(timeText, formatter);
        } catch (Exception e) {
            // 尝试其他格式
            try {
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return LocalDateTime.parse(timeText, formatter2);
            } catch (Exception e2) {
                return LocalDateTime.now();
            }
        }
    }
}

