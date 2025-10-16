<template>
  <div class="news-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon class="title-icon"><Document /></el-icon>
          新闻管理
        </h2>
        <p class="page-desc">创建、编辑和管理新闻内容，支持批量生成AI摘要</p>
      </div>
      <el-button type="primary" size="large" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建新闻
      </el-button>
    </div>

    <el-card shadow="hover" class="content-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><List /></el-icon>
            新闻列表
          </span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索新闻标题..."
          style="width: 300px"
          @keyup.enter="loadNews"
        >
          <template #append>
            <el-button :icon="Search" @click="loadNews" />
          </template>
        </el-input>
        
        <el-select v-model="filterCategory" placeholder="选择分类" clearable @change="loadNews" style="width: 150px">
          <el-option label="全部" :value="null" />
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>

        <el-select v-model="filterStatus" placeholder="状态" clearable @change="loadNews" style="width: 120px">
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </div>

      <!-- 批量操作栏 -->
      <div class="batch-actions" v-if="selectedNews.length > 0">
        <el-alert
          :title="`已选择 ${selectedNews.length} 条新闻`"
          type="info"
          :closable="false"
          style="margin-bottom: 10px"
        >
          <template #default>
            <div style="display: flex; align-items: center; gap: 10px;">
              <span>已选择 {{ selectedNews.length }} 条新闻</span>
              <el-button size="small" type="primary" @click="batchGenerateSummary" :loading="batchGenerating">
                <el-icon><MagicStick /></el-icon>
                批量生成摘要
              </el-button>
              <el-button size="small" @click="clearSelection">取消选择</el-button>
            </div>
          </template>
        </el-alert>
      </div>

      <!-- 新闻表格 -->
      <el-table 
        :data="newsList" 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="300" />
        <el-table-column prop="sourceWebsite" label="来源" width="100" />
        <el-table-column label="分类" width="100">
          <template #default="scope">
            {{ getCategoryName(scope.row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row.id)">查看</el-button>
            <el-button size="small" type="primary" @click="generateSummary(scope.row.id)">
              生成摘要
            </el-button>
            <el-button size="small" type="warning" @click="editNews(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteNews(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalElements"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadNews"
          @size-change="loadNews"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
    >
      <el-form :model="newsForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="newsForm.title" placeholder="请输入新闻标题" />
        </el-form-item>
        
        <el-form-item label="内容" required>
          <el-input
            v-model="newsForm.content"
            type="textarea"
            :rows="10"
            placeholder="请输入新闻内容"
          />
        </el-form-item>

        <el-form-item label="分类" required>
          <el-select v-model="newsForm.categoryId" placeholder="选择分类">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="来源">
          <el-input v-model="newsForm.sourceWebsite" placeholder="MANUAL" />
        </el-form-item>

        <el-form-item label="原始URL">
          <el-input v-model="newsForm.originalUrl" placeholder="https://..." />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="newsForm.status">
            <el-radio value="PUBLISHED">已发布</el-radio>
            <el-radio value="DRAFT">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNews" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, MagicStick, Document, List } from '@element-plus/icons-vue'
import {
  getNewsList,
  getCategories,
  createNews,
  updateNews,
  deleteNews as deleteNewsApi,
  generateSummary as generateSummaryApi,
  batchGenerateSummaries as batchGenerateSummariesApi,
  type News,
  type Category
} from '@/api/news'

const router = useRouter()

// 数据
const newsList = ref<News[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const saving = ref(false)
const batchGenerating = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const totalElements = ref(0)
const searchKeyword = ref('')
const filterCategory = ref<number | null>(null)
const filterStatus = ref<string>('')
const selectedNews = ref<News[]>([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => editingNews.value ? '编辑新闻' : '创建新闻')
const editingNews = ref<News | null>(null)
const newsForm = ref({
  title: '',
  content: '',
  sourceWebsite: 'MANUAL',
  originalUrl: '',
  categoryId: null as number | null,
  status: 'PUBLISHED'
})

// 加载分类
async function loadCategories() {
  try {
    const response: any = await getCategories()
    categories.value = response
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

// 加载新闻列表
async function loadNews() {
  loading.value = true
  try {
    const response: any = await getNewsList({
      page: currentPage.value - 1,
      size: pageSize.value,
      categoryId: filterCategory.value || undefined
    })
    newsList.value = response.content
    totalElements.value = response.totalElements
  } catch (error) {
    ElMessage.error('加载新闻列表失败')
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
function showCreateDialog() {
  editingNews.value = null
  newsForm.value = {
    title: '',
    content: '',
    sourceWebsite: 'MANUAL',
    originalUrl: '',
    categoryId: null,
    status: 'PUBLISHED'
  }
  dialogVisible.value = true
}

// 编辑新闻
function editNews(news: News) {
  editingNews.value = news
  newsForm.value = {
    title: news.title,
    content: news.content,
    sourceWebsite: news.sourceWebsite,
    originalUrl: news.originalUrl,
    categoryId: news.categoryId,
    status: news.status
  }
  dialogVisible.value = true
}

// 保存新闻
async function saveNews() {
  if (!newsForm.value.title || !newsForm.value.content || !newsForm.value.categoryId) {
    ElMessage.warning('请填写必填项')
    return
  }

  saving.value = true
  try {
    if (!newsForm.value.originalUrl) {
      newsForm.value.originalUrl = `https://manual/${Date.now()}`
    }

    if (editingNews.value) {
      await updateNews(editingNews.value.id, newsForm.value as any)
      ElMessage.success('更新成功')
    } else {
      await createNews(newsForm.value as any)
      ElMessage.success('创建成功，AI摘要正在生成中...')
    }

    dialogVisible.value = false
    await loadNews()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 删除新闻
async function deleteNews(news: News) {
  try {
    await ElMessageBox.confirm(`确定要删除新闻"${news.title}"吗？`, '确认删除', {
      type: 'warning'
    })

    await deleteNewsApi(news.id)
    ElMessage.success('删除成功')
    await loadNews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 生成摘要
async function generateSummary(newsId: number) {
  try {
    await generateSummaryApi(newsId)
    ElMessage.success('摘要生成请求已提交')
  } catch (error) {
    ElMessage.error('生成摘要失败')
  }
}

// 批量生成摘要
async function batchGenerateSummary() {
  if (selectedNews.value.length === 0) {
    ElMessage.warning('请先选择要生成摘要的新闻')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定为选中的 ${selectedNews.value.length} 条新闻生成AI摘要吗？`,
      '批量生成摘要',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    batchGenerating.value = true
    
    // 方式1: 使用后端批量接口
    try {
      await batchGenerateSummariesApi()
      ElMessage.success(`批量摘要生成任务已启动，正在后台处理...`)
      clearSelection()
    } catch (error: any) {
      // 方式2: 如果批量接口不可用，逐个生成
      ElMessage.info('使用逐个生成模式...')
      
      let successCount = 0
      let failCount = 0
      
      for (let i = 0; i < selectedNews.value.length; i++) {
        const news = selectedNews.value[i]
        try {
          await generateSummaryApi(news.id)
          successCount++
          ElMessage.success(`[${i + 1}/${selectedNews.value.length}] ${news.title} - 摘要生成成功`)
          // 间隔2秒，避免API限流
          await new Promise(resolve => setTimeout(resolve, 2000))
        } catch (err) {
          failCount++
          ElMessage.error(`[${i + 1}/${selectedNews.value.length}] ${news.title} - 生成失败`)
        }
      }
      
      ElMessage.success(`批量生成完成！成功: ${successCount}, 失败: ${failCount}`)
      clearSelection()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量生成失败')
    }
  } finally {
    batchGenerating.value = false
  }
}

// 处理表格选择变化
function handleSelectionChange(selection: News[]) {
  selectedNews.value = selection
}

// 清除选择
function clearSelection() {
  selectedNews.value = []
}

// 查看详情
function viewDetail(id: number) {
  router.push(`/news/${id}`)
}

// 获取分类名称
function getCategoryName(categoryId: number) {
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : '-'
}

// 获取状态类型
function getStatusType(status: string) {
  const types: Record<string, any> = {
    PUBLISHED: 'success',
    DRAFT: 'info',
    ARCHIVED: 'warning'
  }
  return types[status] || 'info'
}

onMounted(() => {
  loadCategories()
  loadNews()
})
</script>

<style scoped>
.news-management {
  animation: fadeIn 0.5s ease;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(79, 172, 254, 0.3);
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

/* 内容卡片 */
.content-card {
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

.search-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.batch-actions {
  margin-bottom: 15px;
}

.batch-actions :deep(.el-alert__content) {
  width: 100%;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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

