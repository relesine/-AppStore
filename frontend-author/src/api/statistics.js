import request from '@/utils/request'

export const getAuthorStats = () => request.get('/statistics/author')
