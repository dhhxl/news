package com.news.service;

import com.news.model.entity.ClassificationRule;
import com.news.model.entity.News;
import com.news.repository.CategoryRepository;
import com.news.repository.ClassificationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动分类服务
 * 实现来源规则 + 关键词匹配的混合分类策略
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassificationService {

    private final ClassificationRuleRepository ruleRepository;
    private final CategoryRepository categoryRepository;

    // 来源预设规则映射：来源 -> 默认分类名称（作为兜底策略）
    private static final Map<String, String> SOURCE_DEFAULT_CATEGORY = new HashMap<>();
    
    static {
        SOURCE_DEFAULT_CATEGORY.put("SINA", "社会");
        SOURCE_DEFAULT_CATEGORY.put("CCTV", "时政");
        SOURCE_DEFAULT_CATEGORY.put("NETEASE", "社会");
        SOURCE_DEFAULT_CATEGORY.put("PEOPLE", "时政");      // 人民网 - 时政
        SOURCE_DEFAULT_CATEGORY.put("XINHUA", "时政");      // 新华网 - 时政
    }

    /**
     * 自动分类新闻
     * 策略：1. 优先使用关键词匹配（最准确）2. 使用来源预设规则  3. 默认分类
     */
    public Long classifyNews(News news) {
        log.info("Auto-classifying news: {}", news.getTitle());

        // 策略1：关键词匹配规则（优先级最高）
        Long categoryId = classifyByKeywords(news.getTitle(), news.getContent());
        if (categoryId != null) {
            log.info("Classified by keyword rule: {} -> categoryId {}", news.getTitle(), categoryId);
            return categoryId;
        }

        // 策略2：来源预设规则（作为兜底）
        categoryId = classifyBySource(news.getSourceWebsite());
        if (categoryId != null) {
            log.info("Classified by source rule: {} -> categoryId {}", news.getSourceWebsite(), categoryId);
            return categoryId;
        }

        // 策略3：默认分类（社会）
        Long defaultCategoryId = categoryRepository.findByName("社会")
                .map(cat -> cat.getId())
                .orElse(1L); // 如果找不到"社会"分类，使用ID=1
        
        log.info("Using default category: {}", defaultCategoryId);
        return defaultCategoryId;
    }

    /**
     * 根据来源分类
     */
    private Long classifyBySource(String sourceWebsite) {
        String defaultCategoryName = SOURCE_DEFAULT_CATEGORY.get(sourceWebsite);
        if (defaultCategoryName != null) {
            return categoryRepository.findByName(defaultCategoryName)
                    .map(cat -> cat.getId())
                    .orElse(null);
        }
        return null;
    }

    /**
     * 根据关键词匹配分类
     */
    private Long classifyByKeywords(String title, String content) {
        String text = (title + " " + content).toLowerCase();
        
        // 内置高优先级关键词规则（覆盖常见场景）
        Long builtInCategoryId = classifyByBuiltInKeywords(text);
        if (builtInCategoryId != null) {
            return builtInCategoryId;
        }
        
        // 数据库配置的关键词规则
        List<ClassificationRule> rules = ruleRepository
                .findByRuleTypeAndIsEnabledOrderByPriorityAsc("KEYWORD", true);

        for (ClassificationRule rule : rules) {
            // rule.keywords 格式：关键词1,关键词2,关键词3
            if (rule.getKeywords() == null || rule.getKeywords().isEmpty()) {
                continue;
            }
            
            String[] keywordArray = rule.getKeywords().split(",");
            
            for (String keyword : keywordArray) {
                if (text.contains(keyword.trim().toLowerCase())) {
                    log.info("Matched keyword '{}' for categoryId {}", keyword.trim(), rule.getTargetCategoryId());
                    return rule.getTargetCategoryId();
                }
            }
        }

        return null;
    }
    
    /**
     * 内置关键词分类规则（高优先级）
     */
    private Long classifyByBuiltInKeywords(String text) {
        // 财经类关键词
        String[] financeKeywords = {"股份", "董事长", "ceo", "总裁", "上市", "股票", "证券", 
                "基金", "投资", "融资", "ipo", "财报", "季度", "营收", "利润", "亏损", 
                "市值", "股价", "交易所", "a股", "港股", "美股"};
        if (containsAny(text, financeKeywords)) {
            return getCategoryIdByName("经济");
        }
        
        // 科技类关键词
        String[] techKeywords = {"ai", "人工智能", "芯片", "半导体", "5g", "6g", "机器人", 
                "算法", "云计算", "大数据", "区块链", "量子", "航天", "卫星", "火箭"};
        if (containsAny(text, techKeywords)) {
            return getCategoryIdByName("科技");
        }
        
        // 体育类关键词
        String[] sportsKeywords = {"足球", "篮球", "nba", "世界杯", "奥运", "比赛", "冠军", 
                "球员", "教练", "运动员", "赛事", "联赛"};
        if (containsAny(text, sportsKeywords)) {
            return getCategoryIdByName("体育");
        }
        
        // 娱乐类关键词
        String[] entertainmentKeywords = {"演员", "明星", "电影", "电视剧", "综艺", "导演", 
                "票房", "上映", "音乐", "歌手", "演唱会", "颁奖"};
        if (containsAny(text, entertainmentKeywords)) {
            return getCategoryIdByName("娱乐");
        }
        
        // 时政类关键词
        String[] politicsKeywords = {"政府", "国务院", "主席", "总理", "部长", "会议", "政策", 
                "法律", "法规", "改革", "外交", "大使"};
        if (containsAny(text, politicsKeywords)) {
            return getCategoryIdByName("时政");
        }
        
        return null;
    }
    
    /**
     * 检查文本是否包含任意关键词
     */
    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据分类名称获取分类ID
     */
    private Long getCategoryIdByName(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .map(cat -> cat.getId())
                .orElse(null);
    }

    /**
     * 批量分类新闻
     */
    public void classifyNewsBatch(List<News> newsList) {
        for (News news : newsList) {
            if (news.getCategoryId() == null) {
                Long categoryId = classifyNews(news);
                news.setCategoryId(categoryId);
            }
        }
    }
}

