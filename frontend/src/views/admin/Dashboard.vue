<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <div class="welcome-text">
          <h1>👋 欢迎回来，{{ username }}</h1>
          <p>{{ currentTime }} | 今天是个美好的一天，让我们开始工作吧！</p>
        </div>
        <div class="quick-actions">
          <el-button type="primary" @click="goToNews">
            <el-icon><Document /></el-icon>
            管理新闻
          </el-button>
          <el-button type="success" @click="goToCrawler">
            <el-icon><Download /></el-icon>
            采集新闻
          </el-button>
          <el-button type="warning" @click="goToReview">
            <el-icon><CircleCheck /></el-icon>
            审核新闻
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" :xs="12">
        <el-card class="stat-card stat-card-blue" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper stat-icon-blue">
              <el-icon class="stat-icon"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">新闻总数</div>
              <div class="stat-value">{{ statistics.totalNews }}</div>
              <div class="stat-desc">
                <el-icon><TrendCharts /></el-icon>
                较昨日 +{{ statistics.newsIncrement }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12">
        <el-card class="stat-card stat-card-green" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper stat-icon-green">
              <el-icon class="stat-icon"><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">分类数量</div>
              <div class="stat-value">{{ statistics.totalCategories }}</div>
              <div class="stat-desc">包含默认分类</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12">
        <el-card class="stat-card stat-card-orange" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper stat-icon-orange">
              <el-icon class="stat-icon"><Download /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日采集</div>
              <div class="stat-value">{{ statistics.todayCrawled }}</div>
              <div class="stat-desc">成功 {{ statistics.todaySuccess }} 条</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12">
        <el-card class="stat-card stat-card-red" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper stat-icon-red">
              <el-icon class="stat-icon"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待审核</div>
              <div class="stat-value">{{ statistics.pendingReviews || 0 }}</div>
              <div class="stat-desc">
                <span v-if="statistics.pendingReviews > 0" class="urgent">需要处理</span>
                <span v-else class="normal">暂无待审</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作和最近活动 -->
    <el-row :gutter="20" class="content-row">
      <el-col :span="12" :xs="24">
        <el-card shadow="hover" class="action-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Star /></el-icon>
                快捷操作
              </span>
            </div>
          </template>
          
          <div class="quick-links">
            <div class="quick-link-item" @click="goToNews">
              <div class="link-icon link-icon-blue">
                <el-icon><Plus /></el-icon>
              </div>
              <div class="link-info">
                <div class="link-title">创建新闻</div>
                <div class="link-desc">发布新的新闻文章</div>
              </div>
              <el-icon class="link-arrow"><ArrowRight /></el-icon>
            </div>
            
            <div class="quick-link-item" @click="goToCrawler">
              <div class="link-icon link-icon-green">
                <el-icon><Download /></el-icon>
              </div>
              <div class="link-info">
                <div class="link-title">采集新闻</div>
                <div class="link-desc">从新闻源自动采集</div>
              </div>
              <el-icon class="link-arrow"><ArrowRight /></el-icon>
            </div>
            
            <div class="quick-link-item" @click="goToCategories">
              <div class="link-icon link-icon-orange">
                <el-icon><Folder /></el-icon>
              </div>
              <div class="link-info">
                <div class="link-title">管理分类</div>
                <div class="link-desc">新增或编辑分类</div>
              </div>
              <el-icon class="link-arrow"><ArrowRight /></el-icon>
            </div>
            
            <div class="quick-link-item" @click="goToRules">
              <div class="link-icon link-icon-purple">
                <el-icon><MagicStick /></el-icon>
              </div>
              <div class="link-info">
                <div class="link-title">AI摘要生成</div>
                <div class="link-desc">批量生成智能摘要</div>
              </div>
              <el-icon class="link-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12" :xs="24">
        <el-card shadow="hover" class="activity-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Clock /></el-icon>
                最近活动
              </span>
            </div>
          </template>
          
          <el-timeline>
            <el-timeline-item 
              v-for="(activity, index) in recentActivities" 
              :key="index"
              :timestamp="activity.time"
              :color="activity.color"
            >
              <div class="activity-item">
                <span class="activity-text">{{ activity.text }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
          
          <el-empty v-if="recentActivities.length === 0" description="暂无最近活动" :image-size="100" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-row :gutter="20" class="content-row">
      <el-col :span="24">
        <el-card shadow="hover" class="system-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><InfoFilled /></el-icon>
                系统信息
              </span>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">系统版本</div>
                <div class="info-value">v1.0.0</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">后端框架</div>
                <div class="info-value">Spring Boot 3.2</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">前端框架</div>
                <div class="info-value">Vue 3 + Element Plus</div>
              </div>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">数据库</div>
                <div class="info-value">MySQL 8.0</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">缓存</div>
                <div class="info-value">Redis 7.0</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">AI引擎</div>
                <div class="info-value">智谱AI GLM-4</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  Document, 
  Download, 
  Folder, 
  Plus, 
  Star, 
  Clock,
  ArrowRight,
  View,
  TrendCharts,
  InfoFilled,
  MagicStick,
  CircleCheck
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const username = computed(() => userStore.userInfo?.username || 'Admin')
const currentTime = ref('')

// 统计数据
const statistics = ref({
  totalNews: 0,
  newsIncrement: 0,
  totalCategories: 6,
  todayCrawled: 0,
  todaySuccess: 0,
  totalViews: 0,
  todayViews: 0,
  pendingReviews: 0
})

// 最近活动
const recentActivities = ref<any[]>([
  { text: '系统启动成功', time: '刚刚', color: '#67c23a' },
  { text: '欢迎使用新闻管理系统', time: '刚刚', color: '#409eff' }
])

// 更新当前时间
const updateTime = () => {
  const now = new Date()
  const hours = now.getHours()
  let greeting = '早上好'
  if (hours >= 12 && hours < 18) greeting = '下午好'
  else if (hours >= 18) greeting = '晚上好'
  
  currentTime.value = `${greeting}，${now.toLocaleDateString('zh-CN', { 
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })}`
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    // 加载新闻统计
    const newsResponse: any = await request.get('/news?page=0&size=1')
    statistics.value.totalNews = newsResponse.totalElements || 0
    
    // 加载分类统计
    const categoriesResponse: any = await request.get('/categories')
    statistics.value.totalCategories = categoriesResponse.length || 0
    
    // 加载爬虫统计
    const crawlerStats: any = await request.get('/crawler/statistics')
    statistics.value.todayCrawled = crawlerStats.recentTasksCount || 0
    statistics.value.todaySuccess = crawlerStats.totalSuccessCrawled || 0
    
    // 加载审核统计
    try {
      const reviewStats: any = await request.get('/api/review/stats')
      statistics.value.pendingReviews = (reviewStats.pending || 0) + (reviewStats.reviewing || 0)
    } catch (reviewError) {
      console.warn('Failed to load review statistics:', reviewError)
      statistics.value.pendingReviews = 0
    }
    
  } catch (error) {
    console.error('Failed to load statistics:', error)
  }
}

// 快捷跳转
const goToNews = () => router.push('/admin/news')
const goToCrawler = () => router.push('/admin/crawler')
const goToReview = () => router.push('/admin/review')
const goToCategories = () => router.push('/admin/categories')
const goToRules = () => router.push('/admin/rules')

onMounted(() => {
  updateTime()
  loadStatistics()
  setInterval(updateTime, 60000) // 每分钟更新时间
})
</script>

<style scoped>
.dashboard {
  width: 100%;
  animation: fadeIn 0.5s ease;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.welcome-text h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  border: none;
  overflow: hidden;
  position: relative;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: currentColor;
}

.stat-card-blue {
  color: #409eff;
}

.stat-card-green {
  color: #67c23a;
}

.stat-card-orange {
  color: #e6a23c;
}

.stat-card-purple {
  color: #9c27b0;
}

.stat-card-red {
  color: #f56c6c;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 8px 0;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.stat-icon-blue {
  background: linear-gradient(135deg, #e3f2ff 0%, #409eff 100%);
  color: #409eff;
}

.stat-icon-green {
  background: linear-gradient(135deg, #f0f9ff 0%, #67c23a 100%);
  color: #67c23a;
}

.stat-icon-orange {
  background: linear-gradient(135deg, #fff7e6 0%, #e6a23c 100%);
  color: #e6a23c;
}

.stat-icon-purple {
  background: linear-gradient(135deg, #f3e5f5 0%, #9c27b0 100%);
  color: #9c27b0;
}

.stat-icon-red {
  background: linear-gradient(135deg, #fef0f0 0%, #f56c6c 100%);
  color: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-desc {
  font-size: 12px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 内容行 */
.content-row {
  margin-bottom: 24px;
}

/* 快捷操作卡片 */
.action-card {
  border-radius: 12px;
  height: 100%;
  min-height: 400px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quick-link-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-link-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.link-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.link-icon-blue {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.link-icon-green {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.link-icon-orange {
  background: linear-gradient(135deg, #e6a23c 0%, #f56c6c 100%);
}

.link-icon-purple {
  background: linear-gradient(135deg, #9c27b0 0%, #ba68c8 100%);
}

.link-info {
  flex: 1;
}

.link-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.link-desc {
  font-size: 13px;
  color: #909399;
}

.link-arrow {
  font-size: 18px;
  color: #c0c4cc;
  transition: all 0.3s ease;
}

.quick-link-item:hover .link-arrow {
  color: #409eff;
  transform: translateX(4px);
}

/* 最近活动卡片 */
.activity-card {
  border-radius: 12px;
  height: 100%;
  min-height: 400px;
}

.activity-item {
  padding: 4px 0;
}

.activity-text {
  font-size: 14px;
  color: #606266;
}

/* 系统信息卡片 */
.system-card {
  border-radius: 12px;
}

.info-item {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.info-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .welcome-banner {
    padding: 20px;
  }
  
  .welcome-text h1 {
    font-size: 22px;
  }
  
  .stat-value {
    font-size: 24px;
  }
}

/* 审核状态样式 */
.urgent {
  color: #f56c6c;
  font-weight: 600;
}

.normal {
  color: #67c23a;
  font-weight: 500;
}
</style>

