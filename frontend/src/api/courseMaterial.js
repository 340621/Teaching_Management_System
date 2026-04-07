import request from '@/utils/request'

// 上传课程资料
export function uploadCourseMaterial(data) {
  return request({
    url: '/course/materials/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取课程资料列表
export function getCourseMaterials(courseId, params) {
  return request({
    url: `/course/${courseId}/materials`,
    method: 'get',
    params
  })
}

// 删除课程资料
export function deleteCourseMaterial(id) {
  return request({
    url: `/course/materials/${id}`,
    method: 'delete'
  })
}
