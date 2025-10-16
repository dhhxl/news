package com.news.controller;

import com.news.model.entity.ClassificationRule;
import com.news.repository.ClassificationRuleRepository;
import com.news.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类规则Controller
 */
@RestController
@RequestMapping("/classification-rules")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')") // 所有操作需要管理员权限
public class ClassificationRuleController {

    private final ClassificationRuleRepository ruleRepository;

    /**
     * 创建分类规则
     */
    @PostMapping
    public ResponseEntity<ClassificationRule> createRule(@Valid @RequestBody ClassificationRule rule) {
        log.info("Create classification rule request: type={}, target={}", 
                rule.getRuleType(), rule.getTargetCategoryId());
        
        ClassificationRule created = ruleRepository.save(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新分类规则
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClassificationRule> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody ClassificationRule rule) {
        log.info("Update classification rule request: {}", id);
        
        ClassificationRule existing = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("规则不存在，ID: " + id));
        
        existing.setRuleType(rule.getRuleType());
        existing.setSourcePattern(rule.getSourcePattern());
        existing.setKeywords(rule.getKeywords());
        existing.setTargetCategoryId(rule.getTargetCategoryId());
        existing.setPriority(rule.getPriority());
        existing.setIsEnabled(rule.getIsEnabled());
        
        ClassificationRule updated = ruleRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除分类规则
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        log.info("Delete classification rule request: {}", id);
        
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("规则不存在，ID: " + id);
        }
        
        ruleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID获取规则
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassificationRule> getRuleById(@PathVariable Long id) {
        ClassificationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("规则不存在，ID: " + id));
        return ResponseEntity.ok(rule);
    }

    /**
     * 获取所有规则
     */
    @GetMapping
    public ResponseEntity<List<ClassificationRule>> getAllRules() {
        List<ClassificationRule> rules = ruleRepository.findAll();
        return ResponseEntity.ok(rules);
    }

    /**
     * 获取所有启用的规则（按优先级排序）
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<ClassificationRule>> getEnabledRules() {
        List<ClassificationRule> rules = ruleRepository.findAllEnabledRulesOrderByPriority();
        return ResponseEntity.ok(rules);
    }

    /**
     * 根据类型获取规则
     */
    @GetMapping("/type/{ruleType}")
    public ResponseEntity<List<ClassificationRule>> getRulesByType(@PathVariable String ruleType) {
        List<ClassificationRule> rules = ruleRepository.findByRuleTypeAndIsEnabledOrderByPriorityAsc(
                ruleType, true);
        return ResponseEntity.ok(rules);
    }
}

