import request from '@/utils/request'

export const getVersionsByAppId = (appId, status) =>
  request.get(`/version/list/${appId}`, { params: { status } })
