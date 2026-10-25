import request from '@/utils/request'

export const getVersions = (appId) => request.get(`/version/list/${appId}`)
export const deleteVersion = (id) => request.delete(`/version/delete/${id}`)
