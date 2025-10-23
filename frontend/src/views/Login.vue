<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <template #header>
        <div class="card-header">
          <h2>{{ appTitle }}</h2>
          <p>管理员登录</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>

        <div class="login-tips">
          <p>默认管理员账号: admin / admin123</p>
        </div>

        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, FormInstance, FormRules } from 'element-plus';
import { useUserStore } from '@/stores/user';
import { login } from '@/api/auth';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const appTitle = '新闻管理系统';
const loading = ref(false);
const loginFormRef = ref<FormInstance>();

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
});

// 验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为6-100个字符', trigger: 'blur' }
  ]
};

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return;

  try {
    // 验证表单
    await loginFormRef.value.validate();

    loading.value = true;

    // 调用登录API
    const response = await login({
      username: loginForm.username,
      password: loginForm.password
    });

    // 保存token和用户信息
    userStore.setToken(response.token);
    userStore.setUserInfo({
      id: response.userId,
      username: response.username,
      role: response.role
    });

    ElMessage.success('登录成功！');

    // 根据用户角色决定重定向路径
    let defaultPath = '/';
    if (response.role === 'ADMIN') {
      defaultPath = '/admin';
    } else if (response.role === 'EDITOR') {
      defaultPath = '/editor';
    }

    // 跳转到目标页面或根据角色的默认页面
    const redirect = (route.query.redirect as string) || defaultPath;
    router.push(redirect);
  } catch (error: any) {
    console.error('Login error:', error);
    ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 森林绿色渐变背景 */
  background: linear-gradient(135deg, #1b5e20 0%, #2e7d32 25%, #388e3c 50%, #43a047 75%, #4caf50 100%);
  /* 如果有图片，会叠加在渐变背景上 */
  background-image: 
    radial-gradient(ellipse at center, rgba(255,255,255,0.1) 0%, transparent 70%),
    url('/images/b.jpg');
  background-size: cover, cover;
  background-position: center center, center center;
  background-repeat: no-repeat, no-repeat;
  position: relative;
}

/* 柔和的覆盖层，增加深度感 */
.login-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 30% 30%, rgba(255,255,255,0.15) 0%, transparent 50%),
    linear-gradient(135deg, rgba(27, 94, 32, 0.3) 0%, rgba(46, 125, 50, 0.4) 100%);
  z-index: 1;
}

/* 移除多余的::before样式，使用::after即可 */

.login-card {
  width: 420px;
  border-radius: 20px;
  position: relative;
  z-index: 10;
  backdrop-filter: blur(15px) saturate(180%);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.25) 0%, 
    rgba(255, 255, 255, 0.15) 100%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  transition: all 0.3s ease;
}

.login-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 40px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 15px 0;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #1b5e20, #2e7d32);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 10px rgba(46, 125, 50, 0.3);
}

.card-header p {
  margin: 0;
  font-size: 16px;
  color: rgba(27, 94, 32, 0.8);
  font-weight: 500;
}

.login-form {
  padding: 25px 0;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(46, 125, 50, 0.2);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(46, 125, 50, 0.4);
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.15);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #2e7d32;
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.25);
}

.login-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2e7d32 0%, #43a047 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.3);
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(46, 125, 50, 0.4);
}

.login-tips {
  text-align: center;
  margin-top: 25px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-tips p {
  margin: 0;
  font-size: 13px;
  color: rgba(27, 94, 32, 0.7);
  line-height: 1.4;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
  color: rgba(27, 94, 32, 0.8);
}

.register-link a {
  color: #2e7d32;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  padding: 5px 10px;
  border-radius: 8px;
}

.register-link a:hover {
  background: rgba(46, 125, 50, 0.1);
  color: #1b5e20;
}
</style>

