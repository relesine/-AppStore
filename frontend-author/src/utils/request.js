import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({ baseURL: '/api', timeout: 30000 })

// 请求拦截器：自动携带token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('author_token')
    if (token) config.headers['Authorization'] = 'Bearer ' + token
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器：统一处理业务码与HTTP异常
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) return res
    // 401：登录过期，清理本地状态并跳转登录页
    if (res.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('author_token')
      localStorage.removeItem('author_info')
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    // 403：无权限
    if (res.code === 403) {
      ElMessage.error(res.message || '没有权限执行该操作')
      return Promise.reject(new Error(res.message))
    }
    // 账号被禁用：清理本地状态并跳转登录页
    if (res.code === 1004) {
      ElMessage.error(res.message || '账号已被禁用')
      localStorage.removeItem('author_token')
      localStorage.removeItem('author_info')
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('author_token')
        localStorage.removeItem('author_info')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有权限访问该资源')
      } else if (status >= 500) {
        ElMessage.error('服务器开小差了，请稍后重试')
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络后重试')
    } else {
      ElMessage.error('网络异常，请检查后端服务是否已启动')
    }
    return Promise.reject(error)
  },
)

export default request
