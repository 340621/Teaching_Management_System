import request from '@/utils/request'

// 上传课程资料
export function uploadCourseMaterial(data) {
  return request({
    url: '/course/material/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取课程资料列表
export function getCourseMaterials(courseId) {
  return request({
    url: `/course/material/list/${courseId}`,
    method: 'get'
  })
}

// 删除课程资料
export function deleteCourseMaterial(id, uploaderId) {
  return request({
    url: `/course/material/delete/${id}`,
    method: 'delete',
    params: {
      uploaderId
    }
  })
}
