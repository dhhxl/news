<template>
  <div class="crawler-manage">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon class="title-icon"><Download /></el-icon>
          新闻采集管理
        </h2>
        <p class="page-desc">自动从多个新闻源采集最新新闻，支持智能去重和分类</p>
      </div>
      <div class="header-actions">
        <el-button 
          type="primary" 
          size="large" 
          :loading="crawlingAll"
          @click="triggerAllCrawlers"
        >
          <el-icon><Opportunity /></el-icon>
          启动所有爬虫
        </el-button>
        
        <el-button 
          type="success" 
          size="large"
          :loading="testing"
          @click="testAllCrawlers"
        >
          <el-icon><Search /></el-icon>
          测试连接
        </el-button>
      </div>
    </div>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="statistics-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">📈</div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalSuccessCrawled || 0 }}</div>
              <div class="stat-label">成功采集</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">❌</div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalFailedCrawled || 0 }}</div>
              <div class="stat-label">失败次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">📝</div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.recentTasksCount || 0 }}</div>
              <div class="stat-label">最近任务</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">🌐</div>
            <div class="stat-info">
              <div class="stat-value">{{ availableSources.length }}</div>
              <div class="stat-label">可用来源</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 爬虫源管理 -->
    <el-card class="sources-card">
      <template #header>
        <h3>📡 可用新闻源</h3>
      </template>

      <el-table :data="sourceList" stripe style="width: 100%">
        <el-table-column prop="name" label="新闻源" width="200">
          <template #default="{ row }">
            <el-tag :type="row.status === 'online' ? 'success' : 'danger'">
              {{ row.name }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="描述" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-badge 
              :value="row.status === 'online' ? '在线' : '离线'" 
              :type="row.status === 'online' ? 'success' : 'danger'"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="采集数量" width="150">
          <template #default="{ row }">
            <el-input-number 
              v-model="row.maxCount" 
              :min="5" 
              :max="50" 
              :step="5"
              size="small"
            />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small"
              :loading="row.crawling"
              :disabled="row.status !== 'online'"
              @click="triggerSingleCrawler(row)"
            >
              启动采集
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 任务历史 -->
    <el-card class="history-card">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h3>📜 采集历史</h3>
          <el-button size="small" @click="loadTaskHistory">刷新</el-button>
        </div>
      </template>

      <el-table :data="taskHistory" stripe style="width: 100%">
        <el-table-column prop="targetSource" label="来源" width="150" />
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag 
              :type="getStatusType(row.status)"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="successCount" label="成功" width="80" />
        <el-table-column prop="failCount" label="失败" width="80" />
        
        <el-table-column prop="startedAt" label="开始时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.startedAt) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="completedAt" label="完成时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.completedAt) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Download, Opportunity, Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

// 数据
const crawlingAll = ref(false);
const testing = ref(false);
const availableSources = ref<string[]>([]);
const sourceList = ref<any[]>([]);
const taskHistory = ref<any[]>([]);
const statistics = ref<any>({});

// 源信息映射
const sourceInfo: Record<string, any> = {
  'cctv': { name: 'CCTV新闻', description: '中央电视台官方新闻', icon: '📺' },
  'netease': { name: '网易新闻', description: '网易新闻频道', icon: '📱' }
};

// 加载可用源
const loadAvailableSources = async () => {
  try {
    const response: any = await request.get('/crawler/sources');
    availableSources.value = response;
    
    // 构建源列表
    sourceList.value = response.map((source: string) => ({
      name: sourceInfo[source]?.name || source,
      source: source,
      description: sourceInfo[source]?.description || '新闻源',
      status: 'unknown',
      maxCount: 10,
      crawling: false
    }));
  } catch (error) {
    console.error('Failed to load sources:', error);
    ElMessage.error('加载新闻源失败');
  }
};

// 测试所有爬虫
const testAllCrawlers = async () => {
  testing.value = true;
  try {
    const response: any = await request.get('/crawler/test');
    
    // 更新状态
    sourceList.value.forEach(source => {
      source.status = response[source.source] ? 'online' : 'offline';
    });
    
    const onlineCount = Object.values(response).filter(v => v).length;
    ElMessage.success(`测试完成：${onlineCount}/${sourceList.value.length} 个源在线`);
  } catch (error) {
    console.error('Failed to test crawlers:', error);
    ElMessage.error('测试失败');
  } finally {
    testing.value = false;
  }
};

// 触发单个爬虫
const triggerSingleCrawler = async (source: any) => {
  source.crawling = true;
  try {
    await request.post(`/crawler/trigger/${source.source}?maxCount=${source.maxCount}`);
    ElMessage.success(`${source.name} 采集任务已启动`);
    
    // 3秒后刷新历史
    setTimeout(() => {
      loadTaskHistory();
      loadStatistics();
    }, 3000);
  } catch (error: any) {
    console.error('Failed to trigger crawler:', error);
    ElMessage.error(error.response?.data?.message || '启动失败');
  } finally {
    source.crawling = false;
  }
};

// 触发所有爬虫
const triggerAllCrawlers = async () => {
  crawlingAll.value = true;
  try {
    const maxCount = sourceList.value[0]?.maxCount || 10;
    await request.post(`/crawler/trigger/all?maxCount=${maxCount}`);
    ElMessage.success('所有采集任务已启动');
    
    // 5秒后刷新历史
    setTimeout(() => {
      loadTaskHistory();
      loadStatistics();
    }, 5000);
  } catch (error: any) {
    console.error('Failed to trigger all crawlers:', error);
    ElMessage.error(error.response?.data?.message || '启动失败');
  } finally {
    crawlingAll.value = false;
  }
};

// 加载任务历史
const loadTaskHistory = async () => {
  try {
    const response: any = await request.get('/crawler/tasks?limit=20');
    taskHistory.value = response;
  } catch (error) {
    console.error('Failed to load task history:', error);
  }
};

// 加载统计信息
const loadStatistics = async () => {
  try {
    const response: any = await request.get('/crawler/statistics');
    statistics.value = response;
  } catch (error) {
    console.error('Failed to load statistics:', error);
  }
};

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString('zh-CN');
};

// 获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'RUNNING': 'warning',
    'PENDING': 'info'
  };
  return types[status] || 'info';
};

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'RUNNING': '运行中',
    'PENDING': '待执行'
  };
  return texts[status] || status;
};

// 初始化
onMounted(async () => {
  await loadAvailableSources();
  await testAllCrawlers();
  await loadTaskHistory();
  await loadStatistics();
});
</script>

<style scoped>
.crawler-manage {
  animation: fadeIn 0.5s ease;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(250, 112, 154, 0.3);
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

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.statistics-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  padding: 10px 0;
}

.stat-icon {
  font-size: 48px;
}

.stat-info {
  text-align: left;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

/* 卡片样式 */
.sources-card,
.history-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.sources-card h3,
.history-card h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
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
</style>
