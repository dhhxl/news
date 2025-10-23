import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 60000, // 增加到60秒，避免超时
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
});

// 请求拦截器
service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 从localStorage获取token
    const token = localStorage.getItem(
      import.meta.env.VITE_TOKEN_KEY || 'news_management_token'
    );
    
    // 如果token存在，添加到请求头
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;
    
    // 如果响应状态码不是2xx，视为错误
    if (response.status < 200 || response.status >= 300) {
      ElMessage.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    
    return res;
  },
  (error) => {
    console.error('Response error:', error);
    
    if (error.response) {
      const { status, data, config } = error.response;
      
      // 特殊处理：获取摘要404不显示错误消息（摘要可能不存在）
      const isSummaryNotFound = status === 404 && config.url?.includes('/summaries/news/');
      
      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem(
            import.meta.env.VITE_TOKEN_KEY || 'news_management_token'
          );
          ElMessage.error(data?.message || '请先登录');
          window.location.href = '/login';
          break;
          
        case 403:
          ElMessage.error(data?.message || '没有权限访问');
          break;
          
        case 404:
          // 如果是摘要不存在，不显示错误消息
          if (!isSummaryNotFound) {
            ElMessage.error(data?.message || '请求的资源不存在');
          }
          break;
          
        case 500:
          ElMessage.error(data?.message || '服务器错误');
          break;
          
        default:
          ElMessage.error(data?.message || '请求失败');
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接');
    } else {
      ElMessage.error('请求配置错误');
    }
    
    return Promise.reject(error);
  }
);

export default service;

