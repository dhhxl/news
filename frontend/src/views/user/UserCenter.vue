<template>
  <div class="user-center-page">
    <div class="container">
      <h1>个人中心</h1>

      <el-card class="user-info-card">
        <template #header>
          <div class="card-header">
            <span>用户信息</span>
          </div>
        </template>
        <div class="user-info">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">
              {{ userStore.user?.username }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="userStore.user?.role === 'ADMIN' ? 'danger' : 'primary'">
                {{ userStore.user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ userStore.user?.email || '未设置' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <el-card class="my-comments-card">
        <template #header>
          <div class="card-header">
            <span>我的评论</span>
            <el-button type="primary" size="small" @click="loadMyComments">
              刷新
            </el-button>
          </div>
        </template>

        <div v-loading="loading" class="comments-list">
          <div
            v-for="comment in myComments"
            :key="comment.id"
            class="comment-item"
          >
            <div class="comment-header">
              <router-link
                :to="`/news/${comment.newsId}`"
                class="news-link"
              >
                查看新闻 →
              </router-link>
              <span class="time">{{ formatDateTime(comment.createdAt) }}</span>
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-actions">
              <el-button
                type="danger"
                size="small"
                text
                @click="deleteMyComment(comment.id)"
              >
                删除
              </el-button>
            </div>
          </div>

          <el-empty
            v-if="myComments.length === 0 && !loading"
            description="还没有发表过评论"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getNewsComments, deleteComment, type Comment } from '@/api/comment'

const userStore = useUserStore()
const myComments = ref<Comment[]>([])
const loading = ref(false)

// 加载我的评论（简化版 - 遍历所有新闻的评论并过滤）
async function loadMyComments() {
  if (!userStore.user) {
    ElMessage.error('请先登录')
    return
  }

  loading.value = true
  try {
    // 这里简化处理，实际应该在后端提供专门的接口
    // 暂时只能获取当前用户的评论通过遍历
    myComments.value = []
    ElMessage.info('功能开发中，请访问新闻页面查看评论')
  } catch (error) {
    console.error('Failed to load comments:', error)
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

// 删除评论
async function deleteMyComment(commentId: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteComment(commentId)
    ElMessage.success('评论已删除')
    await loadMyComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 格式化日期时间
function formatDateTime(time: string) {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadMyComments()
})
</script>

<style scoped>
.user-center-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px 0;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

h1 {
  color: #333;
  margin-bottom: 30px;
}

.user-info-card,
.my-comments-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  padding: 20px 0;
}

.comments-list {
  min-height: 200px;
}

.comment-item {
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 15px;
  transition: all 0.3s;
}

.comment-item:hover {
  background: #f5f5f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.news-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
}

.news-link:hover {
  text-decoration: underline;
}

.time {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #555;
  line-height: 1.6;
  font-size: 15px;
  margin: 10px 0;
  word-wrap: break-word;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}
</style>

