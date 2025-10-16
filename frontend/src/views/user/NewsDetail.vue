<template>
  <div class="news-detail-page">
    <div class="container">
      <!-- 返回按钮 -->
      <div class="back-btn">
        <el-button @click="goBack" :icon="ArrowLeft">返回列表</el-button>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <!-- 新闻内容 -->
      <article v-else-if="news" class="news-article">
        <!-- 标题 -->
        <h1 class="title">{{ news.title }}</h1>

        <!-- 元信息 -->
        <div class="meta">
          <span class="source">{{ news.sourceWebsite }}</span>
          <span class="time">{{ formatDateTime(news.publishTime) }}</span>
          <span class="views">
            <el-icon><View /></el-icon>
            {{ news.viewCount }} 阅读
          </span>
        </div>

        <!-- 新闻配图 -->
        <div v-if="news.imageUrl" class="news-image">
          <img :src="news.imageUrl" :alt="news.title" />
        </div>

        <!-- AI摘要 -->
        <div v-if="summary" class="summary-box">
          <div class="summary-header">
            <el-icon><Document /></el-icon>
            <span>AI智能摘要</span>
          </div>
          <p class="summary-content">{{ summary.summaryContent }}</p>
        </div>

        <!-- 正文 -->
        <div class="content" v-html="formatContent(news.content)"></div>

        <!-- 底部信息 -->
        <div class="footer-info">
          <p>来源: <a :href="news.originalUrl" target="_blank">{{ news.originalUrl }}</a></p>
        </div>

        <!-- 点赞和分享 -->
        <div class="actions">
          <el-button
            :type="isLiked ? 'primary' : 'default'"
            :icon="isLiked ? StarFilled : Star"
            @click="toggleLike"
          >
            {{ isLiked ? '已点赞' : '点赞' }} ({{ likeCount }})
          </el-button>
          <el-button :icon="ChatDotRound">
            评论 ({{ commentCount }})
          </el-button>
        </div>

        <!-- 评论区 -->
        <div class="comments-section">
          <h3>评论区</h3>

          <!-- 发表评论 -->
          <div v-if="isLoggedIn" class="comment-form">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              placeholder="发表你的看法..."
              maxlength="500"
              show-word-limit
            />
            <el-button
              type="primary"
              :loading="submitting"
              :disabled="!commentContent.trim()"
              @click="submitComment"
              style="margin-top: 10px"
            >
              发表评论
            </el-button>
          </div>
          <div v-else class="login-prompt">
            <router-link to="/login">登录</router-link> 后发表评论
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-header">
                <span class="username">{{ comment.username }}</span>
                <span class="time">{{ formatDateTime(comment.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
              <div v-if="canDeleteComment(comment)" class="comment-actions">
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="deleteCommentById(comment.id)"
                >
                  删除
                </el-button>
              </div>
            </div>

            <el-empty
              v-if="comments.length === 0"
              description="暂无评论，快来抢沙发吧！"
            />
          </div>
        </div>
      </article>

      <!-- 错误状态 -->
      <el-empty v-else description="新闻不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Loading, View, Document, 
  Star, StarFilled, ChatDotRound 
} from '@element-plus/icons-vue'
import { getNewsById, getSummary, type News, type Summary } from '@/api/news'
import { 
  getNewsComments, createComment, deleteComment, 
  type Comment 
} from '@/api/comment'
import { 
  likeNews, unlikeNews, checkLikeStatus, getLikeCount 
} from '@/api/like'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const news = ref<News | null>(null)
const summary = ref<Summary | null>(null)
const loading = ref(false)

// 点赞相关
const isLiked = ref(false)
const likeCount = ref(0)

// 评论相关
const comments = ref<Comment[]>([])
const commentContent = ref('')
const submitting = ref(false)
const commentCount = computed(() => comments.value.length)

// 登录状态
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('news_management_token')
})

// 加载新闻详情
async function loadNews() {
  const newsId = Number(route.params.id)
  if (!newsId) {
    ElMessage.error('新闻ID无效')
    return
  }

  loading.value = true
  try {
    news.value = await getNewsById(newsId)
    
    // 尝试加载摘要
    try {
      summary.value = await getSummary(newsId)
    } catch (err) {
      // 摘要可能不存在，忽略错误
      console.log('No summary available')
    }
  } catch (error) {
    ElMessage.error('加载新闻失败')
    news.value = null
  } finally {
    loading.value = false
  }
}

// 返回
function goBack() {
  router.back()
}

// 格式化日期时间
function formatDateTime(time: string) {
  return new Date(time).toLocaleString('zh-CN')
}

// 格式化内容（将换行转为<br>）
function formatContent(content: string) {
  return content.replace(/\n/g, '<br>')
}

// 加载点赞状态
async function loadLikeStatus() {
  if (!isLoggedIn.value) return

  const newsId = Number(route.params.id)
  try {
    const response = await checkLikeStatus(newsId)
    isLiked.value = response.liked
  } catch (error) {
    // 未登录或其他错误，忽略
    console.log('Failed to load like status')
  }
}

// 加载点赞数
async function loadLikeCount() {
  const newsId = Number(route.params.id)
  try {
    const response = await getLikeCount(newsId)
    likeCount.value = response.count
  } catch (error) {
    console.error('Failed to load like count:', error)
  }
}

// 切换点赞
async function toggleLike() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const newsId = Number(route.params.id)
  try {
    if (isLiked.value) {
      await unlikeNews(newsId)
      isLiked.value = false
      likeCount.value--
      ElMessage.success('已取消点赞')
    } else {
      await likeNews(newsId)
      isLiked.value = true
      likeCount.value++
      ElMessage.success('点赞成功')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 加载评论列表
async function loadComments() {
  const newsId = Number(route.params.id)
  try {
    comments.value = await getNewsComments(newsId)
  } catch (error) {
    console.error('Failed to load comments:', error)
  }
}

// 提交评论
async function submitComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  const newsId = Number(route.params.id)
  submitting.value = true

  try {
    await createComment({
      newsId,
      content: commentContent.value
    })

    ElMessage.success('评论发表成功')
    commentContent.value = ''
    await loadComments()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '评论发表失败')
  } finally {
    submitting.value = false
  }
}

// 删除评论
async function deleteCommentById(commentId: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteComment(commentId)
    ElMessage.success('评论已删除')
    await loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 检查是否可以删除评论
function canDeleteComment(comment: Comment) {
  if (!isLoggedIn.value) return false
  
  const currentUser = userStore.user
  if (!currentUser) return false
  
  // 用户自己的评论或管理员可以删除
  return comment.userId === currentUser.id || currentUser.role === 'ADMIN'
}

onMounted(() => {
  loadNews()
  loadLikeStatus()
  loadLikeCount()
  loadComments()
})
</script>

<style scoped>
.news-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px 0;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-btn {
  margin-bottom: 20px;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  color: #666;
  font-size: 16px;
}

/* 文章容器 */
.news-article {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

/* 标题 */
.title {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
  margin: 0 0 20px 0;
}

/* 元信息 */
.meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 14px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  margin-bottom: 20px;
  align-items: center;
}

.source {
  color: #c00;
  font-weight: 500;
  font-size: 15px;
}

.views {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 新闻配图 */
.news-image {
  width: 100%;
  max-width: 800px;
  margin: 20px auto;
  border-radius: 8px;
  overflow: hidden;
}

.news-image img {
  width: 100%;
  height: auto;
  display: block;
}

/* AI摘要 */
.summary-box {
  background: #f8f9fa;
  border-left: 4px solid #409eff;
  padding: 20px;
  margin-bottom: 30px;
  border-radius: 4px;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #409eff;
  font-weight: bold;
  margin-bottom: 12px;
  font-size: 16px;
}

.summary-content {
  color: #555;
  line-height: 1.8;
  margin: 0;
  font-size: 15px;
}

/* 正文 */
.content {
  color: #333;
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 30px;
}

.content :deep(p) {
  margin: 16px 0;
}

/* 底部信息 */
.footer-info {
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
  color: #999;
  font-size: 14px;
}

.footer-info a {
  color: #409eff;
  text-decoration: none;
}

.footer-info a:hover {
  text-decoration: underline;
}

/* 操作按钮 */
.actions {
  display: flex;
  gap: 15px;
  padding: 20px 0;
  border-top: 1px solid #e8e8e8;
  border-bottom: 1px solid #e8e8e8;
  margin: 20px 0;
}

/* 评论区 */
.comments-section {
  margin-top: 30px;
}

.comments-section h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
}

.comment-form {
  margin-bottom: 30px;
}

.login-prompt {
  text-align: center;
  padding: 30px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #666;
  margin-bottom: 30px;
}

.login-prompt a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
}

.login-prompt a:hover {
  text-decoration: underline;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.comment-item:hover {
  background: #f5f5f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.username {
  font-weight: 600;
  color: #409eff;
  font-size: 14px;
}

.time {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #555;
  line-height: 1.6;
  font-size: 15px;
  margin: 10px 0;
  word-wrap: break-word;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}
</style>

