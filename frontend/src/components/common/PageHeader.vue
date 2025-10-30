<template>
  <div class="page-header fade-in-up">
    <div class="header-content">
      <div class="header-main">
        <h1 class="page-title">{{ title }}</h1>
        <p v-if="description" class="page-description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="header-actions">
        <slot name="actions"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  description?: string
}

defineProps<Props>()
</script>

<style scoped lang="scss">
.page-header {
  margin-bottom: 24px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-main {
    flex: 1;
  }
  
  .page-title {
    margin: 0 0 8px;
    font-size: 28px;
    font-weight: 700;
    color: #000;
    line-height: 1.2;
  }
  
  .page-description {
    margin: 0;
    color: #6b7280;
    font-size: 14px;
    line-height: 1.6;
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
    flex-shrink: 0;
  }
}

@media (max-width: 768px) {
  .page-header {
    .header-content {
      flex-direction: column;
    }
    
    .page-title {
      font-size: 24px;
    }
    
    .header-actions {
      width: 100%;
      justify-content: flex-start;
    }
  }
}
</style>




