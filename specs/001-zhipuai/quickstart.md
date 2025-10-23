# Quick Start Guide: 新闻管理系统

**目标**: 帮助开发人员快速搭建本地开发环境并运行项目

## 前置条件

### 必需工具
- **JDK**: Java 17 或更高版本
- **Node.js**: v18.x 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 7.x 或更高版本（可选，用于缓存）
- **Git**: 最新版本
- **IDE**: IntelliJ IDEA（后端）+ VS Code（前端）推荐

### 可选工具
- **Docker**: 用于容器化部署
- **Postman**: API测试
- **Maven**: 3.8+（如IDE未集成）
- **npm/pnpm**: 包管理器

---

## 环境安装

### 1. 安装Java 17

**Windows**:
```powershell
# 使用Scoop安装
scoop install openjdk17

# 验证安装
java -version
```

**macOS/Linux**:
```bash
# 使用SDKMAN安装
curl -s "https://get.sdkman.io" | bash
sdk install java 17.0.10-tem

# 验证安装
java -version
```

### 2. 安装Node.js

**推荐使用nvm管理Node版本**:
```bash
# 安装nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash

# 安装Node.js 18
nvm install 18
nvm use 18

# 验证安装
node -v
npm -v
```

### 3. 安装MySQL 8.0

**使用Docker（推荐）**:
```bash
docker run --name news-mysql \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=news_db \
  -p 3306:3306 \
  -d mysql:8.0
```

**手动安装**: 参考 [MySQL官方文档](https://dev.mysql.com/doc/refman/8.0/en/installing.html)

### 4. 安装Redis（可选）

```bash
docker run --name news-redis \
  -p 6379:6379 \
  -d redis:7-alpine
```

---

## 项目初始化

### Step 1: 克隆仓库

```bash
git clone https://github.com/your-org/news-website.git
cd news-website
```

### Step 2: 创建数据库

```sql
CREATE DATABASE news_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建数据库用户（可选）
CREATE USER 'news_user'@'localhost' IDENTIFIED BY 'news_password';
GRANT ALL PRIVILEGES ON news_db.* TO 'news_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 3: 配置后端

**修改配置文件** `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/news_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    
  jpa:
    hibernate:
      ddl-auto: validate  # 使用Flyway管理schema，禁止Hibernate自动建表
    show-sql: true
    
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    
  redis:
    host: localhost
    port: 6379
    # password: (如果Redis设置了密码)
    
# 智谱AI配置
zhipuai:
  api:
    key: 6cb2e767c736258cb7159c0788895b7b.auiS63BgsJAujZTZ  # 请替换为真实的API Key
    url: https://open.bigmodel.cn/api/paas/v4/chat/completions
    
# JWT配置
jwt:
  secret: your-jwt-secret-key-min-256-bits  # 生产环境请使用复杂密钥
  expiration: 1800  # Token过期时间（秒），30分钟
```

### Step 4: 配置前端

**修改配置文件** `frontend/.env.development`:

```env
# API Base URL
VITE_API_BASE_URL=http://localhost:8080/api

# 应用标题
VITE_APP_TITLE=新闻管理系统

# 其他配置
VITE_PAGE_SIZE=20
```

---

## 启动项目

### 后端启动

**方式一：使用IDE（IntelliJ IDEA）**

1. 打开 `backend` 目录作为Maven项目
2. 等待依赖下载完成
3. 找到 `com.news.NewsApplication` 主类
4. 右键 → Run 'NewsApplication'
5. 访问 http://localhost:8080/swagger-ui.html 查看API文档

**方式二：使用Maven命令行**

```bash
cd backend

# 安装依赖并编译
mvn clean install -DskipTests

# 运行应用
mvn spring-boot:run

# 或者运行打包后的jar
java -jar target/news-backend-1.0.0.jar
```

**启动成功标志**:
```
Started NewsApplication in 8.5 seconds
```

### 前端启动

```bash
cd frontend

# 安装依赖
npm install
# 或使用pnpm（更快）
pnpm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

**启动成功标志**:
```
VITE v5.0.0  ready in 1234 ms

➜  Local:   http://localhost:5173/
➜  Network: http://192.168.1.100:5173/
```

---

## 数据库迁移

项目使用Flyway自动管理数据库Schema版本。

### 查看迁移状态

```bash
cd backend
mvn flyway:info
```

### 手动执行迁移

```bash
mvn flyway:migrate
```

### 迁移脚本位置

```
backend/src/main/resources/db/migration/
├── V1__init_schema.sql              # 初始化表结构
├── V2__add_default_categories.sql   # 插入默认分类
├── V3__add_default_admin.sql        # 插入默认管理员
└── V4__add_indexes.sql              # 性能优化索引
```

---

## 初始数据

### 默认管理员账户

首次启动后，系统会自动创建默认管理员账户：

- **用户名**: `admin`
- **密码**: `admin123`

**⚠️ 重要**: 生产环境部署前请立即修改默认密码！

### 默认分类

系统预置6个新闻分类：
- 政治
- 经济
- 体育
- 娱乐
- 科技
- 社会

---

## 功能测试

### 1. 登录测试

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**期望响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 1800,
    "user": {
      "id": 1,
      "username": "admin",
      "role": "ADMIN"
    }
  }
}
```

### 2. 创建新闻测试

```bash
curl -X POST http://localhost:8080/api/admin/news \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试新闻标题",
    "content": "这是一条测试新闻的正文内容，至少需要50个字符才能通过验证。测试新闻的正文内容继续添加...",
    "categoryId": 1,
    "sourceName": "手动录入",
    "sourceUrl": "https://example.com",
    "status": "PUBLISHED"
  }'
```

### 3. 手动触发新闻采集

```bash
curl -X POST http://localhost:8080/api/admin/crawl-tasks/trigger \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "sources": ["新浪", "央视", "网易"]
  }'
```

### 4. 前端访问测试

1. 打开浏览器访问 http://localhost:5173
2. 点击"管理员登录"
3. 输入用户名 `admin`，密码 `admin123`
4. 进入管理后台，测试新闻增删改查功能

---

## 开发工作流

### 后端开发流程

1. **创建Feature分支**
   ```bash
   git checkout -b feature/001-zhipuai-news-crawler
   ```

2. **编写测试用例**（Test-First）
   ```java
   // backend/src/test/java/com/news/service/NewsCrawlerServiceTest.java
   @Test
   void testCrawlNews() {
       List<News> news = crawlerService.crawl("新浪");
       assertThat(news).isNotEmpty();
       assertThat(news.get(0).getTitle()).isNotBlank();
   }
   ```

3. **实现功能**
   ```java
   // backend/src/main/java/com/news/service/impl/NewsCrawlerServiceImpl.java
   @Override
   public List<News> crawl(String source) {
       // 实现采集逻辑
   }
   ```

4. **运行测试**
   ```bash
   mvn test
   ```

5. **本地验证**
   - 启动应用
   - 使用Postman测试API
   - 检查日志输出

6. **提交代码**
   ```bash
   git add .
   git commit -m "feat: 实现新浪网新闻采集功能"
   git push origin feature/001-zhipuai-news-crawler
   ```

7. **创建Pull Request**
   - 在GitHub/GitLab创建PR
   - 等待代码审查

### 前端开发流程

1. **创建组件**
   ```bash
   # 创建新闻列表组件
   mkdir -p frontend/src/components/news
   touch frontend/src/components/news/NewsListItem.vue
   ```

2. **编写组件代码**
   ```vue
   <!-- frontend/src/components/news/NewsListItem.vue -->
   <template>
     <div class="news-item">
       <h3>{{ news.title }}</h3>
       <p>{{ news.summary }}</p>
     </div>
   </template>
   
   <script setup lang="ts">
   import { News } from '@/types/news'
   
   defineProps<{
     news: News
   }>()
   </script>
   ```

3. **编写单元测试**
   ```typescript
   // frontend/tests/unit/NewsListItem.spec.ts
   import { mount } from '@vue/test-utils'
   import NewsListItem from '@/components/news/NewsListItem.vue'
   
   describe('NewsListItem', () => {
     it('renders news title', () => {
       const wrapper = mount(NewsListItem, {
         props: {
           news: { title: 'Test News', summary: 'Summary' }
         }
       })
       expect(wrapper.text()).toContain('Test News')
     })
   })
   ```

4. **运行测试**
   ```bash
   npm run test:unit
   ```

5. **本地预览**
   - 浏览器自动刷新
   - 检查样式和交互

---

## 常见问题

### Q1: MySQL连接失败
**错误**: `Communications link failure`

**解决**:
1. 检查MySQL是否启动：`docker ps` 或 `systemctl status mysql`
2. 确认端口3306未被占用
3. 检查用户名密码是否正确
4. 防火墙是否阻止连接

### Q2: Redis连接失败
**错误**: `Unable to connect to Redis`

**解决**:
1. Redis是可选依赖，可以在配置中禁用：
   ```yaml
   spring:
     cache:
       type: none  # 禁用缓存
   ```
2. 或启动Redis容器：`docker run -p 6379:6379 -d redis:7-alpine`

### Q3: 智谱AI调用失败
**错误**: `Unauthorized: Invalid API key`

**解决**:
1. 访问 https://open.bigmodel.cn 注册并获取API Key
2. 在 `application-dev.yml` 中配置：
   ```yaml
   zhipuai:
     api:
       key: your-actual-api-key
   ```

### Q4: 前端跨域错误
**错误**: `CORS policy: No 'Access-Control-Allow-Origin' header`

**解决**:
1. 后端已配置CORS，检查`CorsConfig.java`
2. 确认前端API Base URL配置正确
3. 如使用Nginx，检查反向代理配置

### Q5: Flyway迁移失败
**错误**: `Migration checksum mismatch`

**解决**:
```bash
# 清理Flyway历史（仅限开发环境！）
mvn flyway:clean
mvn flyway:migrate

# 或手动修复
DELETE FROM flyway_schema_history WHERE version = 'X';
```

---

## 性能优化建议

### 开发环境优化

1. **禁用不必要的功能**
   ```yaml
   spring:
     jpa:
       show-sql: false  # 关闭SQL日志
     devtools:
       livereload:
         enabled: false  # 关闭自动重启
   ```

2. **使用内存数据库（H2）进行单元测试**
   ```yaml
   # application-test.yml
   spring:
     datasource:
       url: jdbc:h2:mem:testdb
   ```

3. **前端开发服务器优化**
   ```typescript
   // vite.config.ts
   export default {
     server: {
       hmr: {
         overlay: false  // 关闭错误全屏覆盖
       }
     }
   }
   ```

---

## 下一步

环境搭建完成后，建议按以下顺序开发：

1. ✅ **Phase 0**: 环境搭建（已完成）
2. **Phase 1**: 实现用户认证（User Story 1）
3. **Phase 2**: 实现新闻管理（User Story 2）
4. **Phase 3**: 实现新闻采集（User Story 3）
5. **Phase 4**: 实现AI摘要（User Story 4）
6. **Phase 5**: 实现用户浏览（User Story 5）

详细任务分解请参考: `specs/001-zhipuai/tasks.md`（待生成）

---

## 参考资源

- **Spring Boot官方文档**: https://spring.io/projects/spring-boot
- **Vue 3官方文档**: https://vuejs.org/
- **Element Plus文档**: https://element-plus.org/
- **智谱AI API文档**: https://open.bigmodel.cn/dev/api
- **项目规范文档**: `specs/001-zhipuai/spec.md`
- **技术研究文档**: `specs/001-zhipuai/research.md`
- **数据模型文档**: `specs/001-zhipuai/data-model.md`
- **API契约文档**: `specs/001-zhipuai/contracts/`

---

## 技术支持

如遇到问题，请：
1. 查阅项目文档
2. 搜索GitHub Issues
3. 联系团队技术负责人

祝开发顺利！🚀

