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
 * 新华网新闻爬虫
 */
@Component
@Slf4j
public class XinhuaNewsCrawler extends AbstractNewsCrawler {

    private static final String BASE_URL = "http://www.xinhuanet.com";
    private static final String LIST_PAGE_URL = "http://www.xinhuanet.com/";

    @Override
    public String getSourceName() {
        return "XINHUA";
    }

    @Override
    protected String getListPageUrl() {
        return LIST_PAGE_URL;
    }

    @Override
    protected List<String> parseNewsLinks(Document doc, int maxCount) {
        List<String> links = new ArrayList<>();
        
        try {
            // 从主页抓取新闻链接
            Elements newsElements = doc.select("a[href]");
            
            for (Element element : newsElements) {
                String href = element.attr("abs:href");
                
                // 新华网新闻链接特征
                if (href.contains("xinhuanet.com") && 
                    (href.contains("/c_") || href.contains("/20")) &&
                    href.endsWith(".htm") &&
                    !href.contains("javascript") &&
                    !href.contains("/photo/") &&  // 排除图片专题
                    !href.contains("/video/") &&  // 排除视频
                    !links.contains(href)) {
                    
                    links.add(href);
                    
                    if (links.size() >= maxCount) {
                        break;
                    }
                }
            }
            
            // 如果主页链接不够，尝试分类页
            if (links.size() < maxCount) {
                String[] categoryPages = {
                    "http://www.xinhuanet.com/politics/",   // 时政
                    "http://www.xinhuanet.com/fortune/",    // 财经
                    "http://www.xinhuanet.com/local/",      // 地方
                    "http://www.xinhuanet.com/world/"       // 国际
                };
                
                for (String categoryUrl : categoryPages) {
                    if (links.size() >= maxCount) break;
                    
                    try {
                        Document categoryDoc = fetchDocument(categoryUrl);
                        if (categoryDoc != null) {
                            Elements categoryNews = categoryDoc.select("a[href]");
                            
                            for (Element element : categoryNews) {
                                String href = element.attr("abs:href");
                                
                                if (href.contains("xinhuanet.com") && 
                                    (href.contains("/c_") || href.contains("/20")) &&
                                    href.endsWith(".htm") &&
                                    !href.contains("/photo/") &&
                                    !href.contains("/video/") &&
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
            
            log.info("Parsed {} news links from Xinhua", links.size());
            
        } catch (Exception e) {
            log.error("Failed to parse news links from Xinhua: {}", e.getMessage());
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
            Element titleElement = doc.selectFirst("h1.title, span.title");
            if (titleElement == null) {
                titleElement = doc.selectFirst("h1");
            }
            if (titleElement == null) {
                log.warn("No title found for {}", url);
                return null;
            }
            String title = cleanText(titleElement.text());

            // 提取内容
            Element contentElement = doc.selectFirst("div#detail, div.article, span#content");
            if (contentElement == null) {
                contentElement = doc.selectFirst("div.content");
            }
            if (contentElement == null) {
                log.warn("No content found for {}", url);
                return null;
            }
            
            String content = extractContentWithParagraphs(contentElement);

            // 提取发布时间
            LocalDateTime publishTime = LocalDateTime.now();
            Element timeElement = doc.selectFirst("span.time, div.info span");
            if (timeElement != null) {
                try {
                    String timeText = timeElement.text();
                    publishTime = parseDateTime(timeText);
                } catch (Exception e) {
                    log.warn("Failed to parse publish time: {}", e.getMessage());
                }
            }

            // 提取图片
            List<String> imageUrls = extractImageUrls(contentElement, 5);
            
            News news = News.builder()
                    .title(title)
                    .content(content)
                    .originalUrl(url)
                    .publishTime(publishTime)
                    .status("PUBLISHED")
                    .viewCount(0L)
                    .build();
            
            if (!imageUrls.isEmpty()) {
                news.setImageUrlList(imageUrls);
            }
            
            return news;

        } catch (Exception e) {
            log.error("Failed to parse news detail from {}: {}", url, e.getMessage());
            return null;
        }
    }

    private LocalDateTime parseDateTime(String timeText) {
        try {
            // 新华网时间格式：2025-10-31 08:30:00 或 2025年10月31日 08:30:00
            timeText = timeText.replaceAll("来源：.*", "").trim();
            
            // 尝试第一种格式
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(timeText, formatter);
            } catch (Exception e1) {
                // 尝试第二种格式
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
                return LocalDateTime.parse(timeText, formatter2);
            }
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}

