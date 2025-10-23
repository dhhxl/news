<template>
  <div class="category-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon class="title-icon"><Folder /></el-icon>
          分类管理
        </h2>
        <p class="page-desc">管理新闻分类，支持创建、编辑和删除</p>
      </div>
      <el-button type="primary" size="large" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建分类
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-blue">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总分类数</div>
              <div class="stat-value">{{ categories.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-green">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">默认分类</div>
              <div class="stat-value">{{ defaultCategories.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-purple">
              <el-icon><Edit /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">自定义分类</div>
              <div class="stat-value">{{ customCategories.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><List /></el-icon>
            分类列表
          </span>
        </div>
      </template>

      <el-table :data="categories" v-loading="loading" stripe class="category-table">
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="name" label="分类名称" width="150">
          <template #default="{ row }">
            <el-tag :type="row.isDefault ? 'success' : ''" size="large">
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="描述" min-width="250" />
        
        <el-table-column prop="isDefault" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" effect="dark">默认</el-tag>
            <el-tag v-else type="info" effect="plain">自定义</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editCategory(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="deleteCategory(row)"
              :disabled="row.isDefault"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="categoryForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input 
            v-model="categoryForm.name" 
            placeholder="请输入分类名称（如：政治、经济、体育）"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="默认分类">
          <el-switch 
            v-model="categoryForm.isDefault" 
            :disabled="isEditing && categoryForm.isDefault"
          />
          <span class="form-tip">默认分类不可删除</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false" size="large">取消</el-button>
        <el-button type="primary" @click="saveCategory" :loading="saving" size="large">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { 
  Folder, 
  Plus, 
  Edit, 
  Delete, 
  Star, 
  List,
  Check
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 数据
const categories = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const categoryForm = ref({
  id: null as number | null,
  name: '',
  description: '',
  isDefault: false
})

// 表单验证规则
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 255, message: '描述不能超过 255 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEditing.value ? '编辑分类' : '创建分类')
const defaultCategories = computed(() => categories.value.filter(c => c.isDefault))
const customCategories = computed(() => categories.value.filter(c => !c.isDefault))

// 加载分类列表
const loadCategories = async () => {
  loading.value = true
  try {
    const response: any = await request.get('/categories')
    categories.value = response
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEditing.value = false
  categoryForm.value = {
    id: null,
    name: '',
    description: '',
    isDefault: false
  }
  dialogVisible.value = true
}

// 编辑分类
const editCategory = (category: any) => {
  isEditing.value = true
  categoryForm.value = {
    id: category.id,
    name: category.name,
    description: category.description || '',
    isDefault: category.isDefault
  }
  dialogVisible.value = true
}

// 保存分类
const saveCategory = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true

    const data = {
      name: categoryForm.value.name,
      description: categoryForm.value.description,
      isDefault: categoryForm.value.isDefault
    }

    if (isEditing.value && categoryForm.value.id) {
      await request.put(`/categories/${categoryForm.value.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/categories', data)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    await loadCategories()
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除分类
const deleteCategory = async (category: any) => {
  if (category.isDefault) {
    ElMessage.warning('默认分类不能删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？该分类下的所有新闻也会受影响。`,
      '删除分类',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await request.delete(`/categories/${category.id}`)
    ElMessage.success('删除成功')
    await loadCategories()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-manage {
  animation: fadeIn 0.5s ease;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.header-content {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-icon-blue {
  background: linear-gradient(135deg, #e3f2ff 0%, #409eff 100%);
  color: #409eff;
}

.stat-icon-green {
  background: linear-gradient(135deg, #f0f9ff 0%, #67c23a 100%);
  color: #67c23a;
}

.stat-icon-purple {
  background: linear-gradient(135deg, #f3e5f5 0%, #9c27b0 100%);
  color: #9c27b0;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

/* 表格卡片 */
.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.category-table {
  border-radius: 8px;
  overflow: hidden;
}

/* 表单提示 */
.form-tip {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

/* 淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 对话框样式优化 */
:deep(.el-dialog__header) {
  background: #f5f7fa;
  padding: 20px;
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
}
</style>
