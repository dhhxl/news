import request from '@/utils/request'

export interface Comment {
  id: number
  newsId: number
  userId: number
  username: string
  content: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface CommentCreateRequest {
  newsId: number
  content: string
}

/**
 * 创建评论
 */
export function createComment(data: CommentCreateRequest) {
  return request({
    url: '/comments',
    method: 'post',
    data
  })
}

/**
 * 获取新闻的评论列表
 */
export function getNewsComments(newsId: number) {
  return request<Comment[]>({
    url: `/comments/news/${newsId}`,
    method: 'get'
  })
}

/**
 * 删除评论
 */
export function deleteComment(commentId: number) {
  return request({
    url: `/comments/${commentId}`,
    method: 'delete'
  })
}

/**
 * 获取评论数量
 */
export function getCommentCount(newsId: number) {
  return request<{ count: number }>({
    url: `/comments/news/${newsId}/count`,
    method: 'get'
  })
}

