<template>
  <div class="register-container">
    <el-card class="register-card" shadow="always">
      <template #header>
        <div class="card-header">
      <h2>用户注册</h2>
          <p>创建您的账号</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="邮箱地址"
            prefix-icon="Message"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-select
            v-model="form.role"
            placeholder="请选择角色"
            prefix-icon="Avatar"
            size="large"
            style="width: 100%"
          >
            <el-option label="普通用户" value="USER" />
            <el-option label="编辑员" value="EDITOR" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>

      <div class="login-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'USER' // 默认为普通用户
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    const response = await register({
      username: form.username,
      email: form.email,
      password: form.password,
      role: form.role
    })

    // 保存token和用户信息
    localStorage.setItem('news_management_token', response.token)
    localStorage.setItem('news_management_refresh_token', response.refreshToken)
    
    userStore.setUser({
      id: response.userId,
      username: response.username,
      role: response.role
    })

    ElMessage.success('注册成功！')
    
    // 跳转到首页
    router.push('/')
  } catch (error: any) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
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
.register-container::after {
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

.register-card {
  width: 42vw;
  min-width: 450px;
  max-width: 620px;
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
  padding: 50px 50px;
}

.register-card::before {
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

.register-card:hover {
  background: rgba(205, 164, 94, 0.05);
  box-shadow: 
    6px 0 60px rgba(0, 0, 0, 0.6),
    inset -1px 0 0 rgba(205, 164, 94, 0.3);
  border-right-color: rgba(205, 164, 94, 0.6);
}

/* 覆盖Element Plus卡片样式 */
.register-card :deep(.el-card__header) {
  background: transparent;
  border: none;
  padding: 0 0 25px 0;
}

.register-card :deep(.el-card__body) {
  background: transparent;
  padding: 0;
}

.card-header {
  text-align: center;
  margin-bottom: 15px;
}

.card-header h2 {
  margin: 0 0 10px 0;
  font-size: 34px;
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

.register-form {
  padding: 20px 0 0 0;
}

.register-form :deep(.el-input__wrapper) {
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
.register-form :deep(.el-input__wrapper:has(input:-webkit-autofill)) {
  background: rgba(255, 255, 255, 0.05) !important;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(205, 164, 94, 0.5);
  background: rgba(255, 255, 255, 0.08) !important;
  box-shadow: 
    0 4px 25px rgba(218, 165, 32, 0.3),
    inset 0 1px 0 rgba(218, 165, 32, 0.25),
    inset 0 -1px 0 rgba(0, 0, 0, 0.1),
    0 0 30px rgba(218, 165, 32, 0.2);
}

.register-form :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(218, 165, 32, 0.7);
  background: rgba(255, 255, 255, 0.1) !important;
  box-shadow: 
    0 6px 35px rgba(218, 165, 32, 0.4),
    inset 0 2px 0 rgba(218, 165, 32, 0.35),
    inset 0 -1px 0 rgba(0, 0, 0, 0.1),
    0 0 45px rgba(218, 165, 32, 0.3);
}

/* 强制所有输入框内部元素透明 - 全面覆盖 */
.register-form :deep(.el-input__inner),
.register-form :deep(input),
.register-form :deep(input[type="text"]),
.register-form :deep(input[type="password"]),
.register-form :deep(input[type="email"]),
.register-form :deep(.el-input input),
.register-form :deep(.el-input__wrapper input) {
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
.register-form :deep(.el-input__inner:hover),
.register-form :deep(.el-input__inner:focus),
.register-form :deep(.el-input__inner:active),
.register-form :deep(.el-input.is-focus .el-input__inner),
.register-form :deep(input:hover),
.register-form :deep(input:focus),
.register-form :deep(input:active),
.register-form :deep(.el-input input:hover),
.register-form :deep(.el-input input:focus),
.register-form :deep(.el-input input:active) {
  background: transparent !important;
  background-color: transparent !important;
  background-image: none !important;
  box-shadow: none !important;
}

/* 强制覆盖浏览器自动填充样式 - 终极方案 */
.register-form :deep(input:-webkit-autofill),
.register-form :deep(input:-webkit-autofill:hover),
.register-form :deep(input:-webkit-autofill:focus),
.register-form :deep(input:-webkit-autofill:active),
.register-form :deep(.el-input__inner:-webkit-autofill),
.register-form :deep(.el-input__inner:-webkit-autofill:hover),
.register-form :deep(.el-input__inner:-webkit-autofill:focus),
.register-form :deep(.el-input__inner:-webkit-autofill:active),
.register-form :deep(.el-input input:-webkit-autofill),
.register-form :deep(.el-input__wrapper input:-webkit-autofill) {
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

.register-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.6);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.register-form :deep(.el-icon) {
  color: rgba(255, 255, 255, 0.85);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.6));
}

.register-form :deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05) !important;
}

.register-form :deep(.el-select .el-input__inner) {
  background: transparent !important;
  background-color: transparent !important;
}

.register-button {
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

.register-button:hover {
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

.register-button:active {
  transform: translateY(-1px);
}

.login-link {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 152, 0, 0.3);
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}

.login-link a {
  color: rgba(255, 193, 7, 1);
  text-decoration: none;
  font-weight: 700;
  transition: all 0.3s ease;
  padding: 6px 14px;
  border-radius: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.login-link a:hover {
  background: rgba(255, 152, 0, 0.2);
  color: #FFB300;
  transform: translateX(3px);
  box-shadow: 0 0 15px rgba(255, 152, 0, 0.3);
}
</style>

