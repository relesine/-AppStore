import request from '@/utils/request'

export const getVersions = (appId) => request.get(`/version/list/${appId}`)
export const addVersion = (data) => request.post('/version/add', data)
export const deleteVersion = (id) => request.delete(`/version/delete/${id}`)
