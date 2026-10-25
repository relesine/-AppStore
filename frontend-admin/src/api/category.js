import request from '@/utils/request'

export const getCategories = () => request.get('/category/list')
export const addCategory = (data) => request.post('/category/add', data)
export const updateCategory = (data) => request.put('/category/update', data)
export const deleteCategory = (id) => request.delete(`/category/delete/${id}`)
