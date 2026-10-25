import request from '@/utils/request'

export const getCategories = () => request.get('/category/list')
export const getSubCategories = (parentId) => request.get(`/category/sub/${parentId}`)
