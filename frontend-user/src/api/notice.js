import request from '@/utils/request'

export const getNoticeList = (params) => request.get('/notice/list', { params })
export const getNoticeDetail = (id) => request.get(`/notice/detail/${id}`)
