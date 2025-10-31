<template>
  <div class="news-review">
    <!-- 页面标题 -->
      <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon class="title-icon"><CircleCheck /></el-icon>
          新闻审核管理
        </h2>
        <p class="page-desc">审核待发布的新闻内容，确保内容质量和合规性</p>
      </div>
        <div class="header-stats">
          <el-statistic title="待审核" :value="stats.pending" />
          <el-statistic title="审核中" :value="stats.reviewing" />
          <el-statistic title="已处理" :value="stats.processed" />
        </div>
      </div>

    <el-card shadow="hover" class="content-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><List /></el-icon>
            审核列表
          </span>
          <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="header-tabs">
          <el-tab-pane label="待审核" name="pending" />
          <el-tab-pane label="审核中" name="reviewing" />
          <el-tab-pane label="全部" name="all" />
        </el-tabs>
        </div>
      </template>

      <!-- 审核表格 -->
      <el-table
        :data="reviewList"
        v-loading="loading"
        style="width: 100%"
        stripe
      >
        <el-table-column label="序号" width="80">
          <template #default="scope">
            {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        
        <el-table-column label="标题" min-width="250">
          <template #default="scope">
            <div class="title-cell">
              <span class="news-title">{{ scope.row.title }}</span>
              <el-tag v-if="scope.row.isOverdue" type="danger" size="small" style="margin-left: 8px">
                超时
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="categoryName" label="分类" width="120" />
        
        <el-table-column label="提交人" width="120">
          <template #default="scope">
            {{ scope.row.submittedByUsername }}
          </template>
        </el-table-column>

        <el-table-column label="提交时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.submittedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="截止时间" width="160">
          <template #default="scope">
            <span :class="{ 'text-danger': scope.row.isOverdue }">
              {{ scope.row.reviewDeadline ? formatDateTime(scope.row.reviewDeadline) : '-' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
              <el-button 
                type="primary" 
                size="small"
              @click="viewNews(scope.row)"
              >
              查看
              </el-button>
              
              <el-button 
              v-if="scope.row.status === 'PENDING'"
                type="success" 
                size="small"
              @click="approveNews(scope.row)"
              >
                通过
              </el-button>
              
              <el-button 
              v-if="scope.row.status === 'PENDING' || scope.row.status === 'REVIEWING'"
                type="danger" 
                size="small"
              @click="rejectNews(scope.row)"
              >
                拒绝
              </el-button>
                </template>
        </el-table-column>
      </el-table>

        <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadReviewList"
            @size-change="loadReviewList"
          />
        </div>
      </el-card>

    <!-- 审核详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="新闻详情"
      width="70%"
      :close-on-click-modal="false"
    >
      <div v-if="currentNews" class="news-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题" :span="2">
            {{ currentNews.title }}
          </el-descriptions-item>
          <el-descriptions-item label="分类">
            {{ currentNews.categoryName }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentNews.status)">
              {{ getStatusText(currentNews.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交人">
            {{ currentNews.submittedByUsername }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDateTime(currentNews.submittedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="内容" :span="2">
            <div class="content-preview">{{ currentNews.content }}</div>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentNews.reviewNotes" class="review-notes">
          <h4>审核备注</h4>
          <p>{{ currentNews.reviewNotes }}</p>
        </div>
    </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          v-if="currentNews && currentNews.status === 'PENDING'"
          type="success"
          @click="approveNews(currentNews)"
        >
          通过
        </el-button>
        <el-button 
          v-if="currentNews && (currentNews.status === 'PENDING' || currentNews.status === 'REVIEWING')"
          type="danger"
          @click="rejectNews(currentNews)"
        >
          拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :title="approvalAction === 'approve' ? '通过审核' : '拒绝审核'"
      width="500px"
    >
      <el-form :model="approvalForm" label-width="80px">
        <el-form-item label="审核备注">
          <el-input
            v-model="approvalForm.notes"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见..."
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button 
          :type="approvalAction === 'approve' ? 'success' : 'danger'"
          @click="confirmApproval"
          :loading="approving"
        >
          确认{{ approvalAction === 'approve' ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCheck, List, Picture, More, Document, User } from '@element-plus/icons-vue'
import { getPendingReviews, getReviewingNews, getAllReviewableNews, reviewNews, getReviewStats } from '@/api/admin'
import type { NewsReviewRequest } from '@/api/editor'

// 状态数据
const activeTab = ref('pending')
const loading = ref(false)
const reviewList = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const stats = reactive({
  pending: 0,
  reviewing: 0,
  processed: 0
})

// 对话框
const detailDialogVisible = ref(false)
const approvalDialogVisible = ref(false)
const currentNews = ref<any>(null)
const approvalAction = ref<'approve' | 'reject'>('approve')
const approving = ref(false)

const approvalForm = reactive({
  notes: ''
})

// 加载审核列表
const loadReviewList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端从0开始
      size: pageSize.value
    }
    
    let response
    if (activeTab.value === 'pending') {
      response = await getPendingReviews(params)
    } else if (activeTab.value === 'reviewing') {
      response = await getReviewingNews(params)
    } else {
      response = await getAllReviewableNews(params)
    }
    
    reviewList.value = response.content || []
    total.value = response.totalElements || 0
    
    // 加载统计数据
    await loadStats()
  } catch (error: any) {
    console.error('加载审核列表失败:', error)
    ElMessage.error(error.response?.data?.message || '加载审核列表失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const statsData = await getReviewStats()
    stats.pending = statsData.pending || 0
    stats.reviewing = statsData.reviewing || 0
    stats.processed = (statsData.approved || 0) + (statsData.rejected || 0)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 切换标签
const handleTabChange = (tab: string) => {
  currentPage.value = 1
  loadReviewList()
}

// 查看详情
const viewNews = (item: any) => {
  currentNews.value = item
  detailDialogVisible.value = true
}

// 通过审核
const approveNews = (item: any) => {
  currentNews.value = item
  approvalAction.value = 'approve'
  approvalForm.notes = ''
  approvalDialogVisible.value = true
}

// 拒绝审核
const rejectNews = (item: any) => {
  currentNews.value = item
  approvalAction.value = 'reject'
  approvalForm.notes = ''
  approvalDialogVisible.value = true
}

// 确认审批
const confirmApproval = async () => {
  if (approvalAction.value === 'reject' && !approvalForm.notes.trim()) {
    ElMessage.warning('拒绝审核时必须填写备注')
    return
  }

  approving.value = true
  try {
    const request: NewsReviewRequest = {
      newsId: currentNews.value.newsId,
      action: approvalAction.value === 'approve' ? 'APPROVE' : 'REJECT',
      reviewComment: approvalForm.notes || undefined
    }
    
    await reviewNews(request)
    
    ElMessage.success(`审核${approvalAction.value === 'approve' ? '通过' : '拒绝'}成功`)
    approvalDialogVisible.value = false
    detailDialogVisible.value = false
    await loadReviewList()
  } catch (error: any) {
    console.error('审核操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    approving.value = false
  }
}

// 获取状态类型
const getStatusTagType = (status: string) => {
  const typeMap: Record<string, any> = {
    PENDING: 'warning',
    REVIEWING: 'primary',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    PENDING: '待审核',
    REVIEWING: '审核中',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return textMap[status] || status
}

// 格式化时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 初始化
onMounted(() => {
  loadReviewList()
})
</script>

<style scoped lang="scss">
.news-review {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
  
  .header-content {
    flex: 1;
    
    .page-title {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 28px;
      font-weight: 700;
      margin: 0 0 8px;
      color: #000;
      
      .title-icon {
        font-size: 28px;
      }
    }
    
    .page-desc {
  margin: 0;
      color: #6b7280;
      font-size: 14px;
    }
}

.header-stats {
  display: flex;
  gap: 32px;
}
}

.content-card {
  border-radius: 12px;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #e5e7eb;
  }
  
  .card-header {
  display: flex;
    justify-content: space-between;
    align-items: center;
    
    .card-title {
  display: flex;
      align-items: center;
  gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #000;
    }
    
    .header-tabs {
      :deep(.el-tabs__header) {
        margin: 0;
      }
    }
  }
}

.title-cell {
  display: flex;
  align-items: center;
  
  .news-title {
    font-weight: 500;
    color: #000;
  }
}

.text-danger {
  color: #ef4444;
  font-weight: 500;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 10px;
}

.news-detail {
  .content-preview {
    max-height: 400px;
    overflow-y: auto;
    line-height: 1.6;
  }
  
  .review-notes {
    margin-top: 20px;
    padding: 16px;
    background: #f9fafb;
    border-radius: 8px;
    
    h4 {
      margin: 0 0 12px;
      color: #000;
    }
    
    p {
      margin: 0;
      color: #6b7280;
      line-height: 1.6;
    }
  }
}
</style>
