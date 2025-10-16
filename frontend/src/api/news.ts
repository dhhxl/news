import request from '../utils/request'

export interface News {
  id: number
  title: string
  content: string
  sourceWebsite: string
  originalUrl: string
  imageUrl?: string
  categoryId: number
  publishTime: string
  crawlTime?: string
  createdBy?: number
  status: string
  classificationMethod: string
  viewCount: number
  likeCount?: number
  commentCount?: number
  updatedAt: string
}

export interface Category {
  id: number
  name: string
  description?: string
  isDefault: boolean
  createdBy?: number
  createdAt: string
  updatedAt: string
}

export interface Summary {
  id: number
  newsId: number
  summaryContent: string
  generatedAt: string
  modelVersion?: string
  status: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 获取新闻列表
export function getNewsList(params: {
  page?: number
  size?: number
  categoryId?: number
}) {
  return request.get<PageResponse<News>>('/news', { params })
}

// 获取新闻详情
export function getNewsById(id: number) {
  return request.get<News>(`/news/${id}`)
}

// 搜索新闻
export function searchNews(params: {
  keyword: string
  page?: number
  size?: number
}) {
  return request.get<PageResponse<News>>('/news/search', { params })
}

// 获取热门新闻
export function getHotNews(params: { page?: number; size?: number }) {
  return request.get<PageResponse<News>>('/news/hot', { params })
}

// 获取最新新闻
export function getLatestNews(params: { page?: number; size?: number }) {
  return request.get<PageResponse<News>>('/news/latest', { params })
}

// 获取分类列表
export function getCategories() {
  return request.get<Category[]>('/categories')
}

// 创建新闻（管理员）
export function createNews(data: Partial<News>) {
  return request.post<News>('/news', data)
}

// 更新新闻（管理员）
export function updateNews(id: number, data: Partial<News>) {
  return request.put<News>(`/news/${id}`, data)
}

// 删除新闻（管理员）
export function deleteNews(id: number) {
  return request.delete(`/news/${id}`)
}

// 获取新闻摘要
export function getSummary(newsId: number) {
  return request.get<Summary>(`/summaries/news/${newsId}`)
}

// 生成摘要（管理员）
export function generateSummary(newsId: number) {
  return request.post<Summary>(`/summaries/generate/${newsId}`)
}

// 批量生成摘要（管理员）
export function batchGenerateSummaries() {
  return request.post(`/summaries/generate/batch`)
}

// 获取新闻统计（管理员）
export function getNewsStatistics() {
  return request.get('/news/statistics')
}

