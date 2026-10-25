import request from '@/utils/request'

export const getMyApps = (params) => request.get('/app/author/list', { params })
export const addApp = (data) => request.post('/app/add', data)
export const updateApp = (data) => request.put('/app/update', data)
export const deleteApp = (id) => request.delete(`/app/delete/${id}`)
export const getAppDetail = (id) => request.get(`/app/detail/${id}`)
