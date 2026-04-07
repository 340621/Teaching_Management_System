<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 登录卡片 -->
      <el-card class="login-card">
        <!-- 标题 -->
        <div class="title-container">
          <div class="logo">
            <i class="el-icon-school" style="font-size: 32px; color: #409EFF;"></i>
          </div>
          <h3 class="title">高校教学管理系统</h3>
          <p class="subtitle">Education Management System</p>
        </div>
        
        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          label-position="left"
        >
          <!-- 用户名 -->
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入学号/教师编号/用户名"
              prefix-icon="User"
              clearable
            />
          </el-form-item>
          
          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              :type="passwordType"
              prefix-icon="Lock"
              clearable
              @keyup.enter="handleLogin"
            >
              <template #suffix>
                <el-icon
                  class="cursor-pointer"
                  @click="togglePasswordVisible"
                >
                  <component :is="passwordType === 'password' ? 'View' : 'Hide'" />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <!-- 记住密码 -->
          <div class="remember-container">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <a class="forget-link">忘记密码?</a>
          </div>
          
          <!-- 登录按钮 -->
          <el-button
            :loading="loading"
            type="primary"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form>
        
      </el-card>
      
      <!-- 版权信息 -->
      <div class="copyright">
        © {{ new Date().getFullYear() }} 高校教学管理系统 | 版权所有
      </div>
    </div>
    
    <!-- 背景元素 -->
    <div class="bg-container">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
      <div class="bg-shape bg-shape-4"></div>
      <div class="bg-grid"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

// 路由信息
const router = useRouter()
const route = useRoute()

// 用户状态
const userStore = useUserStore()

// 登录表单
const loginFormRef = ref()
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 表单校验规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, message: '密码长度不能少于5个字符', trigger: 'blur' }
  ]
}

// 密码显示状态
const passwordType = ref('password')
const togglePasswordVisible = () => {
  passwordType.value = passwordType.value === 'password' ? 'text' : 'password'
}

// 加载状态
const loading = ref(false)

// 处理登录
const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    
    try {
      await userStore.login(loginForm)
      
      // 重定向或跳转到首页
      const redirect = route.query.redirect || '/dashboard'
      router.push({ path: redirect })
      
      ElMessage.success('登录成功')
    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.message || '登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  
  .login-box {
    position: relative;
    z-index: 2;
    width: 420px;
    
    .login-card {
      padding: 30px 40px;
      border-radius: 12px;
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
      }
      
      .title-container {
        margin-bottom: 35px;
        text-align: center;
        
        .logo {
          margin-bottom: 15px;
        }
        
        .title {
          margin: 0 0 5px 0;
          font-size: 24px;
          font-weight: 600;
          color: #303133;
        }
        
        .subtitle {
          margin: 0;
          font-size: 14px;
          color: #909399;
        }
      }
      
      .login-form {
        .el-form-item {
          margin-bottom: 20px;
          
          .el-input {
            height: 44px;
            border-radius: 8px;
            
            input {
              height: 44px;
              border-radius: 8px;
            }
          }
        }
        
        .remember-container {
          display: flex;
          justify-content: space-between;
          margin-bottom: 25px;
          font-size: 14px;
          
          .forget-link {
            color: #409EFF;
            cursor: pointer;
            
            &:hover {
              text-decoration: underline;
            }
          }
        }
        
        .login-button {
          width: 100%;
          height: 44px;
          font-weight: 600;
          border-radius: 8px;
          margin-bottom: 20px;
        }
      }
      
      .tips {
        margin-top: 20px;
        padding: 15px;
        background: #f5f7fa;
        border-radius: 8px;
        font-size: 13px;
        color: #606266;
        line-height: 1.5;
        
        p {
          margin: 5px 0;
        }
      }
    }
    
    .copyright {
      margin-top: 25px;
      text-align: center;
      color: #606266;
      font-size: 13px;
    }
  }
  
  .bg-container {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1;
    
    .bg-shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(64, 158, 255, 0.1);
      
      &.bg-shape-1 {
        width: 300px;
        height: 300px;
        top: -100px;
        left: -100px;
        animation: float 12s infinite ease-in-out;
      }
      
      &.bg-shape-2 {
        width: 200px;
        height: 200px;
        top: 20%;
        right: 10%;
        animation: float 10s infinite ease-in-out reverse;
      }
      
      &.bg-shape-3 {
        width: 150px;
        height: 150px;
        bottom: 10%;
        left: 15%;
        animation: float 8s infinite ease-in-out;
      }
      
      &.bg-shape-4 {
        width: 250px;
        height: 250px;
        bottom: -100px;
        right: -100px;
        animation: float 14s infinite ease-in-out reverse;
      }
    }
    
    .bg-grid {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-image: 
        linear-gradient(rgba(64, 158, 255, 0.05) 1px, transparent 1px),
        linear-gradient(90deg, rgba(64, 158, 255, 0.05) 1px, transparent 1px);
      background-size: 50px 50px;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  25% {
    transform: translateY(-20px) translateX(10px);
  }
  50% {
    transform: translateY(0) translateX(20px);
  }
  75% {
    transform: translateY(20px) translateX(10px);
  }
}
</style>