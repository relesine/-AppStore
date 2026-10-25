import request from '@/utils/request'

export const getNoticeList = (params) => request.get('/notice/list', { params })
export const getNoticeDetail = (id) => request.get(`/notice/detail/${id}`)
export const addNotice = (data) => request.post('/notice/add', data)
export const updateNotice = (data) => request.put('/notice/update', data)
export const deleteNotice = (id) => request.delete(`/notice/delete/${id}`)
