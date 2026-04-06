<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span><i class="el-icon-setting"></i> 系统配置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="选课时间配置" name="selection-time">
          <el-form :model="selectionTimeForm" :rules="rules" ref="selectionTimeFormRef" label-width="120px">
            <el-form-item label="当前学期" prop="currentSemester">
              <el-input v-model="selectionTimeForm.currentSemester" placeholder="请输入当前学期，格式：2024-2025-1" />
            </el-form-item>
            
            <el-form-item label="选课开始时间" prop="startTime">
              <el-date-picker
                v-model="selectionTimeForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="选课结束时间" prop="endTime">
              <el-date-picker
                v-model="selectionTimeForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="是否在选课时间内" prop="isSelectionTime">
              <el-switch v-model="selectionTimeForm.isSelectionTime" />
            </el-form-item>
            
            <el-form-item label="最大选课数" prop="maxCourses">
              <el-input-number v-model="selectionTimeForm.maxCourses" :min="1" :max="20" />
            </el-form-item>
            
            <el-form-item label="最大学分" prop="maxCredits">
              <el-input-number v-model="selectionTimeForm.maxCredits" :min="1" :max="50" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="submitSelectionTimeForm">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSelectionSettings, updateSelectionSettings } from '@/api/system/config'

export default {
  name: "SystemConfig",
  setup() {
    const activeTab = ref('selection-time')
    const selectionTimeFormRef = ref(null)
    
    const selectionTimeForm = reactive({
      currentSemester: '',
      startTime: null,
      endTime: null,
      isSelectionTime: false,
      maxCourses: 10,
      maxCredits: 30
    })
    
    const rules = {
      currentSemester: [
        { required: true, message: '请输入当前学期', trigger: 'blur' }
      ],
      startTime: [
        { required: true, message: '请选择选课开始时间', trigger: 'change' }
      ],
      endTime: [
        { required: true, message: '请选择选课结束时间', trigger: 'change' }
      ]
    }
    
    // 获取选课时间配置
    const getSelectionTimeSettings = () => {
      getSelectionSettings().then(response => {
        if (response.code === 200 && response.data) {
          const data = response.data
          selectionTimeForm.currentSemester = data.currentSemester || ''
          selectionTimeForm.startTime = data.startTime ? new Date(data.startTime) : null
          selectionTimeForm.endTime = data.endTime ? new Date(data.endTime) : null
          selectionTimeForm.isSelectionTime = data.isSelectionTime || false
          selectionTimeForm.maxCourses = data.maxCourses || 10
          selectionTimeForm.maxCredits = data.maxCredits || 30
        } else {
          ElMessage.error('获取配置失败')
        }
      }).catch(error => {
        console.error('获取配置失败:', error)
        ElMessage.error('获取配置失败')
      })
    }
    
    // 提交选课时间配置
    const submitSelectionTimeForm = () => {
      if (!selectionTimeFormRef.value) return
      
      selectionTimeFormRef.value.validate((valid) => {
        if (valid) {
          // 构建提交数据
          const submitData = {
            currentSemester: selectionTimeForm.currentSemester,
            startTime: selectionTimeForm.startTime ? selectionTimeForm.startTime.toISOString() : null,
            endTime: selectionTimeForm.endTime ? selectionTimeForm.endTime.toISOString() : null,
            isSelectionTime: selectionTimeForm.isSelectionTime,
            maxCourses: selectionTimeForm.maxCourses,
            maxCredits: selectionTimeForm.maxCredits
          }
          
          updateSelectionSettings(submitData).then(response => {
            if (response.code === 200) {
              ElMessage.success('保存成功')
            } else {
              ElMessage.error('保存失败')
            }
          }).catch(error => {
            console.error('保存配置失败:', error)
            ElMessage.error('保存失败')
          })
        } else {
          ElMessage.error('请检查表单数据')
        }
      })
    }
    
    onMounted(() => {
      getSelectionTimeSettings()
    })
    
    return {
      activeTab,
      selectionTimeFormRef,
      selectionTimeForm,
      rules,
      submitSelectionTimeForm
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>