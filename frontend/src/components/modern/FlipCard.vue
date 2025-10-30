<template>
  <div
    class="h-[500px] perspective-1000"
    @mouseenter="isFlipped = true"
    @mouseleave="isFlipped = false"
  >
    <div
      class="relative w-full h-full transition-transform duration-600 preserve-3d"
      :style="{ transform: isFlipped ? 'rotateY(180deg)' : 'rotateY(0deg)' }"
    >
      <!-- 卡片正面 -->
      <div
        class="absolute w-full h-full backface-hidden"
        style="backface-visibility: hidden;"
      >
        <div
          class="rounded-3xl p-10 h-full flex flex-col justify-center items-center shadow-2xl transition-all hover:shadow-3xl hover:-translate-y-2 border-2"
          :style="{
            background: `linear-gradient(135deg, ${service.gradientFrom} 0%, ${service.gradientTo} 100%)`,
            borderColor: 'rgba(255, 255, 255, 0.1)'
          }"
        >
          <!-- 图标容器 -->
          <div class="relative mb-8">
            <!-- 图标背景 -->
            <div 
              class="relative w-32 h-32 rounded-full flex items-center justify-center border-2"
              :style="{
                background: 'rgba(255, 255, 255, 0.05)',
                borderColor: 'rgba(255, 255, 255, 0.2)',
                backdropFilter: 'blur(10px)'
              }"
            >
              <el-icon 
                :size="64" 
                color="#ffffff"
                class="animate-float"
              >
                <component :is="service.iconComponent" />
              </el-icon>
            </div>
          </div>
          
          <!-- 文字内容 -->
          <div class="text-center text-white">
            <h3 class="text-4xl font-bold mb-4 tracking-tight">
              {{ service.title }}
            </h3>
            <p class="text-base opacity-90 leading-relaxed max-w-xs mx-auto">
              {{ service.description }}
            </p>
            <div class="mt-8 flex items-center justify-center gap-2 text-sm opacity-70">
              <el-icon><ArrowRight /></el-icon>
              <span>悬停查看详情</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 卡片背面 -->
      <div
        class="absolute w-full h-full backface-hidden"
        style="backface-visibility: hidden; transform: rotateY(180deg);"
      >
        <div class="bg-white rounded-3xl p-8 h-full overflow-y-auto shadow-2xl border-2 border-black">
          <!-- 背面标题 -->
          <div class="text-center mb-6 pb-4 border-b-2 border-black">
            <el-icon :size="40" color="#000000" class="mb-3">
              <component :is="service.iconComponent" />
            </el-icon>
            <h3 class="text-2xl font-bold text-black">
              {{ service.title }}
            </h3>
          </div>
          
          <!-- 功能列表 -->
          <ul class="space-y-3">
            <li
              v-for="(item, i) in service.items"
              :key="i"
              class="group flex flex-col p-4 rounded-xl transition-all duration-300 cursor-pointer border-2"
              :class="hoveredItem === i ? 'bg-black border-black' : 'bg-gray-50 border-gray-200 hover:border-gray-400'"
              @mouseenter="hoveredItem = i"
              @mouseleave="hoveredItem = -1"
            >
              <div class="flex items-start gap-3">
                <!-- 序号图标 -->
                <div 
                  class="flex-shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-all"
                  :class="hoveredItem === i ? 'bg-white text-black' : 'bg-black text-white'"
                >
                  {{ i + 1 }}
                </div>
                
                <div class="flex-1">
                  <span 
                    class="font-bold text-sm mb-1 block transition-colors"
                    :class="hoveredItem === i ? 'text-white' : 'text-black'"
                  >
                    {{ item.name }}
                  </span>
                  <span 
                    class="text-xs block transition-colors"
                    :class="hoveredItem === i ? 'text-gray-300' : 'text-gray-600'"
                  >
                    {{ item.desc }}
                  </span>
                </div>
              </div>
            </li>
          </ul>
          
          <!-- 底部提示 -->
          <div class="mt-6 text-center text-xs text-gray-400 flex items-center justify-center gap-2">
            <el-icon><WarningFilled /></el-icon>
            <span>鼠标移开返回</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { 
  Reading, 
  DataAnalysis, 
  UserFilled, 
  ArrowRight, 
  WarningFilled 
} from '@element-plus/icons-vue'

interface ServiceItem {
  name: string
  desc: string
}

interface Service {
  title: string
  iconComponent: string
  iconColor: string
  gradientFrom: string
  gradientTo: string
  description: string
  items: ServiceItem[]
}

interface Props {
  service: Service
  index: number
}

defineProps<Props>()

const isFlipped = ref(false)
const hoveredItem = ref(-1)
</script>

<style scoped>
.perspective-1000 {
  perspective: 1000px;
}

.preserve-3d {
  transform-style: preserve-3d;
}

.backface-hidden {
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.duration-600 {
  transition-duration: 600ms;
}

/* 浮动动画 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.animate-float {
  animation: float 3s ease-in-out infinite;
}

/* 阴影增强 */
.shadow-3xl {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.35);
}

/* 列表项悬停效果 */
.group:hover {
  transform: translateX(4px);
}
</style>




