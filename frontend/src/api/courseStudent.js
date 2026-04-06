import request from '@/utils/request'

// 获取课程的学生列表
export function getCourseStudents(courseId, queryParams) {
  return request({
    url: `/course/students/list/${courseId}`,
    method: 'get',
    params: queryParams
  })
}
