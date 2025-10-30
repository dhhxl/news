<template>
  <section class="relative min-h-screen flex items-center justify-center overflow-hidden pt-20 bg-gradient-to-b from-white via-gray-50 to-white">
    <div class="max-w-7xl mx-auto px-6 text-center">
      <!-- 主标题 -->
      <div class="mb-12 animate-fade-in">
        <h1 class="text-6xl md:text-8xl lg:text-9xl font-bold mb-6 leading-none">
          <span class="inline-block animate-slide-up">
            实时追踪
          </span>
          <br />
          <span class="inline-block animate-slide-up" style="animation-delay: 0.1s;">
            全球资讯
          </span>
        </h1>
      </div>

      <!-- 副标题 -->
      <p class="text-lg md:text-xl lg:text-2xl max-w-3xl mx-auto leading-relaxed mb-16 animate-slide-up" style="animation-delay: 0.3s;">
        作为专业新闻资讯平台，我们致力于为读者提供最及时、最权威的新闻报道。
        从突发新闻到深度分析，从本地热点到国际视野，为您呈现全方位的新闻内容。
      </p>

      <!-- 服务卡片 -->
      <div class="grid md:grid-cols-3 gap-6 max-w-5xl mx-auto animate-slide-up" style="animation-delay: 0.5s;">
        <div
          v-for="(service, index) in services"
          :key="service.title"
          class="bg-black text-white p-8 rounded-2xl transition-all duration-300 hover:-translate-y-3 hover:shadow-2xl cursor-pointer"
          :style="{ animationDelay: `${0.6 + index * 0.1}s` }"
        >
          <h3 class="text-2xl font-bold mb-4">{{ service.title }}</h3>
          <ul class="text-left space-y-2 text-sm opacity-80">
            <li v-for="(item, i) in service.items" :key="i">• {{ item }}</li>
          </ul>
        </div>
      </div>

      <!-- 滚动指示器 -->
      <div class="absolute bottom-10 left-1/2 transform -translate-x-1/2 animate-fade-in" style="animation-delay: 1s;">
        <div class="w-6 h-10 border-2 border-black rounded-full flex justify-center animate-pulse">
          <div class="w-1.5 h-1.5 bg-black rounded-full mt-2 animate-float"></div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
const services = [
  {
    title: '时政要闻',
    items: ['国内时政', '国际新闻', '政策解读', '领导动态', '地方要闻', '时事评论']
  },
  {
    title: '财经科技',
    items: ['股市行情', '经济数据', '科技创新', '企业动态', '产业分析', '创投资讯']
  },
  {
    title: '社会民生',
    items: ['社会热点', '民生关注', '文化教育', '健康医疗', '体育赛事', '娱乐八卦']
  }
]
</script>

<style scoped>
/* 动画效果在 tailwind.css 中定义 */
</style>

