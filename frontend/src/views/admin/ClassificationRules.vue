<template>
  <div class="rules-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon class="title-icon"><Setting /></el-icon>
          自动分类规则
        </h2>
        <p class="page-desc">配置智能分类规则，实现新闻自动归类</p>
      </div>
      <el-button type="primary" size="large" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建规则
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-blue">
              <el-icon><DocumentCopy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总规则数</div>
              <div class="stat-value">{{ rules.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-green">
              <el-icon><Checked /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">已启用</div>
              <div class="stat-value">{{ enabledRules.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-orange">
              <el-icon><Link /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">来源规则</div>
              <div class="stat-value">{{ sourceRules.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon stat-icon-purple">
              <el-icon><Key /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">关键词规则</div>
              <div class="stat-value">{{ keywordRules.length }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 规则说明 -->
    <el-alert
      title="规则说明"
      type="info"
      :closable="false"
      show-icon
      class="rules-info"
    >
      <template #default>
        <ul class="info-list">
          <li><strong>来源规则：</strong>根据新闻来源自动分类（如：CCTV → 政治）</li>
          <li><strong>关键词规则：</strong>根据标题或内容中的关键词分类（如：包含"经济"→ 经济分类）</li>
          <li><strong>优先级：</strong>数字越小优先级越高，优先匹配高优先级规则</li>
        </ul>
      </template>
    </el-alert>

    <!-- 规则列表 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><List /></el-icon>
            规则列表
          </span>
          <div class="header-actions">
            <el-radio-group v-model="filterType" @change="loadRules" size="small">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="SOURCE">来源规则</el-radio-button>
              <el-radio-button value="KEYWORD">关键词规则</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-table :data="filteredRules" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="ruleType" label="规则类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.ruleType === 'SOURCE'" type="warning">
              <el-icon><Link /></el-icon>
              来源规则
            </el-tag>
            <el-tag v-else type="primary">
              <el-icon><Key /></el-icon>
              关键词
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="规则内容" min-width="250">
          <template #default="{ row }">
            <div v-if="row.ruleType === 'SOURCE'">
              <el-tag effect="plain">来源: {{ row.sourcePattern }}</el-tag>
            </div>
            <div v-else class="keywords-display">
              <el-tag 
                v-for="(keyword, index) in parseKeywords(row.keywords)" 
                :key="index"
                size="small"
                style="margin: 2px;"
              >
                {{ keyword }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="targetCategoryId" label="目标分类" width="120">
          <template #default="{ row }">
            <el-tag type="success">{{ getCategoryName(row.targetCategoryId) }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" width="100" sortable>
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="isEnabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.isEnabled"
              @change="toggleRuleStatus(row)"
              :loading="row.updating"
            />
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editRule(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteRule(row)">
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
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form :model="ruleForm" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="规则类型" prop="ruleType">
          <el-radio-group v-model="ruleForm.ruleType" :disabled="isEditing">
            <el-radio value="SOURCE">
              <el-icon><Link /></el-icon>
              来源规则
            </el-radio>
            <el-radio value="KEYWORD">
              <el-icon><Key /></el-icon>
              关键词规则
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 来源规则 -->
        <el-form-item 
          v-if="ruleForm.ruleType === 'SOURCE'" 
          label="新闻来源" 
          prop="sourcePattern"
        >
          <el-select 
            v-model="ruleForm.sourcePattern" 
            placeholder="选择新闻来源"
            style="width: 100%"
          >
            <el-option label="CCTV（央视新闻）" value="CCTV" />
            <el-option label="NETEASE（网易新闻）" value="NETEASE" />
          </el-select>
          <div class="form-tip">当新闻来源匹配时，自动分类到目标分类</div>
        </el-form-item>

        <!-- 关键词规则 -->
        <el-form-item 
          v-if="ruleForm.ruleType === 'KEYWORD'" 
          label="关键词" 
          prop="keywords"
        >
          <el-input
            v-model="ruleForm.keywords"
            type="textarea"
            :rows="3"
            placeholder="输入关键词，用逗号分隔（如：经济,GDP,财政,金融）"
            maxlength="500"
            show-word-limit
          />
          <div class="form-tip">当新闻标题或内容包含任一关键词时，自动分类到目标分类</div>
        </el-form-item>

        <el-form-item label="目标分类" prop="targetCategoryId">
          <el-select 
            v-model="ruleForm.targetCategoryId" 
            placeholder="选择目标分类"
            style="width: 100%"
          >
            <el-option 
              v-for="cat in categories" 
              :key="cat.id" 
              :label="cat.name" 
              :value="cat.id"
            >
              <span>{{ cat.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px;">
                {{ cat.description }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-input-number 
            v-model="ruleForm.priority" 
            :min="1" 
            :max="999"
            :step="10"
          />
          <span class="form-tip">数字越小优先级越高（1最高，999最低）</span>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-switch v-model="ruleForm.isEnabled" />
          <span class="form-tip">启用后规则将立即生效</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false" size="large">取消</el-button>
        <el-button type="primary" @click="saveRule" :loading="saving" size="large">
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
  Setting,
  Plus, 
  Edit, 
  Delete, 
  List,
  Check,
  Link,
  Key,
  DocumentCopy,
  Checked
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 数据
const rules = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const filterType = ref('')
const formRef = ref<FormInstance>()

// 表单数据
const ruleForm = ref({
  id: null as number | null,
  ruleType: 'SOURCE',
  sourcePattern: '',
  keywords: '',
  targetCategoryId: null as number | null,
  priority: 100,
  isEnabled: true
})

// 表单验证规则
const formRules: FormRules = {
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  sourcePattern: [
    { 
      required: true, 
      message: '请选择新闻来源', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (ruleForm.value.ruleType === 'SOURCE' && !value) {
          callback(new Error('请选择新闻来源'))
        } else {
          callback()
        }
      }
    }
  ],
  keywords: [
    { 
      required: true, 
      message: '请输入关键词', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (ruleForm.value.ruleType === 'KEYWORD' && !value) {
          callback(new Error('请输入关键词'))
        } else {
          callback()
        }
      }
    }
  ],
  targetCategoryId: [
    { required: true, message: '请选择目标分类', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请设置优先级', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => isEditing.value ? '编辑规则' : '创建规则')
const enabledRules = computed(() => rules.value.filter(r => r.isEnabled))
const sourceRules = computed(() => rules.value.filter(r => r.ruleType === 'SOURCE'))
const keywordRules = computed(() => rules.value.filter(r => r.ruleType === 'KEYWORD'))
const filteredRules = computed(() => {
  if (!filterType.value) return rules.value
  return rules.value.filter(r => r.ruleType === filterType.value)
})

// 加载规则列表
const loadRules = async () => {
  loading.value = true
  try {
    const response: any = await request.get('/classification-rules')
    rules.value = response.map((rule: any) => ({
      ...rule,
      updating: false
    }))
  } catch (error) {
    ElMessage.error('加载规则列表失败')
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response: any = await request.get('/categories')
    categories.value = response
  } catch (error) {
    ElMessage.error('加载分类列表失败')
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEditing.value = false
  ruleForm.value = {
    id: null,
    ruleType: 'SOURCE',
    sourcePattern: '',
    keywords: '',
    targetCategoryId: null,
    priority: 100,
    isEnabled: true
  }
  dialogVisible.value = true
}

// 编辑规则
const editRule = (rule: any) => {
  isEditing.value = true
  ruleForm.value = {
    id: rule.id,
    ruleType: rule.ruleType,
    sourcePattern: rule.sourcePattern || '',
    keywords: rule.keywords || '',
    targetCategoryId: rule.targetCategoryId,
    priority: rule.priority,
    isEnabled: rule.isEnabled
  }
  dialogVisible.value = true
}

// 保存规则
const saveRule = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true

    const data = {
      ruleType: ruleForm.value.ruleType,
      sourcePattern: ruleForm.value.ruleType === 'SOURCE' ? ruleForm.value.sourcePattern : null,
      keywords: ruleForm.value.ruleType === 'KEYWORD' ? ruleForm.value.keywords : null,
      targetCategoryId: ruleForm.value.targetCategoryId,
      priority: ruleForm.value.priority,
      isEnabled: ruleForm.value.isEnabled
    }

    if (isEditing.value && ruleForm.value.id) {
      await request.put(`/classification-rules/${ruleForm.value.id}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/classification-rules', data)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    await loadRules()
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

// 切换规则状态
const toggleRuleStatus = async (rule: any) => {
  rule.updating = true
  try {
    await request.put(`/classification-rules/${rule.id}`, {
      ruleType: rule.ruleType,
      sourcePattern: rule.sourcePattern,
      keywords: rule.keywords,
      targetCategoryId: rule.targetCategoryId,
      priority: rule.priority,
      isEnabled: rule.isEnabled
    })
    ElMessage.success(rule.isEnabled ? '规则已启用' : '规则已禁用')
  } catch (error) {
    rule.isEnabled = !rule.isEnabled
    ElMessage.error('状态更新失败')
  } finally {
    rule.updating = false
  }
}

// 删除规则
const deleteRule = async (rule: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除该规则吗？删除后将无法恢复。`,
      '删除规则',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await request.delete(`/classification-rules/${rule.id}`)
    ElMessage.success('删除成功')
    await loadRules()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category ? category.name : '-'
}

// 解析关键词
const parseKeywords = (keywords: string) => {
  if (!keywords) return []
  return keywords.split(',').map(k => k.trim()).filter(k => k)
}

// 获取优先级类型
const getPriorityType = (priority: number) => {
  if (priority <= 50) return 'danger'
  if (priority <= 100) return 'warning'
  return 'info'
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  loadCategories()
  loadRules()
})
</script>

<style scoped>
.rules-manage {
  animation: fadeIn 0.5s ease;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(240, 147, 251, 0.3);
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

.stat-icon-orange {
  background: linear-gradient(135deg, #fff7e6 0%, #e6a23c 100%);
  color: #e6a23c;
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

/* 规则说明 */
.rules-info {
  margin-bottom: 24px;
  border-radius: 8px;
}

.info-list {
  margin: 8px 0 0 0;
  padding-left: 20px;
  line-height: 1.8;
}

.info-list li {
  margin: 4px 0;
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 关键词显示 */
.keywords-display {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* 表单提示 */
.form-tip {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
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
