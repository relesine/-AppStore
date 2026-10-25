import request from '@/utils/request'

export const getComments = (appId) => request.get(`/comment/list/${appId}`)
export const addComment = (data) => request.post('/comment/add', data)
