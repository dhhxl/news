<template>
  <div class="news-editor">
    <div class="container">
      <!-- 页面标题 -->
      <div class="page-header">
        <el-button 
          @click="goBack" 
          :icon="ArrowLeft" 
          circle
          class="back-btn"
        />
        <h1>{{ isEditing ? '编辑新闻' : '创建新闻' }}</h1>
        <div class="header-actions">
          <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
          <el-button 
            type="primary" 
            @click="submitNews" 
            :loading="submitting"
            :disabled="!canSubmit"
          >
            提交审核
          </el-button>
        </div>
      </div>

      <!-- 编辑表单 -->
      <el-card class="editor-card">
        <el-form
          ref="newsFormRef"
          :model="newsForm"
          :rules="newsRules"
          label-width="100px"
        >
          <!-- 标题 -->
          <el-form-item label="新闻标题" prop="title">
            <el-input
              v-model="newsForm.title"
              placeholder="请输入新闻标题"
              maxlength="255"
              show-word-limit
              size="large"
            />
          </el-form-item>

          <!-- 分类 -->
          <el-form-item label="新闻分类" prop="categoryId">
            <el-select
              v-model="newsForm.categoryId"
              placeholder="请选择新闻分类"
              style="width: 300px"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>

          <!-- 原始链接 -->
          <el-form-item label="原始链接" prop="originalUrl">
            <el-input
              v-model="newsForm.originalUrl"
              placeholder="请输入原始新闻链接（可选）"
              maxlength="500"
            />
          </el-form-item>

          <!-- 图片上传 -->
          <el-form-item label="新闻配图">
            <div class="image-upload-section">
              <!-- 上传按钮 -->
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleFileChange"
                multiple
                accept="image/*"
                class="image-uploader"
              >
                <el-button :icon="Plus" type="primary">添加图片</el-button>
              </el-upload>

              <!-- 已选择的图片 -->
              <div v-if="selectedImages.length > 0" class="selected-images">
                <div 
                  v-for="(image, index) in selectedImages" 
                  :key="image.id || index"
                  class="image-item"
                >
                  <img :src="image.url" :alt="image.name" />
                  <div class="image-actions">
                    <span class="image-name">{{ image.name }}</span>
                    <el-button
                      :icon="Delete"
                      type="danger"
                      size="small"
                      circle
                      @click="removeImage(index)"
                    />
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>

          <!-- 内容编辑器 -->
          <el-form-item label="新闻内容" prop="content">
            <div class="editor-container">
              <el-input
                v-model="newsForm.content"
                type="textarea"
                :rows="20"
                placeholder="请输入新闻内容..."
                maxlength="50000"
                show-word-limit
              />
            </div>
          </el-form-item>

          <!-- 提交说明 -->
          <el-form-item label="提交说明">
            <el-input
              v-model="newsForm.submitNote"
              type="textarea"
              :rows="3"
              placeholder="可以在这里添加提交说明或备注（可选）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { ArrowLeft, Plus, Delete } from '@element-plus/icons-vue'
import { getCategories, type Category } from '@/api/news'
import { 
  createDraft, 
  submitNews as submitNewsApi, 
  updateNews,
  resubmitNews as resubmitNewsApi, 
  getNewsForEdit,
  uploadImage,
  getUnusedImages,
  type NewsSubmitRequest,
  type UploadedImage 
} from '@/api/editor'

const route = useRoute()
const router = useRouter()

// 表单引用
const newsFormRef = ref<FormInstance>()

// 是否编辑模式
const isEditing = computed(() => !!route.params.id)

// 新闻状态（用于判断是否是被驳回的新闻）
const newsStatus = ref<string>('')

// 表单数据
const newsForm = reactive<NewsSubmitRequest & { submitNote?: string }>({
  title: '',
  content: '',
  categoryId: 0,
  originalUrl: '',
  imageIds: [],
  submitType: 'DRAFT',
  submitNote: ''
})

// 表单验证规则
const newsRules = {
  title: [
    { required: true, message: '请输入新闻标题', trigger: 'blur' },
    { min: 5, max: 255, message: '标题长度在 5 到 255 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择新闻分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入新闻内容', trigger: 'blur' },
    { min: 20, message: '内容至少需要20个字符', trigger: 'blur' }
  ]
}

// 状态
const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const categories = ref<Category[]>([])

// 图片相关
const selectedImages = ref<Array<{
  id?: number
  file?: File
  url: string
  name: string
}>>([])

// 计算属性
const canSubmit = computed(() => {
  return newsForm.title && newsForm.content && newsForm.categoryId
})

// 页面初始化
onMounted(async () => {
  await loadCategories()
  await loadUnusedImages()
  
  if (isEditing.value) {
    await loadNewsForEdit()
  }
})

/**
 * 加载分类列表
 */
async function loadCategories() {
  try {
    const response = await getCategories()
    categories.value = response
  } catch (error) {
    ElMessage.error('加载分类失败')
  }
}

/**
 * 加载未使用的图片
 */
async function loadUnusedImages() {
  try {
    const images = await getUnusedImages()
    // 这里可以显示用户之前上传但未使用的图片供选择
  } catch (error) {
    console.error('加载未使用图片失败:', error)
  }
}

/**
 * 加载编辑的新闻
 */
async function loadNewsForEdit() {
  if (!route.params.id) return
  
  try {
    loading.value = true
    const newsId = Number(route.params.id)
    const news = await getNewsForEdit(newsId)
    
    // 保存新闻状态
    newsStatus.value = news.status
    
    // 填充表单
    newsForm.title = news.title
    newsForm.content = news.content
    newsForm.categoryId = news.categoryId
    newsForm.originalUrl = news.originalUrl || ''
    
    // 加载关联的图片
    if (news.imageUrls && news.imageUrls.length > 0) {
      selectedImages.value = news.imageUrls.map((url, index) => ({
        id: index, // 临时ID
        url: url,
        name: `图片${index + 1}`
      }))
    }
  } catch (error) {
    ElMessage.error('加载新闻失败')
    router.push('/editor/news-list')
  } finally {
    loading.value = false
  }
}

/**
 * 处理文件选择
 */
async function handleFileChange(file: any) {
  if (!file.raw) return
  
  // 验证文件类型
  if (!file.raw.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 验证文件大小 (10MB)
  if (file.raw.size > 10 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过10MB')
    return
  }
  
  try {
    // 上传图片
    const uploadedImage = await uploadImage(file.raw)
    
    // 添加到选中列表
    selectedImages.value.push({
      id: uploadedImage.id,
      url: uploadedImage.accessUrl,
      name: uploadedImage.originalName
    })
    
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
}

/**
 * 移除图片
 */
function removeImage(index: number) {
  selectedImages.value.splice(index, 1)
}

/**
 * 保存草稿
 */
async function saveDraft() {
  try {
    await newsFormRef.value?.validate()
  } catch (error) {
    return
  }
  
  try {
    saving.value = true
    newsForm.submitType = 'DRAFT'
    newsForm.imageIds = selectedImages.value
      .filter(img => img.id)
      .map(img => img.id!)
    
    if (isEditing.value) {
      await updateNews(Number(route.params.id), newsForm)
      ElMessage.success('草稿更新成功')
    } else {
      await createDraft(newsForm)
      ElMessage.success('草稿保存成功')
      router.push('/editor/news-list')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 提交审核
 */
async function submitNews() {
  try {
    await newsFormRef.value?.validate()
  } catch (error) {
    return
  }
  
  try {
    submitting.value = true
    newsForm.submitType = 'SUBMIT'
    newsForm.imageIds = selectedImages.value
      .filter(img => img.id)
      .map(img => img.id!)
    
    if (isEditing.value) {
      const newsId = Number(route.params.id)
      // 如果是被驳回的新闻，使用resubmit API
      if (newsStatus.value === 'REJECTED') {
        await resubmitNewsApi(newsId, newsForm)
        ElMessage.success('新闻重新提交成功，等待审核')
      } else {
        // 否则使用update API
        await updateNews(newsId, newsForm)
        ElMessage.success('新闻提交成功，等待审核')
      }
    } else {
      // 创建并提交
      await submitNewsApi(newsForm)
      ElMessage.success('新闻提交成功，等待审核')
    }
    
    router.push('/editor/news-list')
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 返回
 */
function goBack() {
  router.push('/editor/news-list')
}
</script>

<style scoped>
.news-editor {
  padding: 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.back-btn {
  margin-right: 16px;
}

.page-header h1 {
  flex: 1;
  margin: 0;
  font-size: 24px;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.editor-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.image-upload-section {
  width: 100%;
}

.selected-images {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
}

.image-item {
  position: relative;
  width: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
}

.image-item img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.image-actions {
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f5f7fa;
}

.image-name {
  font-size: 12px;
  color: #666;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
