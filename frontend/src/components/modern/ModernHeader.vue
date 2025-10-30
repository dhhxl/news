<template>
  <header
    :class="[
      'fixed top-0 left-0 right-0 z-40 transition-all duration-300',
      isScrolled ? 'bg-white/80 backdrop-blur-md shadow-sm py-4' : 'bg-transparent py-6'
    ]"
  >
    <div class="max-w-7xl mx-auto px-6 flex justify-between items-center">
      <!-- Logo -->
      <router-link
        to="/"
        class="text-2xl font-bold tracking-tight hover:scale-105 transition-transform cursor-pointer"
      >
        新闻资讯网
      </router-link>

      <!-- Desktop Navigation -->
      <nav class="hidden md:flex items-center gap-8">
        <a
          v-for="item in navItems"
          :key="item.name"
          :href="item.href"
          class="text-sm font-medium hover:opacity-60 transition-opacity hover:-translate-y-1 inline-block duration-200"
        >
          {{ item.name }}
        </a>
        <router-link
          v-if="!isLoggedIn"
          to="/login"
          class="text-sm font-medium px-6 py-2 bg-black text-white rounded-full hover:bg-gray-800 transition-colors"
        >
          登录
        </router-link>
        <div v-else class="flex items-center gap-3">
          <!-- 根据角色显示后台入口 -->
          <router-link
            v-if="userStore.userInfo?.role === 'ADMIN'"
            to="/admin"
            class="text-sm font-medium px-6 py-2 border border-black text-black rounded-full hover:bg-black hover:text-white transition-colors"
          >
            管理后台
          </router-link>
          <router-link
            v-else-if="userStore.userInfo?.role === 'EDITOR'"
            to="/editor/news-list"
            class="text-sm font-medium px-6 py-2 border border-black text-black rounded-full hover:bg-black hover:text-white transition-colors"
          >
            编辑后台
          </router-link>
          
          <!-- 个人资料 -->
          <router-link
            to="/profile"
            class="text-sm font-medium px-6 py-2 border border-black text-black rounded-full hover:bg-black hover:text-white transition-colors"
          >
            个人资料
          </router-link>
          
          <!-- 退出登录 -->
          <button
            @click="handleLogout"
            class="text-sm font-medium px-6 py-2 bg-black text-white rounded-full hover:bg-gray-800 transition-colors"
          >
            退出登录
          </button>
        </div>
      </nav>

      <!-- Mobile Menu Button -->
      <button
        class="md:hidden p-2"
        @click="toggleMobileMenu"
        aria-label="Toggle menu"
      >
        <svg v-if="!isMobileMenuOpen" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="3" y1="12" x2="21" y2="12"></line>
          <line x1="3" y1="6" x2="21" y2="6"></line>
          <line x1="3" y1="18" x2="21" y2="18"></line>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
    </div>

    <!-- Mobile Menu -->
    <Transition name="mobile-menu">
      <div
        v-if="isMobileMenuOpen"
        class="md:hidden absolute top-full left-0 right-0 bg-white shadow-lg"
      >
        <nav class="flex flex-col p-6 gap-4">
          <a
            v-for="item in navItems"
            :key="item.name"
            :href="item.href"
            class="text-lg font-medium hover:opacity-60 transition-opacity"
            @click="closeMobileMenu"
          >
            {{ item.name }}
          </a>
              <router-link
                v-if="!isLoggedIn"
                to="/login"
                class="text-lg font-medium text-center px-6 py-2 bg-black text-white rounded-full hover:bg-gray-800 transition-colors"
                @click="closeMobileMenu"
              >
                登录
              </router-link>
              <template v-else>
                <!-- 根据角色显示后台入口 -->
                <router-link
                  v-if="userStore.userInfo?.role === 'ADMIN'"
                  to="/admin"
                  class="text-lg font-medium px-6 py-2 border border-black text-black rounded-full hover:bg-black hover:text-white transition-colors"
                  @click="closeMobileMenu"
                >
                  管理后台
                </router-link>
                <router-link
                  v-else-if="userStore.userInfo?.role === 'EDITOR'"
                  to="/editor/news-list"
                  class="text-lg font-medium px-6 py-2 border border-black text-black rounded-full hover:bg-black hover:text-white transition-colors"
                  @click="closeMobileMenu"
                >
                  编辑后台
                </router-link>
                
                <!-- 个人资料 -->
                <router-link
                  to="/profile"
                  class="text-lg font-medium px-6 py-2 border border-black text-black rounded-full hover:bg-black hover:text-white transition-colors"
                  @click="closeMobileMenu"
                >
                  个人资料
                </router-link>
                
                <!-- 退出登录 -->
                <button
                  @click="handleLogout"
                  class="text-lg font-medium text-center px-6 py-2 bg-black text-white rounded-full hover:bg-gray-800 transition-colors"
                >
                  退出登录
                </button>
              </template>
        </nav>
      </div>
    </Transition>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isScrolled = ref(false)
const isMobileMenuOpen = ref(false)

const isLoggedIn = computed(() => !!userStore.token)

const navItems = [
  { name: '首页', href: '/' },
  { name: '新闻分类', href: '#categories' },
  { name: '热门话题', href: '#trending' },
  { name: '关于我们', href: '#about' }
]

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

const handleLogout = () => {
  userStore.logout()
  closeMobileMenu()
  router.push('/login')
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: all 0.3s ease;
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  opacity: 0;
  max-height: 0;
}

.mobile-menu-enter-to,
.mobile-menu-leave-from {
  opacity: 1;
  max-height: 500px;
}
</style>

