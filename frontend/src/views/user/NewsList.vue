<template>
  <div class="news-list-page">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <h1 class="logo">新闻网</h1>
        <nav class="nav">
          <a 
            v-for="cat in categories" 
            :key="cat.id"
            :class="['nav-item', { active: currentCategory === cat.id }]"
            @click="selectCategory(cat.id)"
          >
            {{ cat.name }}
          </a>
        </nav>
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索新闻..."
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
        <div class="user-menu">
          <template v-if="isLoggedIn">
            <el-dropdown>
              <span class="user-dropdown">
                <el-icon><User /></el-icon>
                {{ currentUsername }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="goToUserCenter">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" @click="goToAdmin">
                    <el-icon><Setting /></el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button size="small" @click="goToLogin">登录</el-button>
            <el-button type="primary" size="small" @click="goToRegister">
              注册
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主要内容区 -->
    <div class="main-content">
      <div class="container">
        <!-- 新闻列表 -->
        <div class="news-list">
          <div class="list-header">
            <h2>{{ currentCategoryName }}</h2>
            <el-radio-group v-model="sortType" size="small" @change="loadNews">
              <el-radio-button value="smart">智能排序</el-radio-button>
              <el-radio-button value="hot">热门</el-radio-button>
              <el-radio-button value="latest">最新</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>

          <!-- 新闻卡片 -->
          <div v-else class="news-items">
            <article 
              v-for="news in newsList" 
              :key="news.id"
              class="news-card"
              @click="viewNewsDetail(news.id)"
            >
              <div v-if="news.imageUrl" class="news-image">
                <img :src="news.imageUrl" :alt="news.title" />
              </div>
              <div class="news-content">
                <h3 class="news-title">{{ news.title }}</h3>
                <p class="news-snippet">
                  {{ news.content.substring(0, 150) }}...
                </p>
                <div class="news-meta">
                  <span class="source">{{ news.sourceWebsite }}</span>
                  <span class="time">{{ formatTime(news.publishTime) }}</span>
                  <span class="views">
                    <el-icon><View /></el-icon>
                    {{ news.viewCount }}
                  </span>
                  <span v-if="news.likeCount" class="likes">
                    <el-icon><Star /></el-icon>
                    {{ news.likeCount }}
                  </span>
                  <span v-if="news.commentCount" class="comments">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ news.commentCount }}
                  </span>
                </div>
              </div>
            </article>

            <!-- 空状态 -->
            <el-empty 
              v-if="newsList.length === 0 && !loading" 
              description="暂无新闻"
            />
          </div>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="totalElements"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="loadNews"
              @size-change="loadNews"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, Loading, View, User, Setting, SwitchButton,
  Star, ChatDotRound
} from '@element-plus/icons-vue'
import { getNewsList, getCategories, searchNews, getHotNews, getLatestNews, type News, type Category } from '@/api/news'
import { useUserStore } from '@/stores/user'
import { logout } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// 数据
const newsList = ref<News[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const currentCategory = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(20)
const totalElements = ref(0)
const sortType = ref('smart')
const searchKeyword = ref('')

// 计算属性
const currentCategoryName = computed(() => {
  if (!currentCategory.value) return '全部新闻'
  const cat = categories.value.find(c => c.id === currentCategory.value)
  return cat ? cat.name : '全部新闻'
})

// 用户相关
const isLoggedIn = computed(() => {
  return !!localStorage.getItem('news_management_token')
})

const currentUsername = computed(() => {
  return userStore.user?.username || '用户'
})

const isAdmin = computed(() => {
  return userStore.user?.role === 'ADMIN'
})

// 加载分类
async function loadCategories() {
  try {
    const data = await getCategories()
    categories.value = data
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

// 加载新闻
async function loadNews() {
  loading.value = true
  try {
    let response

    if (searchKeyword.value) {
      // 搜索模式
      response = await searchNews({
        keyword: searchKeyword.value,
        page: currentPage.value - 1,
        size: pageSize.value
      })
    } else if (sortType.value === 'hot') {
      // 热门
      response = await getHotNews({
        page: currentPage.value - 1,
        size: pageSize.value
      })
    } else if (sortType.value === 'latest') {
      // 最新
      response = await getLatestNews({
        page: currentPage.value - 1,
        size: pageSize.value
      })
    } else {
      // 智能排序（默认）
      response = await getNewsList({
        page: currentPage.value - 1,
        size: pageSize.value,
        categoryId: currentCategory.value || undefined
      })
    }

    newsList.value = response.content
    totalElements.value = response.totalElements
  } catch (error) {
    ElMessage.error('加载新闻失败')
  } finally {
    loading.value = false
  }
}

// 选择分类
function selectCategory(categoryId: number | null) {
  currentCategory.value = categoryId
  currentPage.value = 1
  searchKeyword.value = ''
  loadNews()
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  currentCategory.value = null
  loadNews()
}

// 查看新闻详情
function viewNewsDetail(id: number) {
  router.push(`/news/${id}`)
}

// 格式化时间
function formatTime(time: string) {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

// 用户相关方法
function goToLogin() {
  router.push('/login')
}

function goToRegister() {
  router.push('/register')
}

function goToUserCenter() {
  router.push('/user/center')
}

function goToAdmin() {
  router.push('/admin')
}

async function handleLogout() {
  try {
    await logout()
    localStorage.removeItem('news_management_token')
    localStorage.removeItem('news_management_refresh_token')
    userStore.clearUser()
    ElMessage.success('已退出登录')
    // 刷新页面
    window.location.reload()
  } catch (error) {
    ElMessage.error('退出失败')
  }
}

// 初始化
onMounted(() => {
  loadCategories()
  loadNews()
})
</script>

<style scoped>
.news-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 头部 */
.header {
  background: #c00;
  color: white;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 30px;
}

.logo {
  font-size: 28px;
  font-weight: bold;
  margin: 0;
  cursor: pointer;
}

/* 导航 */
.nav {
  display: flex;
  gap: 20px;
  flex: 1;
}

.nav-item {
  color: white;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.2);
}

.nav-item.active {
  background: rgba(255,255,255,0.3);
  font-weight: bold;
}

/* 搜索框 */
.search-box {
  width: 300px;
}

/* 用户菜单 */
.user-menu {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-left: 20px;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: white;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: rgba(255, 255, 255, 0.2);
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 主内容 */
.main-content {
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 列表头部 */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: white;
  border-radius: 8px;
}

.list-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
  color: #666;
  font-size: 16px;
}

.loading .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

/* 新闻卡片 */
.news-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.news-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e8e8e8;
  display: flex;
  gap: 20px;
}

.news-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.news-image {
  flex-shrink: 0;
  width: 200px;
  height: 150px;
  border-radius: 6px;
  overflow: hidden;
}

.news-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.news-card:hover .news-image img {
  transform: scale(1.05);
}

.news-content {
  flex: 1;
  min-width: 0;
}

.news-title {
  font-size: 20px;
  color: #333;
  margin: 0 0 12px 0;
  font-weight: 600;
  line-height: 1.4;
}

.news-snippet {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.news-meta {
  display: flex;
  gap: 20px;
  color: #999;
  font-size: 13px;
  align-items: center;
}

.source {
  color: #c00;
  font-weight: 500;
}

.views {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页 */
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
}
</style>

