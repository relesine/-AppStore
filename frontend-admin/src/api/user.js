import request from '@/utils/request'

export const getUserList = (params) => request.get('/user/list', { params })
export const updateUser = (data) => request.put('/user/update', data)
export const deleteUser = (id) => request.delete(`/user/delete/${id}`)
export const updateStatus = (data) => request.put('/user/status', data)
