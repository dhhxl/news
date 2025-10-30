<template>
  <div class="stat-card">
    <div class="stat-icon" :style="{ background: iconBg }">
      {{ icon }}
    </div>
    <div class="stat-value">{{ value }}</div>
    <div class="stat-label">{{ label }}</div>
    <div v-if="trend" class="stat-trend" :class="trendClass">
      <span class="trend-icon">{{ trendIcon }}</span>
      <span class="trend-text">{{ trend }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  icon: string
  value: string | number
  label: string
  iconBg?: string
  trend?: string
  trendType?: 'up' | 'down' | 'neutral'
}

const props = withDefaults(defineProps<Props>(), {
  iconBg: '#f3f4f6',
  trendType: 'neutral'
})

const trendClass = computed(() => {
  return `trend-${props.trendType}`
})

const trendIcon = computed(() => {
  if (props.trendType === 'up') return '📈'
  if (props.trendType === 'down') return '📉'
  return '➡️'
})
</script>

<style scoped lang="scss">
.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    transform: translateY(-4px);
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-bottom: 16px;
  }
  
  .stat-value {
    font-size: 32px;
    font-weight: 700;
    color: #000;
    margin-bottom: 8px;
    line-height: 1;
  }
  
  .stat-label {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
    margin-bottom: 12px;
  }
  
  .stat-trend {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    
    .trend-icon {
      font-size: 16px;
    }
    
    &.trend-up {
      color: #10b981;
    }
    
    &.trend-down {
      color: #ef4444;
    }
    
    &.trend-neutral {
      color: #6b7280;
    }
  }
}
</style>




