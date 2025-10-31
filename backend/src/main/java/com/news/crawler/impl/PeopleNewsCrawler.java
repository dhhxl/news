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
 * 人民网新闻爬虫
 */
@Component
@Slf4j
public class PeopleNewsCrawler extends AbstractNewsCrawler {

    private static final String BASE_URL = "http://www.people.com.cn";
    private static final String LIST_PAGE_URL = "http://www.people.com.cn/";

    @Override
    public String getSourceName() {
        return "PEOPLE";
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
                
                // 人民网新闻链接特征：包含people.com.cn/n1/年份
                if (href.contains("people.com.cn/n1/") && 
                    href.endsWith(".html") &&
                    !href.contains("javascript") &&
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
                    "http://politics.people.com.cn/",    // 时政
                    "http://finance.people.com.cn/",     // 财经
                    "http://society.people.com.cn/",     // 社会
                    "http://world.people.com.cn/"        // 国际
                };
                
                for (String categoryUrl : categoryPages) {
                    if (links.size() >= maxCount) break;
                    
                    try {
                        Document categoryDoc = fetchDocument(categoryUrl);
                        if (categoryDoc != null) {
                            Elements categoryNews = categoryDoc.select("a[href*=/n1/]");
                            
                            for (Element element : categoryNews) {
                                String href = element.attr("abs:href");
                                
                                if (href.contains("people.com.cn/n1/") && 
                                    href.endsWith(".html") &&
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
            
            log.info("Parsed {} news links from People", links.size());
            
        } catch (Exception e) {
            log.error("Failed to parse news links from People: {}", e.getMessage());
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
            Element titleElement = doc.selectFirst("h1, div.title h1");
            if (titleElement == null) {
                log.warn("No title found for {}", url);
                return null;
            }
            String title = cleanText(titleElement.text());

            // 提取内容
            Element contentElement = doc.selectFirst("div.rm_txt_con, div.show_text, div.box_con");
            if (contentElement == null) {
                log.warn("No content found for {}", url);
                return null;
            }
            
            String content = extractContentWithParagraphs(contentElement);

            // 提取发布时间
            LocalDateTime publishTime = LocalDateTime.now();
            Element timeElement = doc.selectFirst("div.box01 div.fl, div.show_author");
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
            // 人民网时间格式：2025年10月31日08:30
            timeText = timeText.replaceAll("来源：.*", "").trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日HH:mm");
            return LocalDateTime.parse(timeText, formatter);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}

