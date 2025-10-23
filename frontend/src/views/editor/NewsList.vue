<template>
  <div class="editor-news-list">
    <div class="container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>我的新闻</h1>
        <div class="header-actions">
          <el-button type="primary" @click="createNews" :icon="Plus">
            创建新闻
          </el-button>
        </div>
      </div>

      <!-- 筛选器 -->
      <el-card class="filter-card" shadow="never">
        <div class="filters">
          <el-radio-group v-model="currentStatus" @change="loadNewsList">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="DRAFT">
              草稿 <el-badge :value="statusCounts.DRAFT" type="info" />
            </el-radio-button>
            <el-radio-button label="PENDING">
              待审核 <el-badge :value="statusCounts.PENDING" type="warning" />
            </el-radio-button>
            <el-radio-button label="REVIEWING">
              审核中 <el-badge :value="statusCounts.REVIEWING" type="primary" />
            </el-radio-button>
            <el-radio-button label="PUBLISHED">
              已发布 <el-badge :value="statusCounts.PUBLISHED" type="success" />
            </el-radio-button>
            <el-radio-button label="REJECTED">
              已退回 <el-badge :value="statusCounts.REJECTED" type="danger" />
            </el-radio-button>
          </el-radio-group>
        </div>
      </el-card>

      <!-- 新闻列表 -->
      <el-card class="news-list-card">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 新闻列表 -->
        <div v-else-if="newsList.length > 0" class="news-list">
          <div 
            v-for="news in newsList" 
            :key="news.id"
            class="news-item"
          >
            <!-- 新闻状态标签 -->
            <div class="status-tag">
              <el-tag 
                :type="getStatusTagType(news.status)"
                size="small"
              >
                {{ getStatusText(news.status) }}
              </el-tag>
            </div>

            <!-- 新闻内容 -->
            <div class="news-content">
              <!-- 标题和元信息 -->
              <div class="news-header">
                <h3 class="news-title" @click="editNews(news)">
                  {{ news.title }}
                </h3>
                <div class="news-meta">
                  <span class="create-time">
                    创建于 {{ formatDateTime(news.crawlTime) }}
                  </span>
                  <span v-if="news.submittedAt" class="submit-time">
                    提交于 {{ formatDateTime(news.submittedAt) }}
                  </span>
                  <span v-if="news.reviewDeadline && news.needsReview" class="deadline">
                    截止 {{ formatDateTime(news.reviewDeadline) }}
                    <el-tag v-if="news.isOverdue" type="danger" size="small">已超时</el-tag>
                  </span>
                </div>
              </div>

              <!-- 内容预览 -->
              <div class="news-preview">
                {{ news.content.substring(0, 200) }}...
              </div>

              <!-- 图片信息 -->
              <div v-if="news.imageCount > 0" class="image-info">
                <el-icon><Picture /></el-icon>
                <span>{{ news.imageCount }} 张图片</span>
              </div>

              <!-- 审核信息 -->
              <div v-if="news.needsReview" class="review-info">
                <div v-if="news.currentReviewerUsername" class="reviewer">
                  审核人：{{ news.currentReviewerUsername }}
                </div>
                <div v-if="news.reviewHistory && news.reviewHistory.length > 0" class="latest-review">
                  最新：{{ news.reviewHistory[0].reviewComment }}
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="news-actions">
              <el-button 
                v-if="news.canEdit" 
                type="primary" 
                size="small"
                @click="editNews(news)"
              >
                编辑
              </el-button>
              
              <el-button 
                v-if="news.status === 'REJECTED'" 
                type="warning" 
                size="small"
                @click="resubmitNews(news)"
              >
                重新提交
              </el-button>
              
              <el-button 
                v-if="news.status === 'DRAFT'" 
                type="success" 
                size="small"
                @click="submitForReview(news.id)"
              >
                提交审核
              </el-button>

              <el-dropdown @command="handleCommand" trigger="click">
                <el-button size="small" :icon="More" circle />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item 
                      :command="{action: 'view', news}"
                      :icon="View"
                    >
                      查看详情
                    </el-dropdown-item>
                    <el-dropdown-item 
                      :command="{action: 'history', news}"
                      :icon="Document"
                    >
                      审核历史
                    </el-dropdown-item>
                    <el-dropdown-item 
                      v-if="news.status === 'DRAFT'"
                      :command="{action: 'delete', news}"
                      :icon="Delete"
                      divided
                    >
                      删除草稿
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty 
          v-else 
          description="暂无新闻"
          :image-size="120"
        >
          <el-button type="primary" @click="createNews">创建第一篇新闻</el-button>
        </el-empty>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadNewsList"
            @size-change="loadNewsList"
          />
        </div>
      </el-card>
    </div>

    <!-- 审核历史对话框 -->
    <el-dialog
      v-model="reviewHistoryVisible"
      title="审核历史"
      width="600px"
    >
      <div class="review-history">
        <el-timeline>
          <el-timeline-item
            v-for="item in reviewHistory"
            :key="item.reviewedAt"
            :timestamp="formatDateTime(item.reviewedAt)"
          >
            <div class="review-item">
              <div class="review-header">
                <span class="action">{{ getActionText(item.action) }}</span>
                <span class="reviewer">{{ item.reviewerUsername }}</span>
              </div>
              <div v-if="item.reviewComment" class="review-comment">
                {{ item.reviewComment }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, More, View, Document, Delete, Picture 
} from '@element-plus/icons-vue'
import { 
  getMyNews, 
  getMyNewsByStatus, 
  deleteDraft,
  submitForReview as submitForReviewApi,
  getReviewHistory,
  type NewsReviewResponse 
} from '@/api/editor'

const router = useRouter()

// 状态数据
const loading = ref(false)
const currentStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const newsList = ref<NewsReviewResponse[]>([])
const statusCounts = reactive({
  DRAFT: 0,
  PENDING: 0,
  REVIEWING: 0,
  PUBLISHED: 0,
  REJECTED: 0
})

// 审核历史相关
const reviewHistoryVisible = ref(false)
const reviewHistory = ref([])

// 页面初始化
onMounted(() => {
  loadNewsList()
})

/**
 * 加载新闻列表
 */
async function loadNewsList() {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    let response
    if (currentStatus.value) {
      response = await getMyNewsByStatus(currentStatus.value, params)
    } else {
      response = await getMyNews(params)
    }
    
    newsList.value = response.content
    total.value = response.totalElements
    
    // TODO: 加载状态统计
    
  } catch (error) {
    ElMessage.error('加载新闻列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 创建新闻
 */
function createNews() {
  router.push('/editor/news-editor')
}

/**
 * 编辑新闻
 */
function editNews(news: any) {
  router.push(`/editor/news-editor/${news.id}`)
}

/**
 * 重新提交新闻
 */
function resubmitNews(news: any) {
  router.push(`/editor/news-editor/${news.id}`)
}

/**
 * 提交审核
 */
async function submitForReview(newsId: number) {
  try {
    await ElMessageBox.confirm(
      '确定要提交这篇新闻进行审核吗？',
      '确认提交',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await submitForReviewApi(newsId)
    ElMessage.success('提交成功')
    loadNewsList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

/**
 * 处理下拉菜单命令
 */
async function handleCommand(command: any) {
  const { action, news } = command
  
  switch (action) {
    case 'view':
      // 查看新闻详情
      router.push(`/news/${news.id}`)
      break
      
    case 'history':
      // 查看审核历史
      await showReviewHistory(news.id)
      break
      
    case 'delete':
      // 删除草稿
      await handleDeleteDraft(news.id)
      break
  }
}

/**
 * 显示审核历史
 */
async function showReviewHistory(newsId: number) {
  try {
    const history = await getReviewHistory(newsId)
    reviewHistory.value = history
    reviewHistoryVisible.value = true
  } catch (error) {
    ElMessage.error('获取审核历史失败')
  }
}

/**
 * 删除草稿
 */
async function handleDeleteDraft(newsId: number) {
  try {
    await ElMessageBox.confirm(
      '确定要删除这篇草稿吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteDraft(newsId)
    ElMessage.success('删除成功')
    loadNewsList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 获取状态标签类型
 */
function getStatusTagType(status: string) {
  const typeMap: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning', 
    REVIEWING: 'primary',
    PUBLISHED: 'success',
    REJECTED: 'danger',
    ARCHIVED: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 获取状态文本
 */
function getStatusText(status: string) {
  const textMap: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待审核',
    REVIEWING: '审核中', 
    PUBLISHED: '已发布',
    REJECTED: '已退回',
    ARCHIVED: '已归档'
  }
  return textMap[status] || status
}

/**
 * 获取动作文本
 */
function getActionText(action: string) {
  const textMap: Record<string, string> = {
    SUBMIT: '提交审核',
    APPROVE: '审核通过',
    REJECT: '审核拒绝',
    REQUEST_CHANGES: '要求修改'
  }
  return textMap[action] || action
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
.editor-news-list {
  padding: 20px;
}

.container {
  max-width: 1200px;
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

.filter-card {
  margin-bottom: 20px;
}

.filters {
  display: flex;
  align-items: center;
  gap: 16px;
}

.news-list-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading-state {
  padding: 20px;
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.news-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  transition: all 0.3s;
}

.news-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.status-tag {
  flex-shrink: 0;
}

.news-content {
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
  cursor: pointer;
  transition: color 0.3s;
}

.news-title:hover {
  color: #409eff;
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

.image-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  margin-bottom: 8px;
}

.review-info {
  font-size: 13px;
  color: #666;
}

.reviewer {
  margin-bottom: 4px;
}

.latest-review {
  color: #999;
}

.news-actions {
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

.review-history {
  max-height: 400px;
  overflow-y: auto;
}

.review-item {
  padding: 8px 0;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.action {
  font-weight: 600;
  color: #333;
}

.reviewer {
  color: #666;
  font-size: 13px;
}

.review-comment {
  color: #666;
  line-height: 1.6;
}
</style>
