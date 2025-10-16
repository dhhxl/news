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
 * 央视新闻爬虫
 */
@Component
@Slf4j
public class CctvNewsCrawler extends AbstractNewsCrawler {

    private static final String BASE_URL = "https://news.cctv.com";
    private static final String LIST_PAGE_URL = "https://news.cctv.com/2019/07/gaiban/cmsdatainterface/page/news_1.jsonp";

    @Override
    public String getSourceName() {
        return "CCTV";
    }

    @Override
    protected String getListPageUrl() {
        return "https://news.cctv.com/"; // 使用主页
    }

    @Override
    protected List<String> parseNewsLinks(Document doc, int maxCount) {
        List<String> links = new ArrayList<>();
        
        try {
            // 方案1: 从主页抓取
            Elements newsElements = doc.select("a[href*=cctv.com]");
            
            for (Element element : newsElements) {
                String href = element.attr("href");
                
                // 过滤掉非新闻链接
                if (href.contains("/news.cctv.com/") || 
                    href.contains("/m.news.cctv.com/")) {
                    
                    String fullUrl = href.startsWith("http") ? href : buildFullUrl(BASE_URL, href);
                    
                    // 确保是新闻详情页
                    if (fullUrl.matches(".*\\d{4}/\\d{2}/\\d{2}/.*") && 
                        !links.contains(fullUrl)) {
                        links.add(fullUrl);
                    }
                    
                    if (links.size() >= maxCount) {
                        break;
                    }
                }
            }
            
            // 方案2: 如果主页链接不够，尝试访问其他分类页
            if (links.size() < maxCount) {
                log.info("Main page only has {} links, trying category pages...", links.size());
                
                String[] categoryPages = {
                    "https://news.cctv.com/china/",      // 国内
                    "https://news.cctv.com/world/",      // 国际
                    "https://news.cctv.com/society/",    // 社会
                    "https://news.cctv.com/politics/"    // 政治
                };
                
                for (String categoryUrl : categoryPages) {
                    if (links.size() >= maxCount) break;
                    
                    try {
                        Document categoryDoc = fetchDocument(categoryUrl);
                        if (categoryDoc != null) {
                            Elements categoryNews = categoryDoc.select("a[href*=news.cctv.com]");
                            
                            for (Element element : categoryNews) {
                                String href = element.attr("abs:href");
                                
                                if (href.matches(".*/20\\d{2}/.*") && 
                                    !href.contains("javascript") &&
                                    !href.endsWith(".jpg") &&
                                    !href.endsWith(".png") &&
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
            
            log.info("Parsed {} news links from CCTV (from main page and category pages)", links.size());
            
        } catch (Exception e) {
            log.error("Failed to parse news links from CCTV: {}", e.getMessage());
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
            Element titleElement = doc.selectFirst("div.title h1, h1");
            if (titleElement == null) {
                log.warn("No title found for {}", url);
                return null;
            }
            String title = cleanText(titleElement.text());

            // 提取内容
            Element contentElement = doc.selectFirst("div.content_area, div.cnt_bd");
            if (contentElement == null) {
                log.warn("No content found for {}", url);
                return null;
            }
            
            // 清理内容
            contentElement.select("script, style, .function_code").remove();
            String content = cleanText(contentElement.text());

            // 提取发布时间
            LocalDateTime publishTime = LocalDateTime.now();
            Element timeElement = doc.selectFirst("div.info span.time, span.info_time");
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
            // 央视时间格式示例：2025年10月10日 15:30:00
            timeText = timeText.replaceAll("来源：.*", "").trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
            return LocalDateTime.parse(timeText, formatter);
        } catch (Exception e) {
            // 尝试其他格式：2025-10-10 15:30:00
            try {
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(timeText, formatter2);
            } catch (Exception e2) {
                return LocalDateTime.now();
            }
        }
    }
}

