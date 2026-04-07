import request from '@/utils/request'

/**
 * 获取所有教师列表
 * @returns 教师列表
 */
export function getAllTeachers() {
  return request({
    url: '/teacher/all',
    method: 'get'
  })
}

/**
 * 获取教师列表
 * @param {Object} params 查询参数
 * @returns 教师列表
 */
export function getTeacherList(params) {
  return request({
    url: '/teacher/list',
    method: 'get',
    params
  })
}

/**
 * 获取教师详情
 * @param {number} id 教师ID
 * @returns 教师详情
 */
export function getTeacherDetail(id) {
  return request({
    url: `/teacher/${id}`,
    method: 'get'
  })
}

/**
 * 新增教师
 * @param {Object} data 教师数据
 * @returns 操作结果
 */
export function addTeacher(data) {
  return request({
    url: '/teacher',
    method: 'post',
    data
  })
}

/**
 * 更新教师
 * @param {Object} data 教师数据
 * @returns 操作结果
 */
export function updateTeacher(data) {
  return request({
    url: '/teacher',
    method: 'put',
    data
  })
}

/**
 * 删除教师
 * @param {number} id 教师ID
 * @returns 操作结果
 */
export function deleteTeacher(id) {
  return request({
    url: `/teacher/${id}`,
    method: 'delete'
  })
}

/**
 * 重置教师密码
 * @param {number} id 教师ID
 * @returns 操作结果
 */
export function resetTeacherPassword(id) {
  return request({
    url: `/teacher/${id}/reset-password`,
    method: 'put'
  })
}

/**
 * 导入教师数据
 * @param {File} file 导入文件
 * @returns 操作结果
 */
export function importTeacher(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/teacher/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导出教师数据
 * @param {Object} params 查询参数
 * @returns 文件流
 */
export function exportTeacher(params) {
  return request({
    url: '/teacher/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 下载教师导入模板
 * @returns 文件流
 */
export function downloadTeacherTemplate() {
  return request({
    url: '/teacher/template',
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 更新教师状态
 * @param {number} id 教师ID
 * @param {number} status 状态
 * @returns 操作结果
 */
export function updateTeacherStatus(id, status) {
  return request({
    url: `/teacher/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 获取教师课程
 * @param {number} id 教师ID
 * @returns 课程列表
 */
export function getTeacherCourses(id) {
  return request({
    url: `/teacher/${id}/courses`,
    method: 'get'
  })
}

/**
 * 获取教师课表
 * @param {number} id 教师ID
 * @returns 课表信息
 */
export function getTeacherSchedule(id) {
  return request({
    url: `/teacher/${id}/schedule`,
    method: 'get'
  })
}

/**
 * 获取教师评价
 * @param {number} id 教师ID
 * @returns 评价列表
 */
export function getTeacherEvaluations(id) {
  return request({
    url: `/teacher/${id}/evaluations`,
    method: 'get'
  })
}

/**
 * 获取教师课程学生列表
 * @param {number} teacherId 教师ID
 * @param {number} courseId 课程ID
 * @param {Object} params 查询参数
 * @returns 学生列表
 */
export function getTeacherCourseStudents(teacherId, courseId, params) {
  return request({
    url: `/teacher/courses/${teacherId}/students/${courseId}`,
    method: 'post',
    data: params
  })
}

/**
 * 提交学生成绩
 * @param {number} teacherId 教师ID
 * @param {number} courseId 课程ID
 * @param {Object} data 成绩数据
 * @returns 操作结果
 */
export function submitStudentGrade(teacherId, courseId, data) {
  return request({
    url: `/teacher/courses/${teacherId}/grade/${courseId}`,
    method: 'put',
    data: data
  })
}
