<template>
  <div class="statistics-container">
    <div class="page-header">
      <h1>数据统计</h1>
      <p class="subtitle">查看您的新闻数据统计</p>
    </div>

    <el-card class="stats-overview" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>概览统计</span>
          <el-button type="primary" size="small" :icon="Refresh" @click="refreshStats">
            刷新数据
          </el-button>
        </div>
      </template>

      <div class="stats-grid" v-loading="loading">
        <div class="stat-item">
          <div class="stat-icon draft">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">草稿数量</div>
            <div class="stat-value">{{ stats.draftCount || 0 }}</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">待审核</div>
            <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon reviewing">
            <el-icon><View /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">审核中</div>
            <div class="stat-value">{{ stats.reviewingCount || 0 }}</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon published">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">已发布</div>
            <div class="stat-value">{{ stats.publishedCount || 0 }}</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon rejected">
            <el-icon><Close /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">被退回</div>
            <div class="stat-value">{{ stats.rejectedCount || 0 }}</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon views">
            <el-icon><View /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总阅读量</div>
            <div class="stat-value">{{ formatNumber(stats.totalViews || 0) }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 最近动态 -->
    <el-card class="recent-activity" shadow="hover">
      <template #header>
        <span>最近动态</span>
      </template>

      <div v-if="recentNews.length === 0" class="empty-state">
        <el-empty description="暂无动态数据" />
      </div>

      <div v-else class="activity-list">
        <div v-for="news in recentNews" :key="news.id" class="activity-item">
          <div class="activity-content">
            <h4 class="activity-title">{{ news.title }}</h4>
            <p class="activity-meta">
              <el-tag :type="getStatusType(news.status)" size="small">
                {{ getStatusText(news.status) }}
              </el-tag>
              <span class="activity-time">
                {{ formatTime(news.updatedAt) }}
              </span>
            </p>
          </div>
          <div class="activity-actions">
            <el-button 
              type="primary" 
              link 
              size="small"
              @click="goToEdit(news.id)"
              v-if="news.status === 'DRAFT' || news.status === 'REJECTED'"
            >
              编辑
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Refresh, Edit, Clock, View, Check, Close 
} from '@element-plus/icons-vue'
import { getEditorStats, getEditorRecentNews } from '@/api/editor'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const stats = ref({
  draftCount: 0,
  pendingCount: 0,
  reviewingCount: 0,
  publishedCount: 0,
  rejectedCount: 0,
  totalViews: 0
})
const recentNews = ref([])

// 获取统计数据
const fetchStats = async () => {
  try {
    loading.value = true
    const response = await getEditorStats()
    stats.value = response
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 获取最近动态
const fetchRecentNews = async () => {
  try {
    const response = await getEditorRecentNews({ page: 0, size: 10 })
    recentNews.value = response.content || []
  } catch (error) {
    console.error('获取最近动态失败:', error)
  }
}

// 刷新统计数据
const refreshStats = async () => {
  await Promise.all([fetchStats(), fetchRecentNews()])
  ElMessage.success('数据已刷新')
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}万`
  }
  return num.toString()
}

// 格式化时间
const formatTime = (time: string): string => {
  return new Date(time).toLocaleString('zh-CN')
}

// 获取状态类型
const getStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'REVIEWING': 'primary',
    'PUBLISHED': 'success',
    'REJECTED': 'danger',
    'ARCHIVED': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PENDING': '待审核',
    'REVIEWING': '审核中',
    'PUBLISHED': '已发布',
    'REJECTED': '被退回',
    'ARCHIVED': '已归档'
  }
  return textMap[status] || status
}

// 跳转到编辑页面
const goToEdit = (newsId: number) => {
  router.push(`/editor/news-editor/${newsId}`)
}

// 页面加载
onMounted(() => {
  fetchStats()
  fetchRecentNews()
})
</script>

<style scoped>
.statistics-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
}

.page-header .subtitle {
  margin: 0;
  color: #7f8c8d;
  font-size: 16px;
}

.stats-overview {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: #e3f2fd;
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
}

.stat-icon.draft {
  background: rgba(144, 202, 249, 0.2);
  color: #1976d2;
}

.stat-icon.pending {
  background: rgba(255, 183, 77, 0.2);
  color: #f57c00;
}

.stat-icon.reviewing {
  background: rgba(149, 117, 205, 0.2);
  color: #7b1fa2;
}

.stat-icon.published {
  background: rgba(129, 199, 132, 0.2);
  color: #388e3c;
}

.stat-icon.rejected {
  background: rgba(239, 154, 154, 0.2);
  color: #d32f2f;
}

.stat-icon.views {
  background: rgba(178, 223, 219, 0.2);
  color: #00796b;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
}

.recent-activity {
  margin-bottom: 24px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.activity-list {
  max-height: 400px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-content {
  flex: 1;
  margin-right: 16px;
}

.activity-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  color: #2c3e50;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-meta {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.activity-time {
  color: #999;
}

.activity-actions {
  flex-shrink: 0;
}
</style>
