import request from '../utils/request'

// 用户个人资料接口
export interface UserProfile {
  id: number
  username: string
  email?: string
  fullName?: string
  phone?: string
  role: string
  avatarUrl?: string
  createdAt: string
  lastLoginAt?: string
  isEnabled: boolean
}

// 更新个人资料请求
export interface UpdateProfileRequest {
  fullName?: string
  email?: string
  phone?: string
}

// 修改密码请求
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

/**
 * 获取当前用户个人资料
 */
export const getUserProfileApi = async (): Promise<UserProfile> => {
  const response = await request.get('/profile')
  return response as UserProfile
}

/**
 * 更新个人资料
 */
export const updateProfileApi = async (data: UpdateProfileRequest): Promise<UserProfile> => {
  const response = await request.put('/profile', data)
  return response as UserProfile
}

/**
 * 上传头像
 */
export const uploadAvatarApi = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)
  
  const response = await request.post('/profile/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response as string
}

/**
 * 修改密码
 */
export const changePasswordApi = async (data: ChangePasswordRequest): Promise<void> => {
  await request.post('/profile/change-password', data)
}

