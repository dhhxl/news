import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

interface UserInfo {
  id: number;
  username: string;
  role: string;
  email?: string;
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
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info;
  }

  function clearUser() {
    token.value = null;
    userInfo.value = null;
  }

  function loadToken() {
    const savedToken = localStorage.getItem(
      import.meta.env.VITE_TOKEN_KEY || 'news_management_token'
    );
    if (savedToken) {
      token.value = savedToken;
    }
  }

  function logout() {
    token.value = null;
    userInfo.value = null;
    localStorage.removeItem(
      import.meta.env.VITE_TOKEN_KEY || 'news_management_token'
    );
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
    logout
  };
});

