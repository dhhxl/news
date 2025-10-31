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
  justify-content: flex-start;
  padding-left: 0;
  /* 蒸汽机械背景图 */
  background-image: url('/images/steam-machines-4405498_1280.jpg');
  background-size: 100% 100%;
  background-position: center center;
  background-repeat: no-repeat;
  position: relative;
}

/* 蒸汽朋克氛围覆盖层 */
.login-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    linear-gradient(90deg, 
      rgba(20, 15, 10, 0.75) 0%, 
      rgba(50, 40, 30, 0.4) 40%, 
      rgba(0, 0, 0, 0.1) 100%);
  z-index: 1;
}

.login-card {
  width: 42vw;
  min-width: 420px;
  max-width: 600px;
  min-height: 100vh;
  border-radius: 0 30px 30px 0;
  position: relative;
  z-index: 10;
  backdrop-filter: blur(25px) saturate(150%);
  background: transparent;
  border: none;
  border-right: 2px solid rgba(205, 164, 94, 0.4);
  box-shadow: 
    4px 0 50px rgba(0, 0, 0, 0.5),
    inset -1px 0 0 rgba(205, 164, 94, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 50px;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 2px;
  height: 100%;
  background: linear-gradient(180deg, 
    rgba(205, 164, 94, 0) 0%, 
    rgba(205, 164, 94, 0.6) 20%, 
    rgba(218, 165, 32, 0.9) 50%, 
    rgba(205, 164, 94, 0.6) 80%, 
    rgba(205, 164, 94, 0) 100%);
  box-shadow: 0 0 25px rgba(205, 164, 94, 0.5);
}

.login-card:hover {
  background: rgba(205, 164, 94, 0.05);
  box-shadow: 
    6px 0 60px rgba(0, 0, 0, 0.6),
    inset -1px 0 0 rgba(205, 164, 94, 0.3);
  border-right-color: rgba(205, 164, 94, 0.6);
}

/* 覆盖Element Plus卡片样式 */
.login-card :deep(.el-card__header) {
  background: transparent;
  border: none;
  padding: 0 0 30px 0;
}

.login-card :deep(.el-card__body) {
  background: transparent;
  padding: 0;
}

.card-header {
  text-align: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0 0 12px 0;
  font-size: 36px;
  font-weight: 800;
  background: linear-gradient(135deg, 
    #FFF 0%, 
    #F5DEB3 30%, 
    #DAA520 70%, 
    #B8860B 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 2px 10px rgba(218, 165, 32, 0.5));
  letter-spacing: 1.5px;
}

.card-header p {
  margin: 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 600;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
  letter-spacing: 0.5px;
}

.login-form {
  padding: 25px 0;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05) !important;
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 2px solid rgba(205, 164, 94, 0.35);
  border-radius: 12px;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(218, 165, 32, 0.15),
    inset 0 -1px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

/* 防止自动填充影响wrapper */
.login-form :deep(.el-input__wrapper:has(input:-webkit-autofill)) {
  background: rgba(255, 255, 255, 0.05) !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(205, 164, 94, 0.5);
  background: rgba(255, 255, 255, 0.08) !important;
  box-shadow: 
    0 4px 25px rgba(218, 165, 32, 0.3),
    inset 0 1px 0 rgba(218, 165, 32, 0.25),
    inset 0 -1px 0 rgba(0, 0, 0, 0.1),
    0 0 30px rgba(218, 165, 32, 0.2);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(218, 165, 32, 0.7);
  background: rgba(255, 255, 255, 0.1) !important;
  box-shadow: 
    0 6px 35px rgba(218, 165, 32, 0.4),
    inset 0 2px 0 rgba(218, 165, 32, 0.35),
    inset 0 -1px 0 rgba(0, 0, 0, 0.1),
    0 0 45px rgba(218, 165, 32, 0.3);
}

/* 强制所有输入框内部元素透明 - 全面覆盖 */
.login-form :deep(.el-input__inner),
.login-form :deep(input),
.login-form :deep(input[type="text"]),
.login-form :deep(input[type="password"]),
.login-form :deep(.el-input input),
.login-form :deep(.el-input__wrapper input) {
  color: rgba(255, 255, 255, 1) !important;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.8);
  background: transparent !important;
  background-color: transparent !important;
  background-image: none !important;
  -webkit-appearance: none !important;
  appearance: none !important;
  caret-color: rgba(218, 165, 32, 1) !important;
}

/* 所有状态下都透明 - 包括输入时 */
.login-form :deep(.el-input__inner:hover),
.login-form :deep(.el-input__inner:focus),
.login-form :deep(.el-input__inner:active),
.login-form :deep(.el-input.is-focus .el-input__inner),
.login-form :deep(input:hover),
.login-form :deep(input:focus),
.login-form :deep(input:active),
.login-form :deep(.el-input input:hover),
.login-form :deep(.el-input input:focus),
.login-form :deep(.el-input input:active) {
  background: transparent !important;
  background-color: transparent !important;
  background-image: none !important;
  box-shadow: none !important;
}

/* 强制覆盖浏览器自动填充样式 - 终极方案 */
.login-form :deep(input:-webkit-autofill),
.login-form :deep(input:-webkit-autofill:hover),
.login-form :deep(input:-webkit-autofill:focus),
.login-form :deep(input:-webkit-autofill:active),
.login-form :deep(.el-input__inner:-webkit-autofill),
.login-form :deep(.el-input__inner:-webkit-autofill:hover),
.login-form :deep(.el-input__inner:-webkit-autofill:focus),
.login-form :deep(.el-input__inner:-webkit-autofill:active),
.login-form :deep(.el-input input:-webkit-autofill),
.login-form :deep(.el-input__wrapper input:-webkit-autofill) {
  -webkit-text-fill-color: rgba(255, 255, 255, 1) !important;
  color: rgba(255, 255, 255, 1) !important;
  caret-color: rgba(218, 165, 32, 1) !important;
  /* 使用5000秒延迟来"永久"阻止白色背景 */
  transition: background-color 5000s ease-in-out 0s !important;
  /* 使用透明内阴影覆盖白色背景 */
  -webkit-box-shadow: 0 0 0 1000px transparent inset !important;
  box-shadow: 0 0 0 1000px transparent inset !important;
  background-color: transparent !important;
  background-image: none !important;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.login-form :deep(.el-icon) {
  color: rgba(255, 255, 255, 0.85);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.6));
}

.login-button {
  width: 100%;
  height: 54px;
  border-radius: 12px;
  background: linear-gradient(135deg, 
    rgba(218, 165, 32, 0.35) 0%, 
    rgba(184, 134, 11, 0.45) 100%);
  border: 2px solid rgba(205, 164, 94, 0.6);
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  backdrop-filter: blur(10px);
  box-shadow: 
    0 8px 30px rgba(218, 165, 32, 0.4),
    inset 0 1px 0 rgba(218, 165, 32, 0.4),
    0 0 40px rgba(218, 165, 32, 0.2);
  transition: all 0.3s ease;
  letter-spacing: 1.5px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.8);
}

.login-button:hover {
  transform: translateY(-3px);
  background: linear-gradient(135deg, 
    rgba(218, 165, 32, 0.45) 0%, 
    rgba(184, 134, 11, 0.55) 100%);
  border-color: rgba(218, 165, 32, 0.8);
  box-shadow: 
    0 12px 40px rgba(218, 165, 32, 0.6),
    inset 0 1px 0 rgba(218, 165, 32, 0.5),
    0 0 60px rgba(218, 165, 32, 0.4);
}

.login-button:active {
  transform: translateY(-1px);
}

.login-tips {
  text-align: center;
  margin-top: 25px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 
    0 2px 12px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    inset 0 -1px 0 rgba(0, 0, 0, 0.1);
}

.login-tips p {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.5;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.6);
}

.register-link {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.register-link a {
  color: rgba(144, 202, 249, 1);
  text-decoration: none;
  font-weight: 700;
  transition: all 0.3s ease;
  padding: 6px 14px;
  border-radius: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
}

.register-link a:hover {
  background: rgba(100, 181, 246, 0.2);
  color: rgba(227, 242, 253, 1);
  transform: translateX(3px);
  box-shadow: 0 0 15px rgba(100, 181, 246, 0.4);
}
</style>

