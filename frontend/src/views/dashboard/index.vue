<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-info">
        <el-avatar :size="64" :src="avatar" class="user-avatar" />
        <div class="welcome-text">
          <h1>{{ welcomeText }}</h1>
          <p>欢迎来到高校智能教学管理平台</p>
        </div>
      </div>
      <div class="current-time">
        <div class="date">{{ currentDate }}</div>
        <div class="time">{{ currentTime }}</div>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :sm="12" :md="6" v-for="(card, index) in statsCards" :key="index" class="card-col">
        <el-card class="stats-card" shadow="hover">
          <div class="stat-icon" :style="{ background: card.color }">
            <el-icon><component :is="card.icon"></component></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">{{ card.title }}</div>
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-trend">
              <span :class="{'trend-up': card.increase, 'trend-down': !card.increase}">
                {{ card.increase ? '↑' : '↓' }} {{ card.compare }}
              </span>
              <span v-if="index === 3 && card.value === '0.0%'" class="refresh-action">
                <el-button size="small" link @click="refreshStatistics">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    

    
    <!-- 通知和待办 -->
    <el-row :gutter="20" class="notice-row">
      <el-col :sm="24" :md="12">
        <el-card class="recent-card">
          <template #header>
            <div class="card-header">
              <span>最近通知</span>
              <el-button class="button" :link="true" @click="viewMoreNotices">查看更多</el-button>
            </div>
          </template>
          <div class="notice-list">
            <div v-for="(notice, index) in recentNotices" :key="index" class="notice-item">
              <div class="notice-icon" :class="notice.type">
                <el-icon><component :is="noticeIcons[notice.type]"></component></el-icon>
              </div>
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-time">{{ notice.time }}</div>
              </div>
            </div>
            <el-empty v-if="recentNotices.length === 0" description="暂无通知" />
          </div>
        </el-card>
      </el-col>
      <el-col :sm="24" :md="12">
        <el-card class="recent-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button class="button" :link="true" @click="viewMoreTodos">查看更多</el-button>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="(todo, index) in todoList" :key="index" class="todo-item">
              <el-tag size="small" :type="getTagType(todo.priority)" class="todo-priority">{{ todo.priority }}</el-tag>
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-time">{{ todo.deadline }}</div>
              </div>
            </div>
            <el-empty v-if="todoList.length === 0" description="暂无待办事项" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import defaultAvatar from '@/assets/logo.png'
import { formatImageUrl } from '@/utils/image'
import { 
  getStatistics, 
  getTodoList, 
  getNoticeList
} from '@/api/dashboard'
import { ElMessage } from 'element-plus'
import { forceRefreshRoute, registerMessageRoutes } from '@/router'



// 用户信息
const userStore = useUserStore()
const currentDate = ref('')
const currentTime = ref('')
const avatar = computed(() => {
  const avatarUrl = userStore.avatar || defaultAvatar;
  return avatarUrl.startsWith('/src/') || avatarUrl.startsWith('/assets/') ? avatarUrl : formatImageUrl(avatarUrl);
})
const welcomeText = computed(() => {
  const hour = new Date().getHours()
  const username = userStore.name
  
  if (hour < 6) return `晚上好，${username}`
  if (hour < 12) return `早上好，${username}`
  if (hour < 14) return `中午好，${username}`
  if (hour < 18) return `下午好，${username}`
  return `晚上好，${username}`
})

// 定时器ID
let timerID = null

// 更新时间
const updateTime = () => {
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  currentDate.value = now.toLocaleDateString('zh-CN', options)
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
}

// 统计卡片数据
const statsCards = ref([
  { 
    title: '总学生数',
    value: '0',
    icon: 'User',
    color: '#409EFF',
    increase: true,
    compare: '0%'
  },
  { 
    title: '总课程数',
    value: '0',
    icon: 'Notebook',
    color: '#67C23A',
    increase: true,
    compare: '0%'
  },
  { 
    title: '教师数量',
    value: '0',
    icon: 'UserFilled',
    color: '#E6A23C',
    increase: false,
    compare: '0%'
  }
])



// 判断当前用户是否是学生
const isStudent = computed(() => {
  return userStore.roles && userStore.roles.includes('student')
})

// 通知类型图标
const noticeIcons = {
  'primary': 'Bell',
  'success': 'Document',
  'warning': 'Warning',
  'danger': 'CircleClose'
}

// 最近通知
const recentNotices = ref([])

// 待办事项
const todoList = ref([])

// 路由实例
const router = useRouter()

// 获取标签类型
const getTagType = (priority) => {
  switch(priority) {
    case '高': return 'danger'
    case '中': return 'warning'
    case '低': return 'info'
    default: return 'info'
  }
}

// 刷新统计数据
const refreshStatistics = async () => {
  try {
    const { data } = await getStatistics()
    if (data) {
      // 更新卡片数据
      statsCards.value[0].value = data.totalStudents.toString()
      statsCards.value[1].value = data.totalCourses.toString()
      statsCards.value[2].value = data.totalTeachers.toString()
      
      // 上周比例
      statsCards.value[0].compare = '+5%'  // 示例值
      statsCards.value[1].compare = '+2%'  // 示例值
      statsCards.value[2].compare = '-1%'  // 示例值
      
      // 增减标记
      statsCards.value[0].increase = true
      statsCards.value[1].increase = true
      statsCards.value[2].increase = false
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}



// 初始化数据
const initData = async () => {
  try {
    // 更新时间
    updateTime()
    
    // 获取统计数据
    await refreshStatistics()
    
    // 获取待办事项列表
    const todoRes = await getTodoList()
    todoList.value = todoRes.data || []
    
    // 获取通知列表
    const noticeRes = await getNoticeList()
    recentNotices.value = noticeRes.data || []
  } catch (error) {
    console.error('初始化数据失败:', error)
  }
}

// 挂载时初始化
onMounted(() => {
  // 设置时间更新定时器
  updateTime()
  timerID = setInterval(updateTime, 1000)
  
  // 初始化数据
  initData()
  
  // 确保消息相关路由已注册
  registerMessageRoutes()
})

// 卸载前清理
onBeforeUnmount(() => {
  if (timerID) {
    clearInterval(timerID)
  }
})

// 查看更多通知
const viewMoreNotices = () => {
  router.push('/notice')
}

// 查看更多待办事项
const viewMoreTodos = () => {
  router.push('/todo')
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  
  // 欢迎区域
  .welcome-section {
    background: linear-gradient(to right, #1890ff, #722ed1);
    color: white;
    padding: 24px;
    border-radius: 8px;
    display: flex;
    justify-content: space-between;
    margin-bottom: 24px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    
    .welcome-info {
      display: flex;
      align-items: center;
      
      .user-avatar {
        margin-right: 20px;
      }
      
      .welcome-text {
        h1 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
        }
        
        p {
          margin: 0;
          font-size: 16px;
          opacity: 0.9;
        }
      }
    }
    
    .current-time {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: flex-end;
      
      .date {
        font-size: 16px;
        margin-bottom: 8px;
      }
      
      .time {
        font-size: 24px;
        font-weight: 700;
      }
    }
  }
  
  // 统计卡片
  .stats-row {
    margin-bottom: 24px;
    
    .card-col {
      margin-bottom: 20px;
    }
    
    .stats-card {
      display: flex;
      height: 100%;
      
      :deep(.el-card__body) {
        display: flex;
        padding: 20px;
        width: 100%;
      }
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-right: 16px;
        
        .el-icon {
          font-size: 30px;
          color: white;
        }
      }
      
      .stat-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;
        
        .stat-title {
          font-size: 16px;
          color: #606266;
          margin-bottom: 8px;
        }
        
        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: #303133;
          margin-bottom: 8px;
        }
        
        .stat-trend {
          font-size: 14px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .trend-up {
            color: #67C23A;
          }
          
          .trend-down {
            color: #F56C6C;
          }
          
          .refresh-action {
            cursor: pointer;
            color: #409EFF;
          }
        }
      }
    }
  }
  

  
  // 通知和待办卡片
  .notice-row {
    .recent-card {
      margin-bottom: 20px;
      
      :deep(.el-card__header) {
        padding: 12px 20px;
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        span {
          font-size: 16px;
          font-weight: 500;
        }
      }
      
      // 通知列表
      .notice-list {
        .notice-item {
          display: flex;
          padding: 12px 0;
          border-bottom: 1px solid #ebeef5;
          
          &:last-child {
            border-bottom: none;
          }
          
          .notice-icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            justify-content: center;
            align-items: center;
            margin-right: 12px;
            
            .el-icon {
              font-size: 18px;
              color: white;
            }
            
            &.primary {
              background-color: #409EFF;
            }
            
            &.success {
              background-color: #67C23A;
            }
            
            &.warning {
              background-color: #E6A23C;
            }
            
            &.danger {
              background-color: #F56C6C;
            }
          }
          
          .notice-content {
            flex: 1;
            
            .notice-title {
              font-size: 14px;
              color: #303133;
              margin-bottom: 4px;
              display: -webkit-box;
              -webkit-line-clamp: 1;
              -webkit-box-orient: vertical;
              overflow: hidden;
            }
            
            .notice-time {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }
      
      // 待办列表
      .todo-list {
        .todo-item {
          display: flex;
          padding: 12px 0;
          border-bottom: 1px solid #ebeef5;
          
          &:last-child {
            border-bottom: none;
          }
          
          .todo-priority {
            margin-right: 12px;
            align-self: flex-start;
            margin-top: 3px;
          }
          
          .todo-content {
            flex: 1;
            
            .todo-title {
              font-size: 14px;
              color: #303133;
              margin-bottom: 4px;
              display: -webkit-box;
              -webkit-line-clamp: 1;
              -webkit-box-orient: vertical;
              overflow: hidden;
            }
            
            .todo-time {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }
    }
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .dashboard-container {
    .welcome-section {
      flex-direction: column;
      
      .welcome-info {
        margin-bottom: 16px;
      }
      
      .current-time {
        align-items: flex-start;
      }
    }
  }
}
</style> 