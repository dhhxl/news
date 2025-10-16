<template>
  <div class="user-layout">
    <el-header class="user-header">
      <div class="header-container">
        <div class="logo">
          <h1>{{ appTitle }}</h1>
        </div>
        <el-menu
          :default-active="currentRoute"
          mode="horizontal"
          router
          class="nav-menu"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-sub-menu index="2">
            <template #title>新闻分类</template>
            <el-menu-item
              v-for="category in categories"
              :key="category.id"
              :index="`/category/${category.id}`"
            >
              {{ category.name }}
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
        <div class="header-actions">
          <el-button
            v-if="!isLoggedIn"
            type="primary"
            @click="goToLogin"
          >
            登录
          </el-button>
          <el-button
            v-else
            type="primary"
            @click="goToAdmin"
          >
            管理后台
          </el-button>
        </div>
      </div>
    </el-header>
    
    <el-main class="user-main">
      <div class="main-container">
        <router-view />
      </div>
    </el-main>
    
    <el-footer class="user-footer">
      <div class="footer-container">
        <p>&copy; 2025 {{ appTitle }}. All rights reserved.</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const appTitle = '新闻管理系统';
const currentRoute = computed(() => route.path);
const isLoggedIn = computed(() => userStore.isLoggedIn);

// TODO: 从API获取分类列表
const categories = ref([
  { id: 1, name: '政治' },
  { id: 2, name: '经济' },
  { id: 3, name: '体育' },
  { id: 4, name: '娱乐' },
  { id: 5, name: '科技' },
  { id: 6, name: '社会' }
]);

const goToLogin = () => {
  router.push('/login');
};

const goToAdmin = () => {
  router.push('/admin');
};

onMounted(() => {
  // TODO: 加载分类列表
});
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.user-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.logo h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.nav-menu {
  flex: 1;
  margin: 0 40px;
  border-bottom: none;
}

.user-main {
  flex: 1;
  background-color: #f5f5f5;
  padding: 20px;
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
}

.user-footer {
  background-color: #333;
  color: #fff;
  text-align: center;
  padding: 20px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>

