package com.news.repository;

import com.news.model.entity.ClassificationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分类规则Repository
 */
@Repository
public interface ClassificationRuleRepository extends JpaRepository<ClassificationRule, Long> {

    /**
     * 查找所有启用的规则，按优先级排序
     */
    @Query("SELECT r FROM ClassificationRule r WHERE r.isEnabled = true ORDER BY r.priority ASC")
    List<ClassificationRule> findAllEnabledRulesOrderByPriority();

    /**
     * 根据规则类型查找启用的规则
     */
    List<ClassificationRule> findByRuleTypeAndIsEnabledOrderByPriorityAsc(String ruleType, Boolean isEnabled);

    /**
     * 根据目标分类查找规则
     */
    List<ClassificationRule> findByTargetCategoryId(Long targetCategoryId);

    /**
     * 统计启用的规则数量
     */
    long countByIsEnabled(Boolean isEnabled);
}

