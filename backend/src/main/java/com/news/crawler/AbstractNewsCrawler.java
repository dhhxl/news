package com.news.crawler;

import com.news.model.entity.News;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

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
     * 提取带段落的内容
     * 保留段落结构，将HTML的p标签转换为换行符
     */
    protected String extractContentWithParagraphs(Element contentElement) {
        if (contentElement == null) {
            return "";
        }
        
        // 清理不需要的元素
        contentElement.select("script, style, .ep-source, .function_code, .ad, .advertisement").remove();
        
        // 获取所有段落元素
        Elements paragraphs = contentElement.select("p");
        StringBuilder content = new StringBuilder();
        
        if (paragraphs.size() > 0) {
            // 有段落标签，按段落提取
            for (Element p : paragraphs) {
                String paragraphText = p.text().trim();
                if (!paragraphText.isEmpty() && paragraphText.length() > 10) { // 过滤掉过短的段落
                    content.append(paragraphText).append("\n\n");
                }
            }
        } else {
            // 没有段落标签，尝试其他方式分段
            String rawText = contentElement.text();
            if (rawText != null && !rawText.isEmpty()) {
                // 按句号分段，每3-4句为一段
                String[] sentences = rawText.split("[。！？]");
                StringBuilder paragraph = new StringBuilder();
                int sentenceCount = 0;
                
                for (String sentence : sentences) {
                    sentence = sentence.trim();
                    if (!sentence.isEmpty() && sentence.length() > 5) {
                        paragraph.append(sentence).append("。");
                        sentenceCount++;
                        
                        // 每3-4句组成一段
                        if (sentenceCount >= 3 || paragraph.length() > 200) {
                            content.append(paragraph.toString().trim()).append("\n\n");
                            paragraph = new StringBuilder();
                            sentenceCount = 0;
                        }
                    }
                }
                
                // 添加剩余内容
                if (paragraph.length() > 0) {
                    content.append(paragraph.toString().trim()).append("\n\n");
                }
            }
        }
        
        String result = content.toString().trim();
        
        // 如果还是太长没有分段，强制分段
        if (result.length() > 500 && !result.contains("\n")) {
            result = result.replaceAll("([。！？])([^\\n])", "$1\n\n$2");
        }
        
        return result.isEmpty() ? cleanText(contentElement.text()) : result;
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

    /**
     * 提取文章中的所有图片URL
     * @param contentElement 内容元素
     * @param maxImages 最大图片数量
     * @return 图片URL列表
     */
    protected List<String> extractImageUrls(Element contentElement, int maxImages) {
        Set<String> imageUrls = new LinkedHashSet<>(); // 使用Set避免重复，LinkedHashSet保持顺序
        
        if (contentElement != null) {
            // 选择内容区域内的所有图片
            Elements imgElements = contentElement.select("img");
            
            for (Element img : imgElements) {
                String imageUrl = null;
                
                // 尝试获取图片URL的不同属性
                imageUrl = img.attr("abs:src");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = img.attr("src");
                }
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = img.attr("data-src"); // 懒加载图片
                }
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = img.attr("data-original"); // 另一种懒加载
                }
                
                // 验证URL有效性
                if (imageUrl != null && !imageUrl.isEmpty() && isValidImageUrl(imageUrl)) {
                    imageUrls.add(imageUrl);
                    
                    // 达到最大数量则停止
                    if (imageUrls.size() >= maxImages) {
                        break;
                    }
                }
            }
        }
        
        log.debug("Extracted {} images from content", imageUrls.size());
        return new ArrayList<>(imageUrls);
    }

    /**
     * 验证图片URL是否有效
     */
    private boolean isValidImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        // 过滤无效的URL
        url = url.toLowerCase();
        if (url.contains("logo") || url.contains("icon") || 
            url.contains("avatar") || url.contains("watermark") ||
            url.endsWith(".gif") && url.contains("loading")) {
            return false;
        }
        
        // 检查是否是图片文件
        return url.matches(".*\\.(jpg|jpeg|png|gif|webp|bmp).*");
    }
}

