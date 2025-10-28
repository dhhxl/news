import request from '../utils/request'

// 编辑新闻相关接口

export interface NewsSubmitRequest {
  title: string
  content: string
  categoryId: number
  originalUrl?: string
  imageIds?: number[]
  submitType: 'DRAFT' | 'SUBMIT'
  submitNote?: string
}

export interface UploadedImage {
  id: number
  originalName: string
  storedName: string
  filePath: string
  fileSize: number
  mimeType: string
  uploadedBy: number
  newsId?: number
  uploadTime: string
  isUsed: boolean
  accessUrl: string
  humanReadableSize: string
}

export interface NewsReviewResponse {
  newsId: number
  title: string
  content: string
  status: string
  categoryId: number
  categoryName?: string
  submittedAt?: string
  submittedBy?: number
  submittedByUsername?: string
  currentReviewer?: number
  currentReviewerUsername?: string
  reviewDeadline?: string
  isOverdue?: boolean
  imageUrl?: string
  imageUrls?: string[]
  imageCount?: number
  reviewHistory?: ReviewHistoryItem[]
  viewCount: number
  likeCount?: number
  commentCount?: number
  createdAt: string
  updatedAt: string
}

export interface ReviewHistoryItem {
  action: string
  status: string
  reviewComment?: string
  reviewedAt: string
  reviewerUsername?: string
}

export interface NewsReviewRequest {
  newsId: number
  action: 'APPROVE' | 'REJECT' | 'REQUEST_CHANGES'
  reviewComment?: string
  reviewDeadline?: string
}

// ============ 编辑新闻相关接口 ============

/**
 * 创建新闻草稿
 */
export function createDraft(data: NewsSubmitRequest) {
  return request.post('/api/editor/news/draft', data)
}

/**
 * 提交新闻
 */
export function submitNews(data: NewsSubmitRequest) {
  return request.post('/api/editor/news/submit', data)
}

/**
 * 更新新闻
 */
export function updateNews(newsId: number, data: NewsSubmitRequest) {
  return request.put(`/api/editor/news/${newsId}`, data)
}

/**
 * 重新提交被退回的新闻
 */
export function resubmitNews(newsId: number, data: NewsSubmitRequest) {
  return request.post(`/api/editor/news/${newsId}/resubmit`, data)
}

/**
 * 取消审核
 */
export function cancelReview(newsId: number) {
  return request.post(`/api/editor/news/${newsId}/cancel-review`)
}

/**
 * 获取我的新闻列表
 */
export function getMyNews(params: { page?: number; size?: number }) {
  return request.get('/api/editor/news/my-news', { params })
}

/**
 * 根据状态获取我的新闻
 */
export function getMyNewsByStatus(
  status: string, 
  params: { page?: number; size?: number }
) {
  return request.get(`/api/editor/news/my-news/status/${status}`, { params })
}

/**
 * 获取新闻详情（编辑视图）
 */
export function getNewsForEdit(newsId: number) {
  return request.get(`/api/editor/news/${newsId}`)
}

/**
 * 获取新闻关联的图片
 */
export function getNewsImages(newsId: number) {
  return request.get(`/api/editor/news/${newsId}/images`)
}

/**
 * 删除草稿
 */
export function deleteDraft(newsId: number) {
  return request.delete(`/api/editor/news/${newsId}`)
}

// ============ 图片上传相关接口 ============

/**
 * 上传单张图片
 */
export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/images/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传图片
 */
export function uploadImages(files: File[]) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return request.post('/images/upload/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取我的图片列表
 */
export function getMyImages(params: { page?: number; size?: number }) {
  return request.get('/images/my-images', { params })
}

/**
 * 获取未使用的图片
 */
export function getUnusedImages() {
  return request.get('/images/unused')
}

/**
 * 获取图片详情
 */
export function getImageDetails(imageId: number) {
  return request.get(`/api/images/${imageId}`)
}

/**
 * 删除图片
 */
export function deleteImage(imageId: number) {
  return request.delete(`/api/images/${imageId}`)
}

/**
 * 关联图片到新闻
 */
export function associateImagesToNews(imageIds: number[], newsId: number) {
  return request.post('/images/associate-to-news', { imageIds, newsId })
}

// ============ 新闻审核相关接口 ============

/**
 * 提交新闻审核
 */
export function submitForReview(newsId: number) {
  return request.post(`/api/review/submit/${newsId}`)
}

/**
 * 获取我的提交记录
 */
export function getMySubmissions(params: { 
  page?: number; 
  size?: number; 
  status?: string 
}) {
  return request.get('/api/review/my-submissions', { params })
}

/**
 * 获取新闻审核历史
 */
export function getReviewHistory(newsId: number) {
  return request.get(`/api/review/${newsId}/history`)
}

// ============ 统计数据相关接口 ============

export interface EditorStats {
  draftCount: number
  pendingCount: number
  reviewingCount: number
  publishedCount: number
  rejectedCount: number
  totalViews: number
}

/**
 * 获取编辑员统计数据
 */
export function getEditorStats() {
  return request.get<EditorStats>('/api/editor/news/statistics')
}

/**
 * 获取编辑员最近动态
 */
export function getEditorRecentNews(params: { page: number; size: number }) {
  return request.get('/api/editor/news/recent', { params })
}
