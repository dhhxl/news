# Technical Research: 新闻管理系统

**Date**: 2025-10-10  
**Status**: Complete  
**Purpose**: 解决实施计划中的技术选型和架构设计问题

## Research Topics

### 1. 新闻采集技术方案

**Decision**: 使用 JSoup + Spring Scheduler 实现多源新闻采集

**Rationale**:
- **JSoup 优势**：Java HTML解析器，API简单易用，支持CSS选择器和DOM遍历
- **Spring Scheduler**：轻量级定时任务，集成在Spring Boot中无需额外依赖
- **稳定性考虑**：三个新闻源（新浪、央视、网易）均为静态HTML渲染，无需处理复杂的JavaScript动态加载
- **容错机制**：单个源采集失败不影响其他源，使用@Async异步执行各源采集任务

**Alternatives Considered**:
- **Selenium/Playwright**：功能强大但资源消耗高，适合JavaScript渲染网站，本项目不需要
- **Scrapy（Python）**：需要集成Python环境，增加部署复杂度，与Spring Boot生态割裂
- **Quartz**：功能更强大的调度框架，但对于简单定时任务过于复杂，Spring Scheduler足够

**Implementation Guidelines**:
```java
// 新闻采集接口
public interface NewsCrawler {
    List<News> crawl();
    String getSourceName();
}

// 定时任务配置
@Scheduled(cron = "${crawler.cron.expression}")
public void scheduledCrawl() {
    crawlers.forEach(crawler -> 
        CompletableFuture.runAsync(() -> crawler.crawl())
    );
}
```

---

### 2. 智能排序算法设计

**Decision**: 使用时间衰减 + 浏览量加权的混合排序算法

**Rationale**:
- **业务需求**：平衡新鲜度和热度，避免旧的热门新闻长期占据首页
- **算法复杂度**：O(n log n)，适合中小规模数据集（每日500-1000条新闻）
- **可调参数**：时间衰减因子和浏览量权重可通过配置调整

**Algorithm Formula**:
```
Score = ViewCount * weight + TimeDecay
TimeDecay = e^(-λ * HoursSincePublish)

其中：
- ViewCount：浏览量
- weight：浏览量权重系数（默认0.3）
- λ：时间衰减系数（默认0.1）
- HoursSincePublish：发布后经过的小时数
```

**Alternatives Considered**:
- **纯时间排序**：过于简单，无法体现用户偏好
- **纯浏览量排序**：对新发布内容不公平，缺乏新鲜感
- **机器学习推荐**：对于初期用户量和数据量，投入产出比不高

**Implementation Guidelines**:
```sql
-- MySQL查询优化
SELECT *, 
    (view_count * 0.3 + EXP(-0.1 * TIMESTAMPDIFF(HOUR, publish_time, NOW()))) as score
FROM news
WHERE status = 'PUBLISHED'
ORDER BY score DESC
LIMIT 20;

-- 创建组合索引
CREATE INDEX idx_news_sorting ON news(status, publish_time, view_count);
```

---

### 3. 自动分类技术方案

**Decision**: 基于规则引擎的两阶段分类（来源规则 + 关键词匹配）

**Rationale**:
- **准确性**：来源规则准确率接近100%（央视政治频道 → 政治分类）
- **覆盖率**：关键词匹配作为兜底方案，覆盖来源规则无法处理的情况
- **可维护性**：规则存储在数据库，管理员可通过界面配置，无需修改代码
- **性能**：规则匹配在内存中完成，延迟<10ms

**Classification Flow**:
```
1. 采集新闻
2. 按优先级查询来源规则 (source_url LIKE '%pattern%')
3. 如果匹配，分配到目标分类 → 结束
4. 如果未匹配，执行关键词匹配
5. 查询所有关键词规则，计算匹配度
6. 选择匹配度最高的分类
7. 如果所有规则都不匹配，标记为"待分类"
```

**Alternatives Considered**:
- **机器学习分类（NLP）**：需要训练数据和模型维护，初期投入大，70%准确率目标用规则即可达到
- **外部分类API**：增加外部依赖和成本，且无法定制分类体系
- **纯关键词匹配**：准确率不稳定，来源规则提供了更可靠的基础

**Implementation Guidelines**:
```java
public Category classifyNews(News news) {
    // Phase 1: 来源规则匹配
    Optional<ClassificationRule> sourceRule = ruleRepository
        .findByTypeAndEnabledOrderByPriorityAsc("SOURCE", true)
        .stream()
        .filter(rule -> news.getSourceUrl().contains(rule.getPattern()))
        .findFirst();
    
    if (sourceRule.isPresent()) {
        return sourceRule.get().getTargetCategory();
    }
    
    // Phase 2: 关键词匹配
    Map<Category, Integer> scores = new HashMap<>();
    List<ClassificationRule> keywordRules = ruleRepository
        .findByTypeAndEnabled("KEYWORD", true);
    
    for (ClassificationRule rule : keywordRules) {
        int score = calculateKeywordMatch(news, rule.getKeywords());
        scores.merge(rule.getTargetCategory(), score, Integer::sum);
    }
    
    return scores.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(defaultCategory); // "待分类"
}
```

---

### 4. 智谱AI集成方案

**Decision**: 使用 RestTemplate + 重试机制调用智谱AI API

**Rationale**:
- **官方支持**：智谱AI提供RESTful API，兼容性好
- **Spring生态**：RestTemplate是Spring标准HTTP客户端，与Spring Boot无缝集成
- **容错机制**：结合Spring Retry实现自动重试，提高成功率
- **性能优化**：异步调用，避免阻塞主线程

**API Integration**:
```java
@Service
public class ZhipuAiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${zhipuai.api.key}")
    private String apiKey;
    
    @Value("${zhipuai.api.url}")
    private String apiUrl;
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public String generateSummary(String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> request = Map.of(
            "model", "glm-4",
            "messages", List.of(
                Map.of("role", "user", 
                       "content", "请为以下新闻生成100字以内的摘要：\n" + content)
            ),
            "max_tokens", 200
        );
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<ZhipuAiResponse> response = 
            restTemplate.postForEntity(apiUrl, entity, ZhipuAiResponse.class);
        
        return response.getBody().getChoices().get(0).getMessage().getContent();
    }
}
```

**Alternatives Considered**:
- **SDK方式**：官方Java SDK可能存在版本兼容问题，RestTemplate更稳定
- **同步调用**：会阻塞用户请求，异步调用用户体验更好

---

### 5. 认证授权方案

**Decision**: JWT Token + Spring Security

**Rationale**:
- **无状态**：JWT Token存储在客户端，服务端无需维护Session，便于水平扩展
- **安全性**：Token包含签名，防止篡改；设置30分钟过期时间，限制滥用风险
- **Spring Security集成**：标准方案，社区支持好，文档齐全

**Authentication Flow**:
```
1. 用户提交用户名密码
2. 后端验证凭证（BCrypt密码加密）
3. 生成JWT Token（包含用户ID、角色、过期时间）
4. 返回Token给前端
5. 前端存储Token（LocalStorage）
6. 后续请求在Header中携带Token
7. Spring Security拦截器验证Token
8. 解析用户信息，设置SecurityContext
```

**Alternatives Considered**:
- **Session-Cookie**：需要后端存储Session状态，不利于分布式部署
- **OAuth2**：对于内部管理系统过于复杂，JWT足够

**Implementation Guidelines**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/news/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

### 6. 缓存策略

**Decision**: Redis缓存热点新闻列表 + Spring Cache抽象层

**Rationale**:
- **性能提升**：新闻列表查询频繁，缓存可减少80%数据库压力
- **缓存时长**：5分钟，平衡实时性和性能
- **缓存粒度**：按分类和页码缓存，缓存键：`news:list:{categoryId}:{page}`
- **缓存失效**：新闻发布/编辑/删除时清除相关缓存

**Caching Strategy**:
```java
@Service
public class NewsService {
    
    @Cacheable(value = "newsList", key = "#categoryId + ':' + #page")
    public Page<News> getNewsList(Long categoryId, int page) {
        // 查询数据库并计算智能排序
    }
    
    @CacheEvict(value = "newsList", allEntries = true)
    public void clearNewsCache() {
        // 新闻变更时清空所有缓存
    }
}
```

**Alternatives Considered**:
- **本地缓存（Caffeine）**：在分布式环境下需要考虑缓存同步问题
- **不使用缓存**：对于100+并发用户，数据库压力过大

---

### 7. 数据库性能优化

**Decision**: 索引优化 + 分页查询 + 读写分离预留

**Key Optimizations**:
1. **索引策略**：
   - 主键索引：`id`
   - 唯一索引：`title`（用于重复检测）
   - 组合索引：`(status, publish_time, view_count)`（用于智能排序）
   - 外键索引：`category_id`, `user_id`

2. **分页查询**：
   - 使用Spring Data JPA的Pageable
   - 避免深分页问题（limit 10000, 10会慢）
   - 使用游标分页（基于上次查询的最后ID）

3. **查询优化**：
   - 避免SELECT *，只查询需要的字段
   - 使用JOIN减少N+1查询问题
   - 使用EXPLAIN分析慢查询

4. **读写分离预留**：
   - 使用Spring AbstractRoutingDataSource
   - 主库写，从库读
   - 当前阶段单库，预留扩展能力

---

### 8. 前端性能优化

**Decision**: 路由懒加载 + 组件按需加载 + 图片懒加载

**Key Optimizations**:
1. **路由懒加载**：
```typescript
const routes = [
    {
        path: '/admin',
        component: () => import('./views/admin/Dashboard.vue')
    }
]
```

2. **组件库按需引入**：
```typescript
// vite.config.ts
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default {
    plugins: [
        AutoImport({
            resolvers: [ElementPlusResolver()],
        }),
        Components({
            resolvers: [ElementPlusResolver()],
        }),
    ],
}
```

3. **图片懒加载**：
```typescript
// 使用 vue-lazyload 或 Intersection Observer
<img v-lazy="news.imageUrl" alt="news image" />
```

4. **代码分割**：
   - Vite自动进行代码分割
   - 手动分割大型第三方库

---

## Technology Stack Summary

| Category | Technology | Version | Justification |
|----------|-----------|---------|---------------|
| **后端核心** | Spring Boot | 3.2.x | 企业级Java框架，生态成熟 |
| **前端核心** | Vue 3 | 3.4.x | 响应式框架，组件化开发 |
| **数据库** | MySQL | 8.0.x | 关系型数据库，事务支持 |
| **缓存** | Redis | 7.x | 高性能KV存储，支持多种数据结构 |
| **爬虫** | JSoup | 1.17.x | Java HTML解析器，API简单 |
| **AI集成** | 智谱AI | GLM-4 | 中文NLP能力强，API稳定 |
| **认证** | JWT + Spring Security | - | 无状态认证，水平扩展友好 |
| **UI组件** | Element Plus | 2.5.x | Vue 3生态，组件丰富 |
| **构建工具** | Vite | 5.x | 极速构建，开发体验好 |
| **测试框架** | JUnit 5 + Vitest | - | 单元测试 + 集成测试覆盖 |

---

## Risk Assessment

| Risk | Severity | Mitigation |
|------|----------|------------|
| 新闻源网站结构变更 | High | 定期监控采集成功率，错误日志告警；提供管理员手动配置选择器的能力 |
| 智谱AI限流或停服 | Medium | 实现重试机制；提供降级方案（显示原文摘要） |
| 数据库性能瓶颈 | Medium | 缓存热点数据；预留读写分离和分库分表能力 |
| 并发编辑冲突 | Low | 使用乐观锁（version字段）检测冲突 |
| 恶意请求（DDoS） | Low | 实现限流（Spring Cloud Gateway + Redis） |

---

## Next Steps

研究阶段完成，所有技术选型已确定。下一步：
1. 设计详细的数据库Schema（data-model.md）
2. 定义RESTful API契约（contracts/）
3. 编写开发快速上手指南（quickstart.md）

