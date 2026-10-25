import request from '@/utils/request'

export const getAuditList = (params) => request.get('/app/audit/list', { params })
export const auditApp = (data) => request.post('/audit/app', data)
export const auditVersion = (data) => request.post('/audit/version', data)
export const getAppDetail = (id) => request.get(`/app/detail/${id}`)
