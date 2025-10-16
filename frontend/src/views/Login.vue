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

    // 跳转到目标页面或管理后台
    const redirect = (route.query.redirect as string) || '/admin';
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  border-radius: 10px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  color: #333;
}

.card-header p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.login-form {
  padding: 20px 0;
}

.login-button {
  width: 100%;
}

.login-tips {
  text-align: center;
  margin-top: 20px;
}

.login-tips p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.register-link {
  text-align: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  color: #666;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>

