import request from '@/utils/request'

export const getAppList = (params) => request.get('/app/list', { params })
export const getAppDetail = (id) => request.get(`/app/detail/${id}`)
export const getHotApps = (limit = 6) => request.get('/app/hot', { params: { limit } })
export const getNewApps = (limit = 6) => request.get('/app/new', { params: { limit } })
export const searchApps = (params) => request.get('/app/search', { params })
export const downloadApp = (id) => request.post(`/download/${id}`)
