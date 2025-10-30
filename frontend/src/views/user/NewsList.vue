<template>
  <div class="min-h-screen bg-gray-50">
    <ModernHeader />
    
    <div class="pt-24 pb-12 px-6">
      <div class="max-w-7xl mx-auto">
        <!-- 页面标题 -->
        <div class="mb-8">
          <h1 class="text-4xl md:text-5xl font-bold mb-4">新闻列表</h1>
          <p class="text-lg text-gray-600">浏览最新的新闻资讯</p>
        </div>

        <!-- 分类筛选 -->
        <div class="mb-8 flex flex-wrap gap-3">
          <button
            @click="selectedCategory = null"
            :class="[
              'px-6 py-2 rounded-full text-sm font-medium transition-all',
              selectedCategory === null
                ? 'bg-black text-white'
                : 'bg-white text-black hover:bg-gray-100 border border-gray-300'
            ]"
          >
            全部
          </button>
          <button
            v-for="category in categories"
            :key="category.id"
            @click="selectedCategory = category.id"
            :class="[
              'px-6 py-2 rounded-full text-sm font-medium transition-all',
              selectedCategory === category.id
                ? 'bg-black text-white'
                : 'bg-white text-black hover:bg-gray-100 border border-gray-300'
            ]"
          >
            {{ category.name }}
          </button>
        </div>

        <!-- 新闻网格 -->
        <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="news in filteredNews"
            :key="news.id"
            class="bg-white rounded-xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden cursor-pointer group"
            @click="goToDetail(news.id)"
          >
            <!-- 新闻图片 -->
            <div class="aspect-video bg-gray-200 overflow-hidden">
              <img
                v-if="news.imageUrl"
                :src="news.imageUrl"
                :alt="news.title"
                class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
              />
              <div v-else class="w-full h-full flex items-center justify-center">
                <span class="text-gray-400 text-4xl">📰</span>
              </div>
            </div>

            <!-- 新闻内容 -->
            <div class="p-6">
              <!-- 分类标签 -->
              <div class="mb-3">
                <span class="text-xs px-3 py-1 bg-black text-white rounded-full">
                  {{ news.categoryName }}
                </span>
              </div>

              <!-- 标题 -->
              <h3 class="text-xl font-bold mb-2 line-clamp-2 group-hover:text-gray-600 transition-colors">
                {{ news.title }}
              </h3>

              <!-- 摘要 -->
              <p class="text-sm text-gray-600 mb-4 line-clamp-3">
                {{ news.summary }}
              </p>

              <!-- 底部信息 -->
              <div class="flex items-center justify-between text-xs text-gray-500">
                <span>{{ formatDate(news.publishTime) }}</span>
                <div class="flex items-center gap-3">
                  <span>👁️ {{ news.viewCount || 0 }}</span>
                  <span>👍 {{ news.likeCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredNews.length === 0" class="text-center py-16">
          <div class="text-6xl mb-4">📭</div>
          <p class="text-xl text-gray-600">暂无新闻</p>
        </div>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="mt-12 flex justify-center">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>

    <ModernFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNewsList } from '@/api/news'
import ModernHeader from '@/components/modern/ModernHeader.vue'
import ModernFooter from '@/components/modern/ModernFooter.vue'

const router = useRouter()

interface News {
  id: number
  title: string
  summary: string
  imageUrl: string
  categoryName: string
  publishTime: string
  viewCount: number
  likeCount: number
}

interface Category {
  id: number
  name: string
}

const newsList = ref<News[]>([])
const categories = ref<Category[]>([])
const selectedCategory = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const filteredNews = computed(() => {
  if (selectedCategory.value === null) {
    return newsList.value
  }
  return newsList.value.filter(news => news.categoryName === categories.value.find(c => c.id === selectedCategory.value)?.name)
})

const fetchNews = async () => {
  try {
    const response = await getNewsList({
      page: currentPage.value - 1,
      size: pageSize.value,
      categoryId: selectedCategory.value,
      status: 'PUBLISHED'
    })
    newsList.value = response.content || []
    total.value = response.totalElements || 0
    
    // 提取分类信息
    const categorySet = new Set<string>()
    newsList.value.forEach(news => {
      if (news.categoryName) {
        categorySet.add(news.categoryName)
      }
    })
    
    categories.value = Array.from(categorySet).map((name, index) => ({
      id: index + 1,
      name
    }))
  } catch (error) {
    console.error('获取新闻列表失败:', error)
  }
}

const goToDetail = (id: number) => {
  router.push(`/news/${id}`)
}

const handlePageChange = () => {
  fetchNews()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

onMounted(() => {
  fetchNews()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
