<template>
  <el-container class="editor-layout">
    <!-- 顶部导航 -->
    <el-header class="editor-header">
      <div class="header-left">
        <h2 class="system-title">新闻编辑系统</h2>
      </div>
      
      <div class="header-right">
        <!-- 用户信息 -->
        <el-dropdown @command="handleUserCommand" trigger="click">
          <div class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
            <span class="username">{{ userStore.userInfo?.username }}</span>
            <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile" :icon="User">
                个人资料
              </el-dropdown-item>
              <el-dropdown-item command="logout" :icon="SwitchButton" divided>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container>
      <!-- 侧边导航 -->
      <el-aside width="240px" class="editor-aside">
        <el-menu
          :default-active="activeMenu"
          class="editor-menu"
          router
        >
          <el-menu-item index="/editor/news-list">
            <el-icon><Document /></el-icon>
            <span>我的新闻</span>
          </el-menu-item>
          
          <el-menu-item index="/editor/news-editor">
            <el-icon><Edit /></el-icon>
            <span>创建新闻</span>
          </el-menu-item>

          <el-menu-item index="/editor/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>

          <el-divider />

          <el-menu-item index="/" @click="goToMainSite">
            <el-icon><House /></el-icon>
            <span>返回主站</span>
          </el-menu-item>

          <!-- 管理员特有菜单 -->
          <template v-if="userStore.userInfo?.role === 'ADMIN'">
            <el-divider />
            <el-sub-menu index="admin">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>管理功能</span>
              </template>
              <el-menu-item index="/admin">
                <el-icon><DataAnalysis /></el-icon>
                <span>管理后台</span>
              </el-menu-item>
              <el-menu-item index="/admin/review">
                <el-icon><Check /></el-icon>
                <span>新闻审核</span>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main class="editor-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  UserFilled, ArrowDown, User, SwitchButton, Document, 
  Edit, House, Setting, DataAnalysis, Check 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

/**
 * 处理用户下拉菜单命令
 */
async function handleUserCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
      
    case 'logout':
      await handleLogout()
      break
  }
}

/**
 * 处理退出登录
 */
async function handleLogout() {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消
  }
}

/**
 * 返回主站
 */
function goToMainSite() {
  router.push('/')
}
</script>

<style scoped>
.editor-layout {
  height: 100vh;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: linear-gradient(135deg, #2c7a3e 0%, #38a169 100%);
  border-bottom: none;
  box-shadow: 0 4px 16px rgba(44, 122, 62, 0.2);
}

.header-left .system-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #ffffff;
}

.dropdown-icon {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.editor-aside {
  background: linear-gradient(to bottom, #f0fdf4 0%, #f8f9fa 100%);
  border-right: 1px solid #dcfce7;
}

.editor-menu {
  border-right: none;
  background: transparent;
  padding: 16px 0;
}

.editor-menu .el-menu-item {
  margin: 4px 12px;
  border-radius: 6px;
  height: 44px;
  line-height: 44px;
}

.editor-menu .el-menu-item:hover {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  color: #2c7a3e;
  transform: translateX(4px);
}

.editor-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #38a169 0%, #2c7a3e 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(56, 161, 105, 0.3);
}

.editor-menu .el-sub-menu .el-menu-item {
  margin: 2px 8px;
  height: 40px;
  line-height: 40px;
}

.editor-main {
  background: linear-gradient(to bottom, #f0fdf4 0%, #f5f7fa 100%);
  padding: 0;
  overflow-y: auto;
}

.el-divider {
  margin: 12px 16px;
}
</style>
