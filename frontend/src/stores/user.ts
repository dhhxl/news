import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

interface UserInfo {
  id: number;
  username: string;
  role: string;
  email?: string;
  fullName?: string;
  avatarUrl?: string;
  phone?: string;
}

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string | null>(null);
  const userInfo = ref<UserInfo | null>(null);

  // Getters
  const isLoggedIn = computed(() => !!token.value);
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN');
  const user = computed(() => userInfo.value);

  // Actions
  function setToken(newToken: string) {
    token.value = newToken;
    localStorage.setItem(
      import.meta.env.VITE_TOKEN_KEY || 'news_management_token',
      newToken
    );
  }

  function setUser(info: UserInfo) {
    userInfo.value = info;
    // 保存用户信息到 localStorage
    localStorage.setItem('user_info', JSON.stringify(info));
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info;
    // 保存用户信息到 localStorage
    localStorage.setItem('user_info', JSON.stringify(info));
  }

  function clearUser() {
    token.value = null;
    userInfo.value = null;
    localStorage.removeItem('user_info');
  }

  function loadToken() {
    const savedToken = localStorage.getItem(
      import.meta.env.VITE_TOKEN_KEY || 'news_management_token'
    );
    if (savedToken) {
      token.value = savedToken;
    }
    
    // 同时加载用户信息
    const savedUserInfo = localStorage.getItem('user_info');
    if (savedUserInfo) {
      try {
        userInfo.value = JSON.parse(savedUserInfo);
      } catch (error) {
        console.error('Failed to parse user info:', error);
        localStorage.removeItem('user_info');
      }
    }
  }

  function logout() {
    token.value = null;
    userInfo.value = null;
    localStorage.removeItem(
      import.meta.env.VITE_TOKEN_KEY || 'news_management_token'
    );
    // 清除所有相关的本地存储
    localStorage.removeItem('user_info');
    // 可以在这里添加其他清理逻辑
  }
  
  function validateToken() {
    const savedToken = localStorage.getItem(
      import.meta.env.VITE_TOKEN_KEY || 'news_management_token'
    );
    
    if (!savedToken) {
      logout();
      return false;
    }
    
    try {
      // 简单的token格式验证
      const parts = savedToken.split('.');
      if (parts.length !== 3) {
        logout();
        return false;
      }
      return true;
    } catch (error) {
      logout();
      return false;
    }
  }

  return {
    token,
    userInfo,
    user,
    isLoggedIn,
    isAdmin,
    setToken,
    setUser,
    setUserInfo,
    clearUser,
    loadToken,
    logout,
    validateToken
  };
});

