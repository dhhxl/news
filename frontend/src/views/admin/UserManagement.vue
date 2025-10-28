<template>
  <div class="user-management">
    <el-card class="header-card">
      <div class="header">
        <h2>用户管理</h2>
        <div class="stats">
          <el-statistic title="总用户" :value="statistics.total">
            <template #suffix>人</template>
          </el-statistic>
          <el-statistic title="管理员" :value="statistics.admins">
            <template #suffix>人</template>
          </el-statistic>
          <el-statistic title="编辑员" :value="statistics.editors">
            <template #suffix>人</template>
          </el-statistic>
          <el-statistic title="普通用户" :value="statistics.users">
            <template #suffix>人</template>
          </el-statistic>
        </div>
      </div>
    </el-card>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/邮箱/姓名"
            clearable
            @clear="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable @change="handleSearch">
            <el-option label="全部" value="" />
            <el-option label="管理员" value="ADMIN" />
            <el-option label="编辑员" value="EDITOR" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.isEnabled" placeholder="全部" clearable @change="handleSearch">
            <el-option label="全部" :value="null" />
            <el-option label="正常" :value="true" />
            <el-option label="已禁用" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatarUrl">
              <el-icon><User /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>

        <el-table-column prop="username" label="用户名" width="150" />
        
        <el-table-column prop="fullName" label="姓名" width="120">
          <template #default="{ row }">
            {{ row.fullName || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" width="200">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>

        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="isEnabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isEnabled ? 'success' : 'danger'">
              {{ row.isEnabled ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column prop="lastLoginAt" label="最后登录" width="180">
          <template #default="{ row }">
            {{ row.lastLoginAt ? formatDate(row.lastLoginAt) : '从未登录' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            
            <el-button
              v-if="row.role !== 'ADMIN'"
              size="small"
              :type="row.isEnabled ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              <el-icon><CircleClose v-if="row.isEnabled" /><CircleCheck v-else /></el-icon>
              {{ row.isEnabled ? '禁用' : '启用' }}
            </el-button>
            
            <el-button
              v-if="row.role !== 'ADMIN'"
              size="small"
              type="danger"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="600px"
    >
      <el-descriptions v-if="currentUser" :column="2" border>
        <el-descriptions-item label="头像" :span="2">
          <el-avatar :size="80" :src="currentUser.avatarUrl">
            <el-icon><User /></el-icon>
          </el-avatar>
        </el-descriptions-item>
        <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentUser.fullName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentUser.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="getRoleType(currentUser.role)">
            {{ getRoleText(currentUser.role) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentUser.isEnabled ? 'success' : 'danger'">
            {{ currentUser.isEnabled ? '正常' : '已禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">
          {{ formatDate(currentUser.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="最后登录" :span="2">
          {{ currentUser.lastLoginAt ? formatDate(currentUser.lastLoginAt) : '从未登录' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button v-if="currentUser && currentUser.role !== 'ADMIN'" type="primary" @click="handleResetPassword">
          重置密码
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <el-form :model="resetPasswordForm" label-width="100px">
        <el-form-item label="用户">
          <el-input v-model="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, RefreshLeft, User, View, Delete,
  CircleClose, CircleCheck
} from '@element-plus/icons-vue'
import {
  getUserListApi,
  getUserDetailApi,
  deleteUserApi,
  toggleUserStatusApi,
  resetPasswordApi,
  getUserStatisticsApi,
  type UserListItem,
  type UserStatistics
} from '../../api/userManagement'

// 数据
const loading = ref(false)
const userList = ref<UserListItem[]>([])
const statistics = ref<UserStatistics>({
  total: 0,
  admins: 0,
  editors: 0,
  users: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: '',
  isEnabled: null as boolean | null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const detailDialogVisible = ref(false)
const resetPasswordDialogVisible = ref(false)
const currentUser = ref<any>(null)

// 重置密码表单
const resetPasswordForm = reactive({
  newPassword: ''
})

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const response = await getUserListApi({
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      role: searchForm.role || undefined,
      isEnabled: searchForm.isEnabled !== null ? searchForm.isEnabled : undefined
    })
    
    userList.value = response.users
    pagination.total = response.totalElements
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    statistics.value = await getUserStatisticsApi()
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUserList()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.isEnabled = null
  handleSearch()
}

// 查看详情
const handleViewDetail = async (user: UserListItem) => {
  try {
    currentUser.value = await getUserDetailApi(user.id)
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

// 禁用/启用用户
const handleToggleStatus = async (user: UserListItem) => {
  const action = user.isEnabled ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户「${user.username}」吗？`,
      `${action}用户`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await toggleUserStatusApi(user.id)
    ElMessage.success(`${action}成功`)
    loadUserList()
    loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${action}用户失败:`, error)
      ElMessage.error(error.response?.data?.message || `${action}用户失败`)
    }
  }
}

// 删除用户
const handleDelete = async (user: UserListItem) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${user.username}」吗？此操作不可恢复！`,
      '删除用户',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await deleteUserApi(user.id)
    ElMessage.success('删除成功')
    loadUserList()
    loadStatistics()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error(error.response?.data?.message || '删除用户失败')
    }
  }
}

// 重置密码
const handleResetPassword = () => {
  resetPasswordForm.newPassword = ''
  resetPasswordDialogVisible.value = true
}

// 确认重置密码
const confirmResetPassword = async () => {
  if (!resetPasswordForm.newPassword || resetPasswordForm.newPassword.length < 6) {
    ElMessage.error('密码长度至少为6位')
    return
  }
  
  try {
    await resetPasswordApi(currentUser.value.id, resetPasswordForm.newPassword)
    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
    detailDialogVisible.value = false
  } catch (error) {
    console.error('重置密码失败:', error)
    ElMessage.error('重置密码失败')
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.page = page
  loadUserList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadUserList()
}

// 工具函数
const getRoleType = (role: string) => {
  const typeMap: Record<string, any> = {
    'ADMIN': 'danger',
    'EDITOR': 'warning',
    'USER': 'info'
  }
  return typeMap[role] || 'info'
}

const getRoleText = (role: string) => {
  const textMap: Record<string, string> = {
    'ADMIN': '管理员',
    'EDITOR': '编辑员',
    'USER': '普通用户'
  }
  return textMap[role] || role
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  loadUserList()
  loadStatistics()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #2d8cf0;
}

.stats {
  display: flex;
  gap: 40px;
}

.filter-card {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

