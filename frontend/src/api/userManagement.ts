import request from '../utils/request'

// 用户列表项接口
export interface UserListItem {
  id: number
  username: string
  email?: string
  fullName?: string
  phone?: string
  role: string
  avatarUrl?: string
  isEnabled: boolean
  createdAt: string
  lastLoginAt?: string
}

// 用户列表响应
export interface UserListResponse {
  users: UserListItem[]
  totalElements: number
  totalPages: number
  currentPage: number
  pageSize: number
}

// 用户统计信息
export interface UserStatistics {
  total: number
  admins: number
  editors: number
  users: number
}

/**
 * 获取用户列表
 */
export const getUserListApi = async (params: {
  page?: number
  size?: number
  keyword?: string
  role?: string
  isEnabled?: boolean
}): Promise<UserListResponse> => {
  const response = await request.get('/admin/users', { params })
  return response as UserListResponse
}

/**
 * 获取用户详情
 */
export const getUserDetailApi = async (userId: number) => {
  const response = await request.get(`/admin/users/${userId}`)
  return response
}

/**
 * 删除用户
 */
export const deleteUserApi = async (userId: number): Promise<void> => {
  await request.delete(`/admin/users/${userId}`)
}

/**
 * 禁用/启用用户
 */
export const toggleUserStatusApi = async (userId: number): Promise<UserListItem> => {
  const response = await request.put(`/admin/users/${userId}/toggle-status`)
  return response as UserListItem
}

/**
 * 重置用户密码
 */
export const resetPasswordApi = async (userId: number, newPassword: string): Promise<void> => {
  await request.post(`/admin/users/${userId}/reset-password`, { newPassword })
}

/**
 * 获取用户统计信息
 */
export const getUserStatisticsApi = async (): Promise<UserStatistics> => {
  const response = await request.get('/admin/users/statistics')
  return response as UserStatistics
}

