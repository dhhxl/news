package com.news.service;

import com.news.model.entity.Category;
import com.news.repository.CategoryRepository;
import com.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取综合统计数据
     */
    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 新闻总数
        long totalNews = newsRepository.count();
        stats.put("totalNews", totalNews);

        // 已发布新闻数
        String publishedSql = "SELECT COUNT(*) FROM news WHERE status = 'PUBLISHED'";
        Long publishedCount = jdbcTemplate.queryForObject(publishedSql, Long.class);
        stats.put("publishedNews", publishedCount != null ? publishedCount : 0);

        // 分类总数
        long totalCategories = categoryRepository.count();
        stats.put("totalCategories", totalCategories);

        // 总浏览量
        String viewSql = "SELECT SUM(view_count) FROM news";
        Long totalViews = jdbcTemplate.queryForObject(viewSql, Long.class);
        stats.put("totalViews", totalViews != null ? totalViews : 0);

        // 今日新增
        String todaySql = "SELECT COUNT(*) FROM news WHERE DATE(crawl_time) = CURDATE()";
        Long todayNews = jdbcTemplate.queryForObject(todaySql, Long.class);
        stats.put("todayNews", todayNews != null ? todayNews : 0);

        // 今日浏览量（简化版：使用总浏览量的一个比例）
        stats.put("todayViews", totalViews != null ? totalViews / 30 : 0);

        // 待审核数量
        String pendingSql = "SELECT COUNT(*) FROM news WHERE status IN ('PENDING', 'REVIEWING')";
        Long pendingCount = jdbcTemplate.queryForObject(pendingSql, Long.class);
        stats.put("pendingReviews", pendingCount != null ? pendingCount : 0);

        return stats;
    }

    /**
     * 获取分类分布统计
     */
    public Map<String, Object> getCategoryDistribution() {
        Map<String, Object> result = new HashMap<>();

        // 获取所有分类
        List<Category> categories = categoryRepository.findAll();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 统计每个分类的新闻数量
        String sql = "SELECT category_id, COUNT(*) as count FROM news WHERE status = 'PUBLISHED' GROUP BY category_id";

        List<Map<String, Object>> distribution = jdbcTemplate.queryForList(sql);

        List<String> names = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Map<String, Object> row : distribution) {
            Long categoryId = ((Number) row.get("category_id")).longValue();
            Long count = ((Number) row.get("count")).longValue();

            String categoryName = categoryMap.getOrDefault(categoryId, "未知分类");
            names.add(categoryName);
            values.add(count);
        }

        result.put("names", names);
        result.put("values", values);

        return result;
    }

    /**
     * 获取新闻来源分布统计
     */
    public Map<String, Object> getSourceDistribution() {
        Map<String, Object> result = new HashMap<>();

        String sql = "SELECT source_website, COUNT(*) as count FROM news " +
                "WHERE status = 'PUBLISHED' GROUP BY source_website ORDER BY count DESC";

        List<Map<String, Object>> distribution = jdbcTemplate.queryForList(sql);

        List<String> sources = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        // 来源名称映射
        Map<String, String> sourceNameMap = new HashMap<>();
        sourceNameMap.put("CCTV", "央视新闻");
        sourceNameMap.put("NETEASE", "网易新闻");
        sourceNameMap.put("SINA", "新浪新闻");
        sourceNameMap.put("PEOPLE", "人民网");
        sourceNameMap.put("XINHUA", "新华网");

        for (Map<String, Object> row : distribution) {
            String source = (String) row.get("source_website");
            Long count = ((Number) row.get("count")).longValue();

            String sourceName = sourceNameMap.getOrDefault(source, source);
            sources.add(sourceName);
            counts.add(count);
        }

        result.put("sources", sources);
        result.put("counts", counts);

        return result;
    }

    /**
     * 获取新闻趋势数据（最近7天）
     */
    public Map<String, Object> getNewsTrend() {
        Map<String, Object> result = new HashMap<>();

        // 获取最近7天的日期
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));

            // 查询该日期的新闻数量
            String sql = "SELECT COUNT(*) FROM news WHERE DATE(publish_time) = ?";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, date);
            counts.add(count != null ? count : 0);
        }

        result.put("dates", dates);
        result.put("counts", counts);

        return result;
    }

    /**
     * 获取浏览量趋势数据（最近7天）
     */
    public Map<String, Object> getViewTrend() {
        Map<String, Object> result = new HashMap<>();

        List<String> dates = new ArrayList<>();
        List<Long> views = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // 由于没有按日期记录浏览量，这里使用模拟数据
        // 实际项目中应该有一个浏览日志表来记录每日浏览量
        Random random = new Random(System.currentTimeMillis());

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));

            // 模拟浏览量数据（基于当天发布的新闻数量）
            String sql = "SELECT COUNT(*) * 50 FROM news WHERE DATE(publish_time) = ?";
            Long baseViews = jdbcTemplate.queryForObject(sql, Long.class, date);
            Long viewCount = (baseViews != null ? baseViews : 100) + random.nextInt(500);
            views.add(viewCount);
        }

        result.put("dates", dates);
        result.put("views", views);

        return result;
    }
}

