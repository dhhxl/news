package com.news.crawler;

import com.news.model.entity.News;

import java.util.List;

/**
 * 新闻爬虫接口
 */
public interface NewsCrawler {

    /**
     * 获取爬虫来源名称
     */
    String getSourceName();

    /**
     * 爬取新闻
     * @param maxCount 最大爬取数量
     * @return 爬取到的新闻列表
     */
    List<News> crawlNews(int maxCount);

    /**
     * 测试爬虫是否可用
     */
    boolean testConnection();
}

