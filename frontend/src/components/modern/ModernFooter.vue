<template>
  <footer id="footer" class="bg-white border-t border-gray-200 py-12 px-6">
    <div class="max-w-7xl mx-auto">
      <div class="grid md:grid-cols-3 gap-8 mb-8">
        <!-- Logo 和描述 -->
        <div>
          <h3 class="text-2xl font-bold mb-4">新闻资讯网</h3>
          <p class="text-sm opacity-70 leading-relaxed">
            专业新闻资讯平台<br />
            实时新闻 | 深度报道 | 权威评论 | 互动社区
          </p>
        </div>

        <!-- 快速导航 -->
        <div>
          <h4 class="font-semibold mb-4">快速导航</h4>
          <ul class="space-y-2">
            <li v-for="link in navLinks" :key="link.name">
              <a :href="link.href" class="text-sm opacity-70 hover:opacity-100 transition-opacity">
                {{ link.name }}
              </a>
            </li>
          </ul>
        </div>

        <!-- 联系我们 -->
        <div>
          <h4 class="font-semibold mb-4">联系我们</h4>
          <ul class="space-y-2">
            <li class="text-sm opacity-70">邮箱: contact@news.com</li>
            <li class="text-sm opacity-70">电话: 400-123-4567</li>
            <li class="text-sm opacity-70">地址: 北京市朝阳区</li>
          </ul>
        </div>
      </div>

      <!-- 底部链接 -->
      <div class="pt-8 border-t border-gray-200 flex flex-col md:flex-row justify-between items-center gap-4">
        <p class="text-sm opacity-70">
          © {{ currentYear }} 新闻资讯网. 保留所有权利.
        </p>
        <div class="flex gap-6">
          <a
            v-for="link in legalLinks"
            :key="link.name"
            :href="link.url"
            class="text-sm opacity-70 hover:opacity-100 transition-opacity"
          >
            {{ link.name }}
          </a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())

const navLinks = [
  { name: '首页', href: '/' },
  { name: '新闻分类', href: '#categories' },
  { name: '热门话题', href: '#trending' },
  { name: '关于我们', href: '#about' }
]

const legalLinks = [
  { name: '关于我们', url: '#about' },
  { name: '隐私政策', url: '#privacy' },
  { name: '版权声明', url: '#copyright' },
  { name: '联系方式', url: '#contact' }
]
</script>




