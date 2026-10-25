import request from '@/utils/request'

export const getMyDownloads = (params) => request.get('/download/my', { params })
