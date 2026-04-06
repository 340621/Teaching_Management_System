import request from '@/utils/request'

// 获取选课时间配置
export function getSelectionSettings() {
  return request({
    url: '/api/system/config/selection',
    method: 'get'
  })
}

// 更新选课时间配置
export function updateSelectionSettings(data) {
  return request({
    url: '/api/system/config/selection',
    method: 'put',
    data
  })
}