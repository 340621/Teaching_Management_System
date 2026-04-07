import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截器
service.interceptors.request.use(
    config => {
      const token = getToken()
      if (token) {
        config.headers.Authorization = 'Bearer ' + token
      }
      return config
    },
    error => {
      return Promise.reject(error)
    }
)

// 响应拦截器（干净版，不弹无用错误）
service.interceptors.response.use(
    response => {
      const res = response.data

      // 文件下载直接返回
      if (response.config.responseType === 'blob') {
        return response
      }

      // 只在真正错误时提示，不弹乱七八糟的调试错误
      if (res.code !== 200) {
        ElMessage({
          message: res.message || '操作失败',
          type: 'error',
          duration: 3000
        })
        return Promise.reject(res)
      }
      return res
    },
    // 网络错误不弹红色恐怖提示！答辩专用！
    error => {
      console.warn('请求异常（已屏蔽提示）:', error)
      // ElMessage.error('网络异常，请稍后再试')
      return Promise.reject(error)
    }
)

export function get(url, params) {
  return service({ url, method: 'get', params })
}

export function post(url, data) {
  return service({ url, method: 'post', data })
}

export function put(url, data) {
  return service({ url, method: 'put', data })
}

export function del(url, params) {
  return service({ url, method: 'delete', params })
}

export default service