<template>
  <section id="categories" class="py-20 px-6 bg-gradient-to-br from-gray-50 to-white">
    <div class="max-w-7xl mx-auto">
      <!-- 标题 -->
      <div class="text-center mb-16">
        <h2 class="text-4xl md:text-6xl font-bold mb-4">新闻分类</h2>
        <p class="text-lg md:text-xl text-gray-600 max-w-2xl mx-auto">
          选择您感兴趣的分类，浏览相关新闻资讯
        </p>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="text-center py-12">
        <el-icon class="is-loading" :size="48"><Loading /></el-icon>
        <p class="text-gray-600 mt-4">加载中...</p>
      </div>

      <!-- 分类网格 -->
      <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        <router-link
          v-for="category in categories"
          :key="category.id"
          :to="`/category/${category.id}`"
          class="group relative bg-white rounded-2xl p-8 shadow-sm hover:shadow-2xl transition-all duration-300 border-2 border-transparent hover:border-black overflow-hidden"
        >
          <!-- 背景装饰 -->
          <div class="absolute inset-0 bg-gradient-to-br from-black/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          
          <!-- 内容 -->
          <div class="relative z-10">
            <!-- 图标 -->
            <div class="w-16 h-16 mb-4 rounded-xl bg-black/5 group-hover:bg-black flex items-center justify-center transition-all duration-300">
              <span class="text-3xl group-hover:scale-110 transition-transform duration-300">
                {{ getCategoryIcon(category.name) }}
              </span>
            </div>
            
            <!-- 分类名称 -->
            <h3 class="text-xl font-bold mb-2 group-hover:translate-x-1 transition-transform duration-300">
              {{ category.name }}
            </h3>
            
            <!-- 描述 -->
            <p class="text-sm text-gray-600 mb-4 line-clamp-2">
              {{ category.description || '浏览该分类下的最新资讯' }}
            </p>
            
            <!-- 查看更多 -->
            <div class="flex items-center gap-2 text-sm font-medium text-black group-hover:gap-3 transition-all duration-300">
              <span>查看更多</span>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="5" y1="12" x2="19" y2="12"></line>
                <polyline points="12 5 19 12 12 19"></polyline>
              </svg>
            </div>
          </div>
          
          <!-- 角标装饰 -->
          <div class="absolute top-4 right-4 w-12 h-12 rounded-full bg-black/5 group-hover:bg-white/20 transition-all duration-300 group-hover:scale-125"></div>
        </router-link>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && categories.length === 0" class="text-center py-12">
        <div class="text-6xl mb-4">📂</div>
        <p class="text-xl text-gray-600">暂无分类</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCategories } from '@/api/news'
import { Loading } from '@element-plus/icons-vue'

interface Category {
  id: number
  name: string
  description: string
}

const categories = ref<Category[]>([])
const loading = ref(false)

const getCategoryIcon = (categoryName: string): string => {
  const iconMap: Record<string, string> = {
    '时政': '🏛️',
    '经济': '💹',
    '科技': '🔬',
    '文化': '🎭',
    '体育': '⚽',
    '娱乐': '🎬',
    '社会': '👥',
    '国际': '🌍',
    '军事': '🎖️',
    '教育': '📚',
    '健康': '🏥',
    '环境': '🌱',
    '旅游': '✈️',
    '汽车': '🚗',
    '房产': '🏠',
    '财经': '💰'
  }
  return iconMap[categoryName] || '📰'
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const data = await getCategories()
    categories.value = data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
    categories.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

