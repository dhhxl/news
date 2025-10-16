import request from '@/utils/request';

/**
 * 登录请求参数
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string;
  refreshToken: string;
  username: string;
  role: string;
  userId: number;
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number;
  username: string;
  role: string;
}

/**
 * Token刷新响应
 */
export interface TokenRefreshResponse {
  token: string;
}

/**
 * 注册请求参数
 */
export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
}

/**
 * 用户登录
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  });
}

/**
 * 用户注册
 */
export function register(data: RegisterRequest): Promise<LoginResponse> {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  });
}

/**
 * 用户登出
 */
export function logout(): Promise<void> {
  return request({
    url: '/auth/logout',
    method: 'post'
  });
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser(): Promise<UserInfo> {
  return request({
    url: '/auth/me',
    method: 'get'
  });
}

/**
 * 刷新Token
 */
export function refreshToken(refreshToken: string): Promise<TokenRefreshResponse> {
  return request({
    url: '/auth/refresh',
    method: 'post',
    data: { refreshToken }
  });
}

