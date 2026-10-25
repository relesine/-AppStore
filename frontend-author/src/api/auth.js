import request from '@/utils/request'

// 登录接口
export const login = (data) => request.post('/auth/login', data)
// 注册接口
export const register = (data) => request.post('/auth/register', data)
// 获取当前登录用户信息
export const getUserInfo = () => request.get('/auth/info')
// 更新当前登录用户个人资料（昵称、邮箱、手机、头像）
export const updateProfile = (data) => request.put('/auth/profile', data)
// 修改当前登录用户密码
export const changePassword = (data) => request.put('/auth/password', data)
