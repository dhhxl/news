<template>
  <div class="audit-log-page">
    <div class="page-header">
      <h1>审计日志</h1>
      <div class="header-actions">
        <el-button @click="refreshLogs" :icon="Refresh">刷新</el-button>
        <el-button @click="showCleanupDialog" type="danger" :icon="Delete">
          清理日志
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filters" inline>
        <el-form-item label="操作类型">
          <el-select v-model="filters.operationType" placeholder="全部" clearable style="width: 150px">
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查看" value="VIEW" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="审核" value="REVIEW" />
            <el-option label="发布" value="PUBLISH" />
            <el-option label="归档" value="ARCHIVE" />
          </el-select>
        </el-form-item>

        <el-form-item label="目标实体">
          <el-select v-model="filters.targetEntity" placeholder="全部" clearable style="width: 150px">
            <el-option label="新闻" value="NEWS" />
            <el-option label="分类" value="CATEGORY" />
            <el-option label="用户" value="USER" />
            <el-option label="评论" value="COMMENT" />
            <el-option label="规则" value="CLASSIFICATION_RULE" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 300px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="search" :icon="Search">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志列表 -->
    <el-card>
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="8" animated />
      </div>

      <el-table v-else :data="logs" stripe style="width: 100%">
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.operationTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTagType(row.operationType)" size="small">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="目标实体" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ getTargetEntityText(row.targetEntity) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="实体ID" prop="targetEntityId" width="100" />

        <el-table-column label="操作者" width="150">
          <template #default="{ row }">
            <div class="operator-info">
              <el-icon><User /></el-icon>
              <span>{{ row.operatorUsername }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="IP地址" prop="ipAddress" width="150" />

        <el-table-column label="操作详情" min-width="200">
          <template #default="{ row }">
            <div class="operation-details">
              {{ row.operationDetails || '-' }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="showLogDetails(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadLogs"
          @size-change="loadLogs"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="日志详情"
      width="700px"
    >
      <div v-if="selectedLog" class="log-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="操作时间">
            {{ formatDateTime(selectedLog.operationTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTagType(selectedLog.operationType)">
              {{ getOperationTypeText(selectedLog.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="目标实体">
            {{ getTargetEntityText(selectedLog.targetEntity) }}
          </el-descriptions-item>
          <el-descriptions-item label="实体ID">
            {{ selectedLog.targetEntityId }}
          </el-descriptions-item>
          <el-descriptions-item label="操作者ID">
            {{ selectedLog.operatorId }}
          </el-descriptions-item>
          <el-descriptions-item label="操作者用户名">
            {{ selectedLog.operatorUsername }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址" :span="2">
            {{ selectedLog.ipAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="User-Agent" :span="2">
            <div class="user-agent">{{ selectedLog.userAgent || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="操作详情" :span="2">
            <div class="details-content">
              {{ selectedLog.operationDetails || '无' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 清理对话框 -->
    <el-dialog
      v-model="cleanupDialogVisible"
      title="清理旧日志"
      width="500px"
    >
      <el-form :model="cleanupForm" label-width="120px">
        <el-form-item label="保留天数">
          <el-input-number
            v-model="cleanupForm.daysToKeep"
            :min="1"
            :max="365"
            :step="1"
          />
          <div class="form-tip">
            将删除 {{ cleanupForm.daysToKeep }} 天前的所有日志
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="cleanupDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmCleanup" :loading="cleaning">
          确认清理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete, Search, User } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface AuditLog {
  id: number
  operationType: string
  targetEntity: string
  targetEntityId: number
  operatorId: number
  operatorUsername: string
  operationDetails: string
  ipAddress: string
  userAgent: string
  operationTime: string
}

const loading = ref(false)
const logs = ref<AuditLog[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filters = reactive({
  operationType: '',
  targetEntity: '',
  timeRange: null as string[] | null
})

const detailsDialogVisible = ref(false)
const selectedLog = ref<AuditLog | null>(null)

const cleanupDialogVisible = ref(false)
const cleaning = ref(false)
const cleanupForm = reactive({
  daysToKeep: 90
})

onMounted(() => {
  loadLogs()
})

/**
 * 加载日志
 */
async function loadLogs() {
  try {
    loading.value = true
    const params: any = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    if (filters.operationType) {
      params.operationType = filters.operationType
    }
    if (filters.targetEntity) {
      params.targetEntity = filters.targetEntity
    }
    if (filters.timeRange && filters.timeRange.length === 2) {
      params.startTime = filters.timeRange[0] + 'T00:00:00'
      params.endTime = filters.timeRange[1] + 'T23:59:59'
    }

    const response = await request.get('/admin/audit-logs', { params })
    logs.value = response.content
    total.value = response.totalElements
  } catch (error) {
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
function search() {
  currentPage.value = 1
  loadLogs()
}

/**
 * 重置筛选
 */
function resetFilters() {
  filters.operationType = ''
  filters.targetEntity = ''
  filters.timeRange = null
  search()
}

/**
 * 刷新日志
 */
function refreshLogs() {
  loadLogs()
  ElMessage.success('已刷新')
}

/**
 * 显示日志详情
 */
function showLogDetails(log: AuditLog) {
  selectedLog.value = log
  detailsDialogVisible.value = true
}

/**
 * 显示清理对话框
 */
function showCleanupDialog() {
  cleanupDialogVisible.value = true
}

/**
 * 确认清理
 */
async function confirmCleanup() {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${cleanupForm.daysToKeep} 天前的所有日志吗？此操作不可恢复！`,
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    cleaning.value = true
    await request.delete('/admin/audit-logs/cleanup', {
      params: { daysToKeep: cleanupForm.daysToKeep }
    })

    ElMessage.success('清理完成')
    cleanupDialogVisible.value = false
    loadLogs()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('清理失败')
    }
  } finally {
    cleaning.value = false
  }
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateTime: string) {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

/**
 * 获取操作类型标签类型
 */
function getOperationTypeTagType(type: string) {
  const typeMap: Record<string, string> = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    VIEW: 'info',
    LOGIN: '',
    LOGOUT: 'info',
    REVIEW: 'primary',
    PUBLISH: 'success',
    ARCHIVE: 'info'
  }
  return typeMap[type] || ''
}

/**
 * 获取操作类型文本
 */
function getOperationTypeText(type: string) {
  const textMap: Record<string, string> = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    VIEW: '查看',
    LOGIN: '登录',
    LOGOUT: '登出',
    REVIEW: '审核',
    PUBLISH: '发布',
    ARCHIVE: '归档'
  }
  return textMap[type] || type
}

/**
 * 获取目标实体文本
 */
function getTargetEntityText(entity: string) {
  const textMap: Record<string, string> = {
    NEWS: '新闻',
    CATEGORY: '分类',
    USER: '用户',
    COMMENT: '评论',
    CLASSIFICATION_RULE: '分类规则',
    CRAWLER_TASK: '爬虫任务'
  }
  return textMap[entity] || entity
}
</script>

<style scoped>
.audit-log-page {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #2c7a3e 0%, #38a169 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 16px;
}

.loading-state {
  padding: 20px;
}

.operator-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.operation-details {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.log-details {
  padding: 16px 0;
}

.user-agent {
  word-break: break-all;
  line-height: 1.6;
  color: #666;
  font-size: 13px;
}

.details-content {
  word-break: break-word;
  line-height: 1.6;
  color: #333;
}

.form-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
</style>
