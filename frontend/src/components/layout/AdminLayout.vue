<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="admin-sidebar">
      <div class="logo">
        <div class="logo-icon">📰</div>
        <div class="logo-text">
          <h2>新闻管理系统</h2>
          <p>News Management</p>
        </div>
      </div>
      
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="currentRoute"
          router
          class="admin-menu"
          :collapse="false"
        >
          <el-menu-item index="/admin" class="menu-item">
            <el-icon class="menu-icon"><HomeFilled /></el-icon>
            <template #title>
              <span class="menu-title">控制台</span>
            </template>
          </el-menu-item>
          
          <el-menu-item index="/admin/news" class="menu-item">
            <el-icon class="menu-icon"><Document /></el-icon>
            <template #title>
              <span class="menu-title">新闻管理</span>
            </template>
          </el-menu-item>
          
          <el-menu-item index="/admin/categories" class="menu-item">
            <el-icon class="menu-icon"><Folder /></el-icon>
            <template #title>
              <span class="menu-title">分类管理</span>
            </template>
          </el-menu-item>
          
          <el-menu-item index="/admin/crawler" class="menu-item">
            <el-icon class="menu-icon"><Download /></el-icon>
            <template #title>
              <span class="menu-title">采集管理</span>
            </template>
          </el-menu-item>
          
          <el-menu-item index="/admin/rules" class="menu-item">
            <el-icon class="menu-icon"><Setting /></el-icon>
            <template #title>
              <span class="menu-title">分类规则</span>
            </template>
          </el-menu-item>
          
          <el-menu-item index="/admin/audit-log" class="menu-item">
            <el-icon class="menu-icon"><List /></el-icon>
            <template #title>
              <span class="menu-title">审计日志</span>
            </template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
      
      <!-- 侧边栏底部信息 -->
      <div class="sidebar-footer">
        <div class="version-info">
          <span>v1.0.0</span>
        </div>
      </div>
    </el-aside>
    
    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/admin' }">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">
              {{ currentTitle }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-tooltip content="前往前台" placement="bottom">
            <el-button link @click="goToFrontend">
              <el-icon><Link /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-divider direction="vertical" />
          
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <span class="user-info">
              <el-avatar :size="32" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ username }}</span>
              <el-icon class="arrow-down"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <div class="dropdown-user-info">
                    <div class="user-name">{{ username }}</div>
                    <div class="user-role">管理员</div>
                  </div>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="admin-main">
        <transition name="fade-transform" mode="out-in">
          <router-view />
        </transition>
      </el-main>
      
      <!-- 底部版权信息 -->
      <el-footer class="admin-footer">
        <div class="footer-content">
          <span>© 2025 新闻管理系统</span>
          <span class="separator">|</span>
          <span>Powered by Spring Boot + Vue 3</span>
        </div>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { 
  HomeFilled, 
  Document, 
  Folder, 
  Download, 
  Setting, 
  List, 
  User,
  Link,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const currentRoute = computed(() => route.path);
const currentTitle = computed(() => route.meta.title as string);
const username = computed(() => userStore.userInfo?.username || 'Admin');

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  }
};

const goToFrontend = () => {
  window.open('/', '_blank');
};
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏样式 */
.admin-sidebar {
  background: linear-gradient(180deg, #1a2332 0%, #263445 100%);
  color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.logo {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(0, 0, 0, 0.2);
  padding: 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 36px;
  animation: pulse 2s ease-in-out infinite;
}

.logo-text h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
  font-weight: 600;
  letter-spacing: 1px;
}

.logo-text p {
  margin: 2px 0 0 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
  text-transform: uppercase;
  letter-spacing: 1.5px;
}

.menu-scrollbar {
  flex: 1;
  overflow-y: auto;
}

.admin-menu {
  border-right: none;
  background-color: transparent;
}

.admin-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.85);
}

.admin-menu :deep(.el-menu-item:hover) {
  color: #fff;
}

.admin-menu :deep(.el-menu-item.is-active) {
  color: #fff;
}

.menu-item {
  margin: 4px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.menu-item:hover {
  background: rgba(64, 158, 255, 0.15) !important;
  transform: translateX(4px);
}

.menu-item.is-active {
  background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%) !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.menu-icon {
  font-size: 18px;
  margin-right: 8px;
  color: inherit;
}

.menu-title {
  font-size: 14px;
  font-weight: 500;
  color: inherit;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  text-align: center;
}

.version-info {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* 主容器样式 */
.main-container {
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 顶部导航栏 */
.admin-header {
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 10;
}

.breadcrumb {
  font-size: 14px;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.arrow-down {
  font-size: 12px;
  color: #909399;
  transition: transform 0.3s ease;
}

.user-info:hover .arrow-down {
  transform: rotate(180deg);
}

.dropdown-user-info {
  padding: 8px 0;
  text-align: center;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.user-role {
  font-size: 12px;
  color: #909399;
}

/* 主内容区 */
.admin-main {
  flex: 1;
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* 底部版权 */
.admin-footer {
  height: 50px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
}

.footer-content {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 12px;
}

.separator {
  color: #dcdfe6;
}

/* Logo脉冲动画 */
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 滚动条美化 */
.menu-scrollbar :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
}

.menu-scrollbar :deep(.el-scrollbar__thumb) {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

.menu-scrollbar :deep(.el-scrollbar__thumb:hover) {
  background: rgba(255, 255, 255, 0.3);
}
</style>

