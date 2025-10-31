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
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" :xs="12">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon class="stat-icon" style="color: #fff"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">新闻总数</div>
              <div class="stat-value">{{ overview.totalNews || 0 }}</div>
              <div class="stat-desc">
                <el-icon><TrendCharts /></el-icon>
                今日新增 {{ overview.todayNews || 0 }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon class="stat-icon" style="color: #fff"><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总浏览量</div>
              <div class="stat-value">{{ formatNumber(overview.totalViews) }}</div>
              <div class="stat-desc">今日 {{ formatNumber(overview.todayViews) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon class="stat-icon" style="color: #fff"><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">分类数量</div>
              <div class="stat-value">{{ overview.totalCategories || 0 }}</div>
              <div class="stat-desc">已发布 {{ overview.publishedNews || 0 }} 篇</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon-wrapper" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
              <el-icon class="stat-icon" style="color: #fff"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">待审核</div>
              <div class="stat-value">{{ overview.pendingReviews || 0 }}</div>
              <div class="stat-desc">
                <span v-if="overview.pendingReviews > 0" class="urgent">需要处理</span>
                <span v-else class="normal">暂无待审</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 新闻趋势图 -->
      <el-col :span="12" :xs="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><TrendCharts /></el-icon>
                新闻发布趋势（最近7天）
              </span>
              <el-tag type="info" size="small">每日统计</el-tag>
            </div>
          </template>
          <div ref="newsTrendChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 浏览量趋势图 -->
      <el-col :span="12" :xs="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><View /></el-icon>
                浏览量趋势（最近7天）
              </span>
              <el-tag type="success" size="small">实时数据</el-tag>
            </div>
          </template>
          <div ref="viewTrendChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <!-- 分类分布饼图 -->
      <el-col :span="12" :xs="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><PieChart /></el-icon>
                新闻分类分布
              </span>
              <el-tag type="warning" size="small">占比分析</el-tag>
            </div>
          </template>
          <div ref="categoryChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 来源分布柱状图 -->
      <el-col :span="12" :xs="24">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><DataLine /></el-icon>
                新闻来源分布
              </span>
              <el-tag type="danger" size="small">数量统计</el-tag>
            </div>
          </template>
          <div ref="sourceChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import {
  Document,
  Download,
  Folder,
  View,
  TrendCharts,
  CircleCheck,
  PieChart,
  DataLine
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const username = computed(() => userStore.userInfo?.username || 'Admin')
const currentTime = ref('')

// 图表引用
const newsTrendChart = ref<HTMLElement>()
const viewTrendChart = ref<HTMLElement>()
const categoryChart = ref<HTMLElement>()
const sourceChart = ref<HTMLElement>()

// 图表实例
let newsTrendChartInstance: echarts.ECharts | null = null
let viewTrendChartInstance: echarts.ECharts | null = null
let categoryChartInstance: echarts.ECharts | null = null
let sourceChartInstance: echarts.ECharts | null = null

// 统计数据
const overview = ref<any>({})

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num
}

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

// 加载概览数据
const loadOverview = async () => {
  try {
    const response = await request.get('/api/statistics/overview')
    overview.value = response
  } catch (error) {
    console.error('Failed to load overview:', error)
  }
}

// 初始化新闻趋势图
const initNewsTrendChart = async () => {
  if (!newsTrendChart.value) return

  try {
    const data = await request.get('/api/statistics/news-trend')

    newsTrendChartInstance = echarts.init(newsTrendChart.value)

    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.dates,
        axisTick: {
          alignWithLabel: true
        }
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '新闻数量',
          type: 'bar',
          barWidth: '60%',
          data: data.counts,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#764ba2' }
            ])
          }
        }
      ]
    }

    newsTrendChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to init news trend chart:', error)
  }
}

// 初始化浏览量趋势图
const initViewTrendChart = async () => {
  if (!viewTrendChart.value) return

  try {
    const data = await request.get('/api/statistics/view-trend')

    viewTrendChartInstance = echarts.init(viewTrendChart.value)

    const option = {
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: data.dates
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '浏览量',
          type: 'line',
          smooth: true,
          data: data.views,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(79, 172, 254, 0.4)' },
              { offset: 1, color: 'rgba(0, 242, 254, 0.1)' }
            ])
          },
          lineStyle: {
            color: '#4facfe',
            width: 3
          },
          itemStyle: {
            color: '#4facfe'
          }
        }
      ]
    }

    viewTrendChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to init view trend chart:', error)
  }
}

// 初始化分类分布饼图
const initCategoryChart = async () => {
  if (!categoryChart.value) return

  try {
    const data = await request.get('/api/statistics/category-distribution')

    categoryChartInstance = echarts.init(categoryChart.value)

    const chartData = data.names.map((name: string, index: number) => ({
      name: name,
      value: data.values[index]
    }))

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '新闻分类',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          label: {
            show: true,
            formatter: '{b}: {d}%'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          data: chartData,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          }
        }
      ],
      color: ['#667eea', '#f093fb', '#4facfe', '#fa709a', '#fee140', '#30cfd0']
    }

    categoryChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to init category chart:', error)
  }
}

// 初始化来源分布柱状图
const initSourceChart = async () => {
  if (!sourceChart.value) return

  try {
    const data = await request.get('/api/statistics/source-distribution')

    sourceChartInstance = echarts.init(sourceChart.value)

    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        boundaryGap: [0, 0.01]
      },
      yAxis: {
        type: 'category',
        data: data.sources
      },
      series: [
        {
          name: '新闻数量',
          type: 'bar',
          data: data.counts,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#fa709a' },
              { offset: 1, color: '#fee140' }
            ]),
            borderRadius: [0, 10, 10, 0]
          },
          label: {
            show: true,
            position: 'right'
          }
        }
      ]
    }

    sourceChartInstance.setOption(option)
  } catch (error) {
    console.error('Failed to init source chart:', error)
  }
}

// 监听窗口大小变化
const handleResize = () => {
  newsTrendChartInstance?.resize()
  viewTrendChartInstance?.resize()
  categoryChartInstance?.resize()
  sourceChartInstance?.resize()
}

// 快捷跳转
const goToNews = () => router.push('/admin/news')
const goToCrawler = () => router.push('/admin/crawler')

onMounted(async () => {
  updateTime()
  setInterval(updateTime, 60000)

  await loadOverview()
  
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(async () => {
    await Promise.all([
      initNewsTrendChart(),
      initViewTrendChart(),
      initCategoryChart(),
      initSourceChart()
    ])

    window.addEventListener('resize', handleResize)
  }, 300)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  newsTrendChartInstance?.dispose()
  viewTrendChartInstance?.dispose()
  categoryChartInstance?.dispose()
  sourceChartInstance?.dispose()
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

.urgent {
  color: #f56c6c;
  font-weight: 600;
}

.normal {
  color: #67c23a;
  font-weight: 500;
}

/* 图表区域 */
.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px;
  margin-bottom: 20px;
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

.chart-container {
  width: 100%;
  height: 350px;
}

/* 动画 */
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
  
  .chart-container {
    height: 300px;
  }
}
</style>

