# 🔧 新闻详情 API 调用修复说明

## 问题描述
用户访问新闻详情页时出现错误：
```
Cannot read properties of undefined (reading 'likeCount')
```

## 错误原因

### 原代码问题
```javascript
const response = await getNewsById(newsId)
news.value = response.news      // ❌ 错误：response 就是 News 对象
summary.value = response.summary // ❌ 错误：response 没有 summary 属性
likeCount.value = response.news.likeCount // ❌ 错误：response.news 是 undefined
```

### API 定义
根据 `frontend/src/api/news.ts`:
```typescript
// getNewsById 直接返回 News 对象
export function getNewsById(id: number) {
  return request.get<News>(`/news/${id}`)
}

// getSummary 单独获取摘要
export function getSummary(newsId: number) {
  return request.get<Summary>(`/summaries/news/${newsId}`)
}
```

**问题**：新闻和摘要是两个独立的 API，需要分别调用。

## 解决方案

### 修复后的代码
```javascript
const fetchNewsDetail = async () => {
  try {
    loading.value = true
    const newsId = Number(route.params.id)
    
    // 1. 获取新闻详情（必须）
    const newsData = await getNewsById(newsId)
    news.value = newsData
    likeCount.value = newsData.likeCount || 0
    commentCount.value = newsData.commentCount || 0
    
    // 2. 尝试获取摘要（可选，可能不存在）
    try {
      const summaryData = await getSummary(newsId)
      summary.value = summaryData
    } catch (summaryError) {
      console.log('暂无摘要')
      summary.value = null
    }
    
    // 3. 检查点赞状态（需要登录）
    if (isLoggedIn.value) {
      try {
        const likeStatus = await checkLikeStatus(newsId)
        isLiked.value = likeStatus.liked
      } catch (error) {
        console.log('未登录或检查点赞状态失败')
      }
    }
    
    // 4. 加载评论
    await loadComments()
  } catch (error) {
    console.error('获取新闻详情失败:', error)
    ElMessage.error('获取新闻详情失败')
  } finally {
    loading.value = false
  }
}
```

## 改进点

### 1. ✅ 正确的 API 调用
- 直接使用 `getNewsById` 返回的 News 对象
- 单独调用 `getSummary` 获取摘要

### 2. ✅ 错误处理增强
- 摘要不存在时不影响主流程
- 点赞状态检查失败不阻塞页面显示
- 每个 API 调用都有独立的错误处理

### 3. ✅ 用户体验优化
- 即使摘要不存在，新闻内容仍正常显示
- 未登录用户也能正常浏览新闻
- 评论加载失败不影响新闻显示

## 数据流程

```
1. 访问新闻详情页 (/news/:id)
   ↓
2. 加载新闻基本信息
   - 调用 getNewsById(id) → News 对象
   - 提取 title, content, viewCount 等
   ↓
3. 尝试加载摘要（可选）
   - 调用 getSummary(id) → Summary 对象
   - 如果失败，显示没有摘要
   ↓
4. 检查点赞状态（需登录）
   - 调用 checkLikeStatus(id) → { liked: boolean }
   - 显示点赞按钮状态
   ↓
5. 加载评论列表
   - 调用 getNewsComments(id) → Comment[]
   - 显示评论区
   ↓
6. 页面完整显示
```

## API 端点说明

### 新闻相关
- `GET /news/:id` - 获取新闻详情
  - 返回：News 对象
  - 包含：title, content, imageUrls, viewCount 等

### 摘要相关
- `GET /summaries/news/:newsId` - 获取新闻摘要
  - 返回：Summary 对象
  - 包含：summaryContent（AI 生成的摘要）
  - 可能不存在（新闻未生成摘要）

### 点赞相关
- `GET /likes/news/:newsId/status` - 检查点赞状态
  - 返回：{ liked: boolean }
  - 需要用户登录

### 评论相关
- `GET /comments/news/:newsId` - 获取评论列表
  - 返回：Comment[]
  - 包含评论内容、用户名、时间等

## 界面显示逻辑

### 新闻内容区域
```vue
<article v-else-if="news">
  <!-- 标题 -->
  <h1>{{ news.title }}</h1>
  
  <!-- 元信息 -->
  <div>
    <span>{{ news.sourceWebsite }}</span>
    <span>{{ news.publishTime }}</span>
    <span>{{ news.viewCount }} 阅读</span>
  </div>
  
  <!-- 图片（如果有） -->
  <div v-if="news.imageUrls && news.imageUrls.length > 0">
    <!-- 图片轮播 -->
  </div>
  
  <!-- AI摘要（如果有） -->
  <div v-if="summary">
    <h3>🤖 AI智能摘要</h3>
    <p>{{ summary.summaryContent }}</p>
  </div>
  
  <!-- 正文 -->
  <div v-html="formatContent(news.content)"></div>
  
  <!-- 点赞和评论 -->
  <button @click="toggleLike">
    {{ isLiked ? '已点赞' : '点赞' }} ({{ likeCount }})
  </button>
</article>
```

### 显示状态说明
- ✅ 新闻内容：始终显示（必须）
- ⚠️ AI 摘要：v-if="summary" 条件显示（可选）
- ⚠️ 点赞状态：登录后显示正确状态（可选）
- ✅ 评论区：始终显示，未登录提示登录（必须）

## 测试要点

### 1. 基本显示测试
- [ ] 新闻标题正确显示
- [ ] 新闻内容正确渲染
- [ ] 图片正常加载
- [ ] 时间和阅读数显示

### 2. 摘要测试
- [ ] 有摘要的新闻显示摘要区域
- [ ] 无摘要的新闻不显示摘要区域
- [ ] 摘要内容格式正确

### 3. 点赞测试
- [ ] 未登录：显示"点赞"按钮
- [ ] 已登录且未点赞：显示"点赞"按钮
- [ ] 已登录且已点赞：显示"已点赞"按钮
- [ ] 点赞数量正确显示

### 4. 评论测试
- [ ] 未登录：显示"登录后可评论"提示
- [ ] 已登录：显示评论输入框
- [ ] 评论列表正常加载
- [ ] 评论数量正确显示

### 5. 错误处理测试
- [ ] 新闻不存在：显示"新闻不存在"提示
- [ ] 网络错误：显示错误提示
- [ ] 摘要不存在：不影响新闻显示
- [ ] 点赞接口失败：不影响页面显示

## 调试方法

### 打开浏览器控制台（F12）
```javascript
// 查看新闻数据
console.log(news.value)

// 查看摘要数据
console.log(summary.value)

// 查看点赞状态
console.log(isLiked.value, likeCount.value)

// 查看评论数据
console.log(comments.value)
```

### 检查 Network 请求
1. 打开 Network 标签
2. 刷新页面
3. 查看以下请求：
   - `/news/:id` - 应该返回 200
   - `/summaries/news/:id` - 可能返回 200 或 404
   - `/likes/news/:id/status` - 登录后返回 200
   - `/comments/news/:id` - 应该返回 200

## 后端数据示例

### News 对象
```json
{
  "id": 1,
  "title": "新闻标题",
  "content": "新闻内容...",
  "sourceWebsite": "新闻来源",
  "imageUrls": ["url1.jpg", "url2.jpg"],
  "publishTime": "2025-10-28T10:00:00",
  "viewCount": 100,
  "likeCount": 10,
  "commentCount": 5,
  "categoryName": "时政"
}
```

### Summary 对象
```json
{
  "id": 1,
  "newsId": 1,
  "summaryContent": "这是AI生成的摘要...",
  "generatedAt": "2025-10-28T10:05:00",
  "status": "SUCCESS"
}
```

## 常见问题

### Q1: 新闻内容不显示
**原因**：API 调用失败或返回数据格式不对  
**解决**：检查 Network 标签，确认 `/news/:id` 返回正确数据

### Q2: 摘要区域不显示
**原因**：该新闻没有生成摘要（正常情况）  
**解决**：在管理后台手动生成摘要

### Q3: 点赞按钮状态不对
**原因**：未登录或点赞状态 API 失败  
**解决**：先登录，然后刷新页面

### Q4: 评论不显示
**原因**：评论 API 调用失败  
**解决**：检查后端评论服务是否正常

## 修改的文件

- ✅ `frontend/src/views/user/NewsDetail.vue` - 修复 fetchNewsDetail 函数
- ✅ 添加 getSummary 导入
- ✅ 优化错误处理逻辑

---

**修复时间**: 2025-10-28  
**问题**: 获取新闻详情失败，Cannot read properties of undefined  
**状态**: ✅ 已修复  
**影响**: 新闻详情页数据加载




