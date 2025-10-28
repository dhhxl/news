<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人中心</h2>
          <el-tag :type="roleType" size="large">{{ roleText }}</el-tag>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <div class="basic-info">
            <!-- 头像部分 -->
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :on-change="handleAvatarChange"
                :auto-upload="false"
                accept="image/*"
              >
                <el-avatar 
                  :size="120" 
                  :src="avatarPreview || profile?.avatarUrl || defaultAvatar"
                  class="avatar"
                >
                  <el-icon><User /></el-icon>
                </el-avatar>
                <div class="avatar-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </el-upload>
              <el-button 
                v-if="avatarFile" 
                type="primary" 
                size="small"
                :loading="avatarUploading"
                @click="uploadAvatar"
                class="upload-btn"
              >
                保存头像
              </el-button>
            </div>

            <!-- 个人信息表单 -->
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="用户名">
                <el-input v-model="profile.username" disabled />
              </el-form-item>

              <el-form-item label="姓名" prop="fullName">
                <el-input v-model="profileForm.fullName" placeholder="请输入您的姓名" />
              </el-form-item>

              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>

              <el-form-item label="用户角色">
                <el-tag :type="roleType">{{ roleText }}</el-tag>
              </el-form-item>

              <el-form-item label="注册时间">
                <span>{{ formatDate(profile.createdAt) }}</span>
              </el-form-item>

              <el-form-item label="最后登录">
                <span>{{ profile.lastLoginAt ? formatDate(profile.lastLoginAt) : '从未登录' }}</span>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="updateProfile" :loading="updating">
                  保存修改
                </el-button>
                <el-button @click="resetForm">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="password-form"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入旧密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="changingPassword">
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 统计信息（仅编辑者和管理员） -->
        <el-tab-pane 
          v-if="profile?.role !== 'USER'" 
          label="统计信息" 
          name="stats"
        >
          <div class="stats-section">
            <el-row :gutter="20">
              <el-col :span="8" v-if="profile?.role === 'EDITOR'">
                <el-statistic title="创建新闻数" :value="0">
                  <template #suffix>篇</template>
                </el-statistic>
              </el-col>
              <el-col :span="8" v-if="profile?.role === 'EDITOR'">
                <el-statistic title="待审核新闻" :value="0">
                  <template #suffix>篇</template>
                </el-statistic>
              </el-col>
              <el-col :span="8">
                <el-statistic title="账户状态" :value="profile?.isEnabled ? '正常' : '已禁用'">
                  <template #prefix>
                    <el-icon :color="profile?.isEnabled ? '#67C23A' : '#F56C6C'">
                      <CircleCheck v-if="profile?.isEnabled" />
                      <CircleClose v-else />
                    </el-icon>
                  </template>
                </el-statistic>
              </el-col>
            </el-row>

            <el-divider />

            <el-alert
              v-if="profile?.role === 'ADMIN'"
              title="管理员权限"
              type="success"
              description="您拥有系统最高权限，可以管理所有用户、新闻和系统设置。"
              :closable="false"
              show-icon
            />

            <el-alert
              v-if="profile?.role === 'EDITOR'"
              title="编辑员权限"
              type="info"
              description="您可以创建、编辑新闻稿件，并提交审核。已通过审核的新闻将发布到前台。"
              :closable="false"
              show-icon
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Camera, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { 
  getUserProfileApi, 
  updateProfileApi, 
  uploadAvatarApi, 
  changePasswordApi,
  type UserProfile 
} from '../api/profile'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

// 数据
const activeTab = ref('basic')
const profile = ref<UserProfile>({
  id: 0,
  username: '',
  role: 'USER',
  createdAt: '',
  isEnabled: true
})

// 默认头像
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 头像相关
const avatarFile = ref<File | null>(null)
const avatarPreview = ref<string>('')
const avatarUploading = ref(false)

// 个人信息表单
const profileFormRef = ref<FormInstance>()
const profileForm = reactive({
  fullName: '',
  email: '',
  phone: ''
})

const updating = ref(false)

// 密码表单
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const changingPassword = ref(false)

// 角色相关
const roleText = computed(() => {
  const roleMap: Record<string, string> = {
    'ADMIN': '管理员',
    'EDITOR': '编辑员',
    'USER': '普通用户'
  }
  return roleMap[profile.value?.role || 'USER'] || '普通用户'
})

const roleType = computed(() => {
  const typeMap: Record<string, any> = {
    'ADMIN': 'danger',
    'EDITOR': 'warning',
    'USER': 'info'
  }
  return typeMap[profile.value?.role || 'USER'] || 'info'
})

// 表单验证规则
const profileRules: FormRules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载个人资料
const loadProfile = async () => {
  try {
    const data = await getUserProfileApi()
    profile.value = data
    
    // 填充表单
    profileForm.fullName = data.fullName || ''
    profileForm.email = data.email || ''
    profileForm.phone = data.phone || ''
  } catch (error) {
    console.error('加载个人资料失败:', error)
    ElMessage.error('加载个人资料失败')
  }
}

// 处理头像选择
const handleAvatarChange = (file: any) => {
  const rawFile = file.raw
  
  // 验证文件类型
  if (!rawFile.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }
  
  // 验证文件大小（2MB）
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }
  
  avatarFile.value = rawFile
  
  // 预览
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target?.result as string
  }
  reader.readAsDataURL(rawFile)
}

// 上传头像
const uploadAvatar = async () => {
  if (!avatarFile.value) return
  
  avatarUploading.value = true
  try {
    const avatarUrl = await uploadAvatarApi(avatarFile.value)
    profile.value.avatarUrl = avatarUrl
    avatarFile.value = null
    avatarPreview.value = ''
    
    // 更新store中的用户信息
    if (userStore.userInfo) {
      userStore.userInfo.avatarUrl = avatarUrl
    }
    
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

// 更新个人资料
const updateProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    updating.value = true
    try {
      const data = await updateProfileApi(profileForm)
      profile.value = data
      
      // 更新store中的用户信息
      if (userStore.userInfo) {
        userStore.userInfo.fullName = data.fullName
        userStore.userInfo.email = data.email
      }
      
      ElMessage.success('个人资料更新成功')
    } catch (error: any) {
      console.error('更新个人资料失败:', error)
      ElMessage.error(error.response?.data?.message || '更新个人资料失败')
    } finally {
      updating.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  profileForm.fullName = profile.value.fullName || ''
  profileForm.email = profile.value.email || ''
  profileForm.phone = profile.value.phone || ''
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    changingPassword.value = true
    try {
      await changePasswordApi({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      
      ElMessage.success('密码修改成功，请重新登录')
      resetPasswordForm()
      
      // 3秒后跳转到登录页
      setTimeout(() => {
        userStore.logout()
        window.location.href = '/login'
      }, 3000)
    } catch (error: any) {
      console.error('修改密码失败:', error)
      ElMessage.error(error.response?.data?.message || '修改密码失败')
    } finally {
      changingPassword.value = false
    }
  })
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 组件挂载时加载个人资料
onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 8px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h2 {
      margin: 0;
      font-size: 24px;
      color: #2d8cf0;
    }
  }
}

.profile-tabs {
  :deep(.el-tabs__item) {
    font-size: 16px;
  }
}

.basic-info {
  display: flex;
  gap: 40px;
  
  .avatar-section {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
    
    .avatar-uploader {
      position: relative;
      cursor: pointer;
      
      .avatar {
        border: 2px solid #e4e7ed;
        transition: all 0.3s;
      }
      
      &:hover .avatar {
        border-color: #2d8cf0;
      }
      
      .avatar-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: rgba(0, 0, 0, 0.6);
        color: white;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 8px;
        border-radius: 0 0 50% 50%;
        opacity: 0;
        transition: opacity 0.3s;
        
        span {
          font-size: 12px;
          margin-top: 4px;
        }
      }
      
      &:hover .avatar-overlay {
        opacity: 1;
      }
    }
    
    .upload-btn {
      width: 100%;
    }
  }
  
  .profile-form {
    flex: 1;
  }
}

.password-form {
  max-width: 500px;
}

.stats-section {
  padding: 20px 0;
  
  .el-statistic {
    text-align: center;
  }
  
  .el-alert {
    margin-top: 20px;
  }
}
</style>

