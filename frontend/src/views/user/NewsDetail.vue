<template>
  <div class="min-h-screen bg-gray-50">
    <ModernHeader />
    
    <div class="pt-24 pb-12 px-6">
      <div class="max-w-4xl mx-auto">
        <!-- 返回按钮 -->
        <button
          @click="goBack"
          class="mb-6 flex items-center gap-2 text-gray-600 hover:text-black transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
          </svg>
          返回列表
        </button>

        <!-- 加载状态 -->
        <div v-if="loading" class="bg-white rounded-2xl shadow-sm p-12 text-center">
          <div class="animate-pulse">
            <div class="text-4xl mb-4">📰</div>
            <p class="text-gray-600">加载中...</p>
          </div>
        </div>

        <!-- 新闻内容 -->
        <article v-else-if="news" class="bg-white rounded-2xl shadow-sm overflow-hidden">
          <!-- 分类标签 -->
          <div class="px-8 pt-8">
            <span class="inline-block px-4 py-1 bg-black text-white text-sm rounded-full">
              {{ news.categoryName }}
            </span>
          </div>

          <!-- 标题 -->
          <h1 class="text-3xl md:text-5xl font-bold px-8 pt-6 pb-4 leading-tight">
            {{ news.title }}
          </h1>

          <!-- 元信息 -->
          <div class="px-8 pb-6 flex flex-wrap gap-4 text-sm text-gray-600">
            <span v-if="news.sourceWebsite">📡 {{ news.sourceWebsite }}</span>
            <span>🕐 {{ formatDateTime(news.publishTime) }}</span>
            <span>👁️ {{ news.viewCount || 0 }} 阅读</span>
          </div>

          <!-- 新闻配图 -->
          <div v-if="news.imageUrls && news.imageUrls.length > 0" class="mb-6">
            <!-- 单张图片 -->
            <div v-if="news.imageUrls.length === 1" class="w-full">
              <img :src="news.imageUrls[0]" :alt="news.title" class="w-full h-auto" />
            </div>
            
            <!-- 多张图片 -->
            <div v-else class="relative">
              <el-carousel height="500px" indicator-position="outside">
                <el-carousel-item v-for="(imageUrl, index) in news.imageUrls" :key="index">
                  <div class="w-full h-full flex items-center justify-center bg-gray-100">
                    <img :src="imageUrl" :alt="`${news.title} - 图片 ${index + 1}`" class="max-w-full max-h-full object-contain" />
                  </div>
                </el-carousel-item>
              </el-carousel>
              <div class="absolute top-4 right-4 bg-black/70 text-white px-3 py-1 rounded-full text-xs">
                {{ news.imageUrls.length }} 张图片
              </div>
            </div>
          </div>
          
          <!-- 备用：显示单张主图（兼容旧数据） -->
          <div v-else-if="news.imageUrl" class="mb-6">
            <img :src="news.imageUrl" :alt="news.title" class="w-full h-auto" />
          </div>

          <!-- AI摘要 -->
          <div v-if="summary" class="mx-8 mb-6 p-6 bg-gray-50 rounded-xl border-l-4 border-black">
            <div class="flex items-center gap-2 font-semibold mb-3">
              <span class="text-xl">🤖</span>
              <span>AI智能摘要</span>
            </div>
            <p class="text-gray-700 leading-relaxed">{{ summary.summaryContent }}</p>
          </div>

          <!-- 正文 -->
          <div class="px-8 pb-8 prose prose-lg max-w-none" v-html="formatContent(news.content)"></div>

          <!-- 底部信息 -->
          <div v-if="news.originalUrl" class="px-8 pb-6 text-sm text-gray-500">
            <p>
              来源: 
              <a :href="news.originalUrl" target="_blank" class="text-blue-600 hover:underline">
                {{ news.originalUrl }}
              </a>
            </p>
          </div>

          <!-- 点赞和评论按钮 -->
          <div class="px-8 pb-8 flex gap-3">
            <button
              @click="toggleLike"
              :class="[
                'flex items-center gap-2 px-6 py-3 rounded-full font-medium transition-all',
                isLiked
                  ? 'bg-black text-white'
                  : 'bg-gray-100 text-black hover:bg-gray-200'
              ]"
            >
              <span>{{ isLiked ? '❤️' : '🤍' }}</span>
              {{ isLiked ? '已点赞' : '点赞' }} ({{ likeCount }})
            </button>
            <button
              class="flex items-center gap-2 px-6 py-3 rounded-full font-medium bg-gray-100 text-black hover:bg-gray-200 transition-all"
            >
              <span>💬</span>
              评论 ({{ commentCount }})
            </button>
          </div>

          <!-- 评论区 -->
          <div class="px-8 pb-8 border-t pt-8">
            <h3 class="text-2xl font-bold mb-6">评论区</h3>

            <!-- 发表评论 -->
            <div v-if="isLoggedIn" class="mb-8">
              <textarea
                v-model="commentContent"
                placeholder="发表你的看法..."
                rows="4"
                maxlength="500"
                class="w-full p-4 border border-gray-300 rounded-xl focus:outline-none focus:border-black transition-colors resize-none"
              ></textarea>
              <div class="flex justify-between items-center mt-3">
                <span class="text-sm text-gray-500">{{ commentContent.length }}/500</span>
                <button
                  @click="submitComment"
                  :disabled="!commentContent.trim()"
                  class="px-6 py-2 bg-black text-white rounded-full font-medium disabled:bg-gray-300 disabled:cursor-not-allowed hover:bg-gray-800 transition-colors"
                >
                  发表评论
                </button>
              </div>
            </div>

            <!-- 未登录提示 -->
            <div v-else class="mb-8 p-6 bg-gray-50 rounded-xl text-center">
              <p class="text-gray-600 mb-3">登录后即可发表评论</p>
              <router-link
                to="/login"
                class="inline-block px-6 py-2 bg-black text-white rounded-full font-medium hover:bg-gray-800 transition-colors"
              >
                立即登录
              </router-link>
            </div>

            <!-- 评论列表 -->
            <div v-if="comments.length > 0" class="space-y-6">
              <div
                v-for="comment in comments"
                :key="comment.id"
                class="border-b pb-6 last:border-b-0"
              >
                <div class="flex items-start gap-4">
                  <!-- 头像 -->
                  <div class="w-10 h-10 rounded-full bg-gradient-to-br from-black to-gray-600 flex items-center justify-center text-white font-bold flex-shrink-0">
                    {{ comment.username?.[0]?.toUpperCase() || 'U' }}
                  </div>

                  <div class="flex-1">
                    <!-- 用户名和时间 -->
                    <div class="flex items-center gap-3 mb-2">
                      <span class="font-semibold">{{ comment.username }}</span>
                      <span class="text-sm text-gray-500">{{ formatDateTime(comment.createdAt) }}</span>
                    </div>

                    <!-- 评论内容 -->
                    <p class="text-gray-700 mb-3">{{ comment.content }}</p>

                    <!-- 操作按钮 -->
                    <div class="flex items-center gap-4">
                      <button
                        @click="toggleCommentLike(comment)"
                        :class="[
                          'text-sm flex items-center gap-1 transition-colors',
                          comment.isLiked ? 'text-red-600' : 'text-gray-500 hover:text-red-600'
                        ]"
                      >
                        <span>{{ comment.isLiked ? '❤️' : '🤍' }}</span>
                        <span>{{ comment.likeCount || 0 }}</span>
                      </button>
                      <button
                        v-if="isLoggedIn"
                        @click="replyToComment(comment)"
                        class="text-sm text-gray-500 hover:text-black transition-colors"
                      >
                        回复
                      </button>
                    </div>

                    <!-- 回复框 -->
                    <div v-if="replyingTo === comment.id" class="mt-4">
                      <textarea
                        v-model="replyContent"
                        placeholder="输入回复内容..."
                        rows="3"
                        maxlength="500"
                        class="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:border-black transition-colors resize-none"
                      ></textarea>
                      <div class="flex justify-end gap-2 mt-2">
                        <button
                          @click="replyingTo = null"
                          class="px-4 py-1 text-sm text-gray-600 hover:text-black transition-colors"
                        >
                          取消
                        </button>
                        <button
                          @click="submitReply(comment)"
                          :disabled="!replyContent.trim()"
                          class="px-4 py-1 text-sm bg-black text-white rounded-full disabled:bg-gray-300 hover:bg-gray-800 transition-colors"
                        >
                          回复
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空评论 -->
            <div v-else class="text-center py-12 text-gray-500">
              <div class="text-4xl mb-3">💭</div>
              <p>还没有评论，来发表第一条吧！</p>
            </div>

            <!-- 加载更多 -->
            <div v-if="hasMoreComments" class="text-center mt-8">
              <button
                @click="loadMoreComments"
                class="px-6 py-2 text-sm border border-gray-300 rounded-full hover:border-black hover:bg-gray-50 transition-all"
              >
                加载更多评论
              </button>
            </div>
          </div>
        </article>

        <!-- 错误状态 -->
        <div v-else class="bg-white rounded-2xl shadow-sm p-12 text-center">
          <div class="text-6xl mb-4">😢</div>
          <p class="text-xl text-gray-600 mb-4">新闻不存在或已被删除</p>
          <button
            @click="goBack"
            class="px-6 py-2 bg-black text-white rounded-full hover:bg-gray-800 transition-colors"
          >
            返回列表
          </button>
        </div>
      </div>
    </div>

    <ModernFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getNewsById, getSummary } from '@/api/news'
import { getNewsComments, createComment, type Comment as CommentType, type CommentCreateRequest } from '@/api/comment'
import { likeNews, checkLikeStatus } from '@/api/like'
import { useUserStore } from '@/stores/user'
import ModernHeader from '@/components/modern/ModernHeader.vue'
import ModernFooter from '@/components/modern/ModernFooter.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

interface News {
  id: number
  title: string
  content: string
  imageUrl?: string
  imageUrls?: string[]
  categoryName: string
  sourceWebsite?: string
  originalUrl?: string
  publishTime: string
  viewCount: number
}

interface Summary {
  summaryContent: string
}

// 使用从 API 导入的 Comment 类型

const loading = ref(true)
const news = ref<News | null>(null)
const summary = ref<Summary | null>(null)
const comments = ref<CommentType[]>([])
const commentContent = ref('')
const replyContent = ref('')
const replyingTo = ref<number | null>(null)
const commentPage = ref(0)
const commentSize = ref(10)
const hasMoreComments = ref(false)
const isLiked = ref(false)
const likeCount = ref(0)
const commentCount = ref(0)

const isLoggedIn = computed(() => !!userStore.token)

const fetchNewsDetail = async () => {
  try {
    loading.value = true
    const newsId = Number(route.params.id)
    
    // 获取新闻详情
    const newsData = await getNewsById(newsId)
    news.value = newsData
    likeCount.value = newsData.likeCount || 0
    commentCount.value = newsData.commentCount || 0
    
    // 尝试获取摘要（如果有的话）
    try {
      const summaryData = await getSummary(newsId)
      summary.value = summaryData
    } catch (summaryError) {
      // 摘要可能不存在，不影响主流程
      console.log('暂无摘要')
      summary.value = null
    }
    
    // 检查点赞状态
    if (isLoggedIn.value) {
      try {
        const likeStatus = await checkLikeStatus(newsId)
        isLiked.value = likeStatus.liked
      } catch (error) {
        console.log('未登录或检查点赞状态失败')
      }
    }
    
    // 加载评论
    await loadComments()
  } catch (error) {
    console.error('获取新闻详情失败:', error)
    ElMessage.error('获取新闻详情失败')
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  try {
    const newsId = Number(route.params.id)
    const response = await getNewsComments(newsId)
    comments.value = response || []
    hasMoreComments.value = false
    commentCount.value = comments.value.length
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const loadMoreComments = async () => {
  // 简化版本，因为 API 返回所有评论
  try {
    await loadComments()
  } catch (error) {
    console.error('加载更多评论失败:', error)
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    const newsId = Number(route.params.id)
    const data: CommentCreateRequest = {
      newsId,
      content: commentContent.value
    }
    await createComment(data)
    ElMessage.success('评论发表成功')
    commentContent.value = ''
    await loadComments()
    commentCount.value++
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败')
  }
}

const replyToComment = (comment: CommentType) => {
  replyingTo.value = comment.id
  replyContent.value = `@${comment.username} `
}

const submitReply = async (comment: CommentType) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    const newsId = Number(route.params.id)
    const data: CommentCreateRequest = {
      newsId,
      content: replyContent.value,
      parentId: comment.id
    }
    await createComment(data)
    ElMessage.success('回复成功')
    replyContent.value = ''
    replyingTo.value = null
    await loadComments()
    commentCount.value++
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }
}

const toggleLike = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const newsId = Number(route.params.id)
    await likeNews(newsId)
    isLiked.value = !isLiked.value
    likeCount.value += isLiked.value ? 1 : -1
    ElMessage.success(isLiked.value ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const toggleCommentLike = async (comment: CommentType) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    // 暂时不支持评论点赞功能，因为 API 中没有这个接口
    ElMessage.info('评论点赞功能开发中')
  } catch (error) {
    console.error('点赞评论失败:', error)
    ElMessage.error('操作失败')
  }
}

const formatContent = (content: string) => {
  if (!content) return ''
  
  // 如果内容已经包含 HTML 标签（如 <p>, <div> 等），直接返回
  if (/<[a-z][\s\S]*>/i.test(content)) {
    return content
  }
  
  // 否则，只处理换行符
  return content.replace(/\n/g, '<br>')
}

const formatDateTime = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchNewsDetail()
})
</script>

<style scoped>
.prose {
  line-height: 1.8;
}

.prose p {
  margin-bottom: 1em;
}

.prose h2,
.prose h3 {
  font-weight: bold;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.prose h2 {
  font-size: 1.5em;
}

.prose h3 {
  font-size: 1.25em;
}

.prose a {
  color: #3b82f6;
  text-decoration: underline;
}

.prose ul,
.prose ol {
  margin-left: 1.5em;
  margin-bottom: 1em;
}

.prose li {
  margin-bottom: 0.5em;
}
</style>
