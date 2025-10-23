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

    // 来源预设规则映射：来源 -> 默认分类名称
    private static final Map<String, String> SOURCE_DEFAULT_CATEGORY = new HashMap<>();
    
    static {
        SOURCE_DEFAULT_CATEGORY.put("SINA", "社会");
        SOURCE_DEFAULT_CATEGORY.put("CCTV", "政治");
        SOURCE_DEFAULT_CATEGORY.put("NETEASE", "科技");
    }

    /**
     * 自动分类新闻
     * 策略：1. 优先使用来源预设规则  2. 使用关键词匹配  3. 默认分类
     */
    public Long classifyNews(News news) {
        log.info("Auto-classifying news: {}", news.getTitle());

        // 策略1：来源预设规则
        Long categoryId = classifyBySource(news.getSourceWebsite());
        if (categoryId != null) {
            log.info("Classified by source rule: {} -> categoryId {}", news.getSourceWebsite(), categoryId);
            return categoryId;
        }

        // 策略2：关键词匹配规则
        categoryId = classifyByKeywords(news.getTitle(), news.getContent());
        if (categoryId != null) {
            log.info("Classified by keyword rule: {} -> categoryId {}", news.getTitle(), categoryId);
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
        // 获取所有启用的关键词规则，按优先级排序
        List<ClassificationRule> rules = ruleRepository
                .findByRuleTypeAndIsEnabledOrderByPriorityAsc("KEYWORD", true);

        String text = (title + " " + content).toLowerCase();

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

