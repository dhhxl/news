import request from '../utils/request'
import type { NewsReviewResponse, NewsReviewRequest } from './editor'

// 管理员审核相关接口

/**
 * 分配审核人
 */
export function assignReviewer(newsId: number, reviewerId: number) {
  return request.post('/api/review/assign', { newsId, reviewerId })
}

/**
 * 审核新闻
 */
export function reviewNews(data: NewsReviewRequest) {
  return request.post('/api/review/review', data)
}

/**
 * 获取待审核新闻列表
 */
export function getPendingReviews(params: { page?: number; size?: number }) {
  return request.get('/api/review/pending', { params })
}

/**
 * 获取正在审核的新闻列表
 */
export function getReviewingNews(params: { page?: number; size?: number }) {
  return request.get('/api/review/reviewing', { params })
}

/**
 * 获取所有需要审核的新闻
 */
export function getAllReviewableNews(params: { page?: number; size?: number }) {
  return request.get('/api/review/all', { params })
}

/**
 * 处理超时审核
 */
export function handleOverdueReviews() {
  return request.post('/api/review/handle-overdue')
}

/**
 * 清理未使用的图片
 */
export function cleanupUnusedImages(daysOld: number = 30) {
  return request.post('/api/images/cleanup', null, { params: { daysOld } })
}
