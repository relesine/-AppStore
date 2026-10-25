import request from '@/utils/request'

export const uploadIcon = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload/icon', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
export const uploadApk = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/file/upload/apk', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
