<template>
  <div class="min-h-screen text-black bg-white">
    <!-- 滚动进度条 -->
    <div
      class="fixed top-0 left-0 h-1 bg-gradient-to-r from-black via-gray-600 to-black z-50 transition-all origin-left"
      :style="{ width: scrollProgress + '%' }"
    />
    
    <ModernHeader />
    
    <main>
      <ModernHero />
      <ServiceSection />
      <TrendingSection />
    </main>
    
    <ModernFooter />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import ModernHeader from '@/components/modern/ModernHeader.vue'
import ModernHero from '@/components/modern/ModernHero.vue'
import ServiceSection from '@/components/modern/ServiceSection.vue'
import TrendingSection from '@/components/modern/TrendingSection.vue'
import ModernFooter from '@/components/modern/ModernFooter.vue'

const scrollProgress = ref(0)

const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  scrollProgress.value = (scrollTop / docHeight) * 100
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* 页面特定样式 */
</style>




