import request from '@/utils/request'

export const addFavorite = (appId) => request.post(`/favorite/add/${appId}`)
export const cancelFavorite = (appId) => request.delete(`/favorite/cancel/${appId}`)
export const checkFavorite = (appId) => request.get(`/favorite/check/${appId}`)
export const getMyFavorites = (params) => request.get('/favorite/my', { params })
