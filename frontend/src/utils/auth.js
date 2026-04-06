import Cookies from 'js-cookie'

const TokenKey = 'Student-Management-Token'

/**
 * 获取token
 * @returns {String}
 */
export function getToken() {
  // 先从 Cookie 取
  let token = Cookies.get(TokenKey)
  // 取不到再从 localStorage 取
  if (!token) {
    token = localStorage.getItem(TokenKey)
  }
  return token
}
/**
 * 设置token
 * @param {String} token
 * @returns {String}
 */
export function setToken(token) {
  localStorage.setItem(TokenKey, token)
  return Cookies.set(TokenKey, token)
}

/**
 * 移除token
 * @returns {void}
 */
export function removeToken() {
  localStorage.removeItem(TokenKey)
  return Cookies.remove(TokenKey)
} 

