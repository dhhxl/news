<template>
  <div class="news-review">
    <div class="container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>新闻审核管理</h1>
        <div class="header-stats">
          <el-statistic title="待审核" :value="stats.pending" />
          <el-statistic title="审核中" :value="stats.reviewing" />
          <el-statistic title="已处理" :value="stats.processed" />
        </div>
      </div>

      <!-- 筛选器 -->
      <el-card class="filter-card" shadow="never">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="待审核" name="pending" />
          <el-tab-pane label="审核中" name="reviewing" />
          <el-tab-pane label="全部" name="all" />
        </el-tabs>
      </el-card>

      <!-- 审核列表 -->
      <el-card class="review-list-card">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else-if="reviewList.length > 0" class="review-list">
          <div 
            v-for="item in reviewList" 
            :key="item.newsId"
            class="review-item"
          >
            <!-- 状态和优先级 -->
            <div class="item-status">
              <el-tag 
                :type="getStatusTagType(item.status)"
                size="large"
              >
                {{ getStatusText(item.status) }}
              </el-tag>
              <div v-if="item.isOverdue" class="overdue-badge">
                <el-tag type="danger" size="small">超时</el-tag>
              </div>
            </div>

            <!-- 新闻信息 -->
            <div class="item-content">
              <div class="news-header">
                <h3 class="news-title">{{ item.title }}</h3>
                <div class="news-meta">
                  <span>提交人: {{ item.submittedByUsername }}</span>
                  <span>提交时间: {{ formatDateTime(item.submittedAt) }}</span>
                  <span v-if="item.reviewDeadline">
                    截止时间: {{ formatDateTime(item.reviewDeadline) }}
                  </span>
                </div>
              </div>

              <div class="news-preview">
                {{ item.content.substring(0, 300) }}...
              </div>

              <div class="news-extra">
                <div v-if="item.imageCount > 0" class="image-count">
                  <el-icon><Picture /></el-icon>
                  {{ item.imageCount }} 张图片
                </div>
                <div class="category">
                  分类: {{ item.categoryName || '未知' }}
                </div>
              </div>
            </div>

            <!-- 审核操作 -->
            <div class="item-actions">
              <el-button 
                type="primary" 
                size="small"
                @click="viewNews(item)"
              >
                查看详情
              </el-button>
              
              <el-button 
                v-if="item.status === 'PENDING'"
                type="success" 
                size="small"
                @click="approveNews(item)"
              >
                通过
              </el-button>
              
              <el-button 
                v-if="item.status === 'PENDING' || item.status === 'REVIEWING'"
                type="danger" 
                size="small"
                @click="rejectNews(item)"
              >
                拒绝
              </el-button>

              <el-dropdown @command="handleCommand" trigger="click">
                <el-button size="small" :icon="More" circle />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      :command="{action: 'history', item}"
                      :icon="Document"
                    >
                      审核历史
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="{action: 'assign', item}"
                      :icon="User"
                    >
                      分配审核人
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <el-empty v-else description="暂无待审核新闻" />

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next, jumper"
            @current-change="loadReviewList"
            @size-change="loadReviewList"
          />
        </div>
      </el-card>
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewAction === 'APPROVE' ? '审核通过' : '审核拒绝'"
      width="500px"
    >
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="4"
            placeholder="请填写审核意见（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmReview"
          :loading="reviewing"
        >
          确认{{ reviewAction === 'APPROVE' ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { More, Document, User, Picture } from '@element-plus/icons-vue'
import { 
  getPendingReviews,
  getReviewingNews, 
  getAllReviewableNews,
  reviewNews,
  type NewsReviewResponse 
} from '@/api/admin'

const router = useRouter()

// 状态数据
const loading = ref(false)
const reviewing = ref(false)
const activeTab = ref('pending')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const reviewList = ref<NewsReviewResponse[]>([])

// 统计数据
const stats = reactive({
  pending: 0,
  reviewing: 0,
  processed: 0
})

// 审核相关
const reviewDialogVisible = ref(false)
const reviewAction = ref<'APPROVE' | 'REJECT'>('APPROVE')
const currentReviewItem = ref<NewsReviewResponse | null>(null)
const reviewForm = reactive({
  reviewComment: ''
})

// 页面初始化
onMounted(() => {
  loadReviewList()
})

/**
 * 加载审核列表
 */
async function loadReviewList() {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    let response
    switch (activeTab.value) {
      case 'pending':
        response = await getPendingReviews(params)
        break
      case 'reviewing':
        response = await getReviewingNews(params)
        break
      default:
        response = await getAllReviewableNews(params)
    }
    
    reviewList.value = response.content
    total.value = response.totalElements
    
    // TODO: 更新统计数据
    
  } catch (error) {
    ElMessage.error('加载审核列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 切换标签页
 */
function handleTabChange() {
  currentPage.value = 1
  loadReviewList()
}

/**
 * 查看新闻详情
 */
function viewNews(item: NewsReviewResponse) {
  router.push(`/news/${item.newsId}`)
}

/**
 * 通过审核
 */
function approveNews(item: NewsReviewResponse) {
  reviewAction.value = 'APPROVE'
  currentReviewItem.value = item
  reviewForm.reviewComment = ''
  reviewDialogVisible.value = true
}

/**
 * 拒绝审核
 */
function rejectNews(item: NewsReviewResponse) {
  reviewAction.value = 'REJECT'
  currentReviewItem.value = item
  reviewForm.reviewComment = ''
  reviewDialogVisible.value = true
}

/**
 * 确认审核
 */
async function confirmReview() {
  if (!currentReviewItem.value) return
  
  try {
    reviewing.value = true
    
    await reviewNews({
      newsId: currentReviewItem.value.newsId,
      action: reviewAction.value,
      reviewComment: reviewForm.reviewComment
    })
    
    ElMessage.success(`审核${reviewAction.value === 'APPROVE' ? '通过' : '拒绝'}成功`)
    reviewDialogVisible.value = false
    loadReviewList()
    
  } catch (error) {
    ElMessage.error('审核操作失败')
  } finally {
    reviewing.value = false
  }
}

/**
 * 处理下拉菜单命令
 */
function handleCommand(command: any) {
  const { action, item } = command
  
  switch (action) {
    case 'history':
      // TODO: 显示审核历史
      break
    case 'assign':
      // TODO: 分配审核人
      break
  }
}

/**
 * 获取状态标签类型
 */
function getStatusTagType(status: string) {
  const typeMap: Record<string, string> = {
    PENDING: 'warning',
    REVIEWING: 'primary',
    PUBLISHED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态文本
 */
function getStatusText(status: string) {
  const textMap: Record<string, string> = {
    PENDING: '待审核',
    REVIEWING: '审核中',
    PUBLISHED: '已发布', 
    REJECTED: '已拒绝'
  }
  return textMap[status] || status
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateTime: string) {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.news-review {
  padding: 20px;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-stats {
  display: flex;
  gap: 32px;
}

.filter-card,
.review-list-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading-state {
  padding: 20px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  transition: all 0.3s;
}

.review-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.item-status {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.news-header {
  margin-bottom: 12px;
}

.news-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.news-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
}

.news-preview {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.news-extra {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999;
}

.image-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #409eff;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.overdue-badge {
  text-align: center;
}
</style>
