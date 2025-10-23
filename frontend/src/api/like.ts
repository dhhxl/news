import request from '@/utils/request'

/**
 * 点赞新闻
 */
export function likeNews(newsId: number) {
  return request({
    url: `/likes/news/${newsId}`,
    method: 'post'
  })
}

/**
 * 取消点赞
 */
export function unlikeNews(newsId: number) {
  return request({
    url: `/likes/news/${newsId}`,
    method: 'delete'
  })
}

/**
 * 检查点赞状态
 */
export function checkLikeStatus(newsId: number) {
  return request<{ liked: boolean }>({
    url: `/likes/news/${newsId}/status`,
    method: 'get'
  })
}

/**
 * 获取点赞数
 */
export function getLikeCount(newsId: number) {
  return request<{ count: number }>({
    url: `/likes/news/${newsId}/count`,
    method: 'get'
  })
}

