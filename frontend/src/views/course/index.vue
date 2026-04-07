<template>
  <div class="course-container">
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="hover">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="课程编号" prop="code">
          <el-input v-model="queryParams.code" placeholder="请输入课程编号" clearable />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="queryParams.name" placeholder="请输入课程名称" clearable />
        </el-form-item>
        <el-form-item label="开课院系" prop="departmentId">
          <el-select 
            v-model="queryParams.departmentId" 
            placeholder="请选择院系" 
            clearable
            style="min-width: 200px"
          >
            <el-option
              v-for="item in departmentList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程类型" prop="type">
          <el-select 
            v-model="queryParams.type" 
            placeholder="请选择类型" 
            clearable
            style="min-width: 150px"
          >
            <el-option label="必修" :value="0" />
            <el-option label="选修" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="header-title">课程信息列表</span>
          <div class="header-buttons">
            <el-button type="primary" @click="handleAdd" v-hasPerms="['course:course:add']">
              <el-icon><Plus /></el-icon>新增
            </el-button>
            <el-button 
              type="danger" 
              :disabled="selection.length === 0" 
              @click="handleBatchDelete"
              v-hasPerms="['course:course:delete']"
            >
              批量删除
            </el-button>
            <el-upload
              class="upload-demo"
              :show-file-list="false"
              :before-upload="handleImport"
              v-hasPerms="['course:course:import']"
            >
              <el-button type="primary" :loading="importLoading" plain>
                <el-icon><Upload /></el-icon>导入
              </el-button>
            </el-upload>
            <el-button 
              type="primary" 
              plain
              @click="handleExport"
              :loading="exportLoading"
              v-hasPerms="['course:course:export']"
            >
              <el-icon><Download /></el-icon>导出
            </el-button>
            <el-tooltip content="设置显示列">
              <el-button type="primary" plain @click="showColumnSettings = true">
                <el-icon><setting /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </template>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="courseList"
        stripe
        border
        @selection-change="handleSelectionChange"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="code" label="课程编号" width="120" />
        <el-table-column prop="name" label="课程名称" width="160" />
        <el-table-column prop="credit" label="学分" width="80" align="center" />
        <el-table-column prop="hours" label="学时" width="80" align="center" />
        <el-table-column label="课程类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.type === 0 ? 'danger' : 'success'">{{ scope.row.typeDesc }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="departmentName" label="开课院系" width="160" />
        <el-table-column prop="description" label="课程描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
        <el-table-column label="操作" min-width="220" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="handleUpdate(scope.row)" 
              v-hasPerms="['course:course:edit']"
            >
              编辑
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleViewDetail(scope.row)"
              v-hasPerms="['course:course:query']"
            >
              详情
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleViewStudents(scope.row)"
            >
              查看学生
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleUploadMaterials(scope.row)"
            >
              上传资料
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑课程对话框 -->
    <el-dialog
      :title="dialog.title"
      v-model="dialog.visible"
      width="600px"
      append-to-body
      destroy-on-close
    >
      <el-form
        ref="courseFormRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="课程编号" prop="code">
              <el-input 
                v-model="form.code" 
                placeholder="请输入课程编号" 
                :disabled="dialog.type === 'update'"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学分" prop="credit">
              <el-input-number 
                v-model="form.credit" 
                :min="0" 
                :max="20" 
                :precision="1" 
                :step="0.5"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学时" prop="hours">
              <el-input-number 
                v-model="form.hours" 
                :min="0" 
                :max="200" 
                :precision="0" 
                :step="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择课程类型" style="width: 100%">
                <el-option label="必修" :value="0" />
                <el-option label="选修" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开课院系" prop="departmentId">
              <el-select v-model="form.departmentId" placeholder="请选择院系" style="width: 100%">
                <el-option
                  v-for="item in departmentList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="课程描述" prop="description">
              <el-input 
                v-model="form.description" 
                type="textarea" 
                placeholder="请输入课程描述"
                :rows="4"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 列设置对话框 -->
    <el-dialog
      title="表格列设置"
      v-model="showColumnSettings"
      width="400px"
      append-to-body
    >
      <el-checkbox-group v-model="checkedColumns">
        <el-row :gutter="20">
          <el-col :span="12" v-for="item in allColumns" :key="item.prop">
            <el-checkbox :label="item.prop">{{ item.label }}</el-checkbox>
          </el-col>
        </el-row>
      </el-checkbox-group>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showColumnSettings = false">取消</el-button>
          <el-button type="primary" @click="applyColumnSettings">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看学生对话框 -->
    <el-dialog
      :title="`课程 ${selectedCourse?.name} 的学生列表`"
      v-model="showStudentDialog"
      width="800px"
      append-to-body
    >
      <!-- 搜索区域 -->
      <el-form :model="studentQueryParams" ref="studentQueryForm" :inline="true" class="mb-4">
        <el-form-item label="学生姓名" prop="name">
          <el-input v-model="studentQueryParams.name" placeholder="请输入学生姓名" clearable />
        </el-form-item>
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="studentQueryParams.studentId" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleStudentQuery">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetStudentQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 学生列表 -->
      <el-table
        v-loading="studentLoading"
        :data="studentList"
        stripe
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.gender === 0 ? 'info' : 'success'"> {{ scope.row.gender === 0 ? '男' : '女' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="className" label="班级" width="150" />
        <el-table-column prop="departmentName" label="院系" width="150" />
        <el-table-column prop="enrollTime" label="入学时间" width="140" align="center" />
      </el-table>

      <!-- 分页区域 -->
      <div class="pagination-container mt-4">
        <el-pagination
          v-model:current-page="studentQueryParams.pageNum"
          v-model:page-size="studentQueryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="studentTotal"
          @size-change="handleStudentSizeChange"
          @current-change="handleStudentCurrentChange"
        />
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showStudentDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 上传资料对话框 -->
    <el-dialog
      :title="`上传课程 ${selectedCourse?.name} 的资料`"
      v-model="showUploadDialog"
      width="700px"
      append-to-body
    >
      <!-- 上传表单 -->
      <el-form :model="uploadForm" ref="uploadFormRef" label-width="100px" class="mb-4">
        <el-form-item label="文件" prop="file">
          <el-upload
            class="upload-demo"
            action=""
            :on-change="handleFileChange"
            :auto-upload="false"
            :file-list="fileList"
            accept=".doc,.docx,.pdf,.ppt,.pptx,.xls,.xlsx,.txt,.zip,.rar"
            :limit="1"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon> 选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                请选择文件，支持 doc、docx、pdf、ppt、pptx、xls、xlsx、txt、zip、rar 格式
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            placeholder="请输入文件描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>

      <!-- 已上传资料列表 -->
      <div class="mt-4">
        <h4 class="mb-2">已上传的课程资料</h4>
        <el-table
          v-loading="materialsLoading"
          :data="materialsList"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="#" width="60" />
          <el-table-column prop="fileName" label="文件名" min-width="200" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="uploaderName" label="上传人" width="120" />
          <el-table-column prop="uploadTime" label="上传时间" width="180" />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button 
                type="primary" 
                link 
                @click="downloadMaterial(scope.row)"
                size="small"
              >
                下载
              </el-button>
              <el-button 
                type="danger" 
                link 
                @click="deleteMaterial(scope.row)"
                size="small"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" @click="submitUpload" :loading="uploadLoading">
            上传
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Plus,
  Upload,
  Download,
  ArrowDown,
  Setting
} from '@element-plus/icons-vue'
import { 
  listCourses, 
  getCourseDetail, 
  addCourse, 
  updateCourse, 
  deleteCourse,
  exportCourses,
  getCoursesByDepartment,
  getTeacherCourses
} from '@/api/course'
import { getCourseStudents } from '@/api/courseStudent'
import { uploadCourseMaterial, getCourseMaterials, deleteCourseMaterial } from '@/api/courseMaterial'
import { getAllDepartments } from '@/api/department'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 创建路由实例
const router = useRouter()

// 数据列表
const courseList = ref([])
const total = ref(0)
const loading = ref(false)
const importLoading = ref(false)
const exportLoading = ref(false)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  code: '',
  name: '',
  departmentId: null,
  type: null
})

// 院系列表
const departmentList = ref([])

// 获取院系列表
const getDepartmentList = async () => {
  try {
    const res = await getAllDepartments()
    if (res && res.data) {
      departmentList.value = res.data
    }
  } catch (err) {
    console.error('获取院系列表失败:', err)
    ElMessage.error('获取院系列表失败')
  }
}

// 表单相关
const courseFormRef = ref(null)
const queryForm = ref(null)
const dialog = reactive({
  visible: false,
  title: '',
  type: 'add'
})
const form = reactive({
  id: undefined,
  code: '',
  name: '',
  credit: 3,
  hours: 48,
  type: 0,
  departmentId: null,
  description: ''
})

// 表单验证规则
const rules = {
  code: [
    { required: true, message: '请输入课程编号', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9-]+$/, message: '课程编号只能包含字母、数字和连字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ],
  credit: [
    { required: true, message: '请输入学分', trigger: 'blur' }
  ],
  hours: [
    { required: true, message: '请输入学时', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择课程类型', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择开课院系', trigger: 'change' }
  ]
}

// 初始化数据
onMounted(async () => {
  await getDepartmentList()
  getList()
})

// 获取课程类型名称
const getTypeDesc = (type) => {
  return type === 0 ? '必修' : (type === 1 ? '选修' : '未知')
}

// 获取院系名称
const getDepartmentName = (departmentId) => {
  const department = departmentList.value.find(item => item.id === departmentId)
  return department ? department.name : ''
}

// 获取课程列表
const getList = async () => {
  loading.value = true
  try {
    const userStore = useUserStore()
    const userRole = userStore.roles[0] // 获取用户角色
    
    // 根据用户角色调用不同的接口
    let res
    if (userRole === 'teacher') {
      // 教师角色调用获取教师课程的接口
      res = await getTeacherCourses(queryParams)
    } else {
      // 管理员角色调用获取所有课程的接口
      res = await listCourses(queryParams)
    }
    
    if (res.data && res.data.list && Array.isArray(res.data.list)) {
      courseList.value = res.data.list.map(item => ({
        ...item,
        typeDesc: getTypeDesc(item.type),
        departmentName: item.departmentName || getDepartmentName(item.departmentId)
      }))
      total.value = res.data.total || 0
    } else {
      courseList.value = []
      total.value = 0
      console.error('返回数据格式异常:', res)
    }
  } catch (err) {
    console.error('获取课程列表失败:', err)
    ElMessage.error('获取课程列表失败')
    courseList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 查询处理
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryForm.value?.resetFields()
  handleQuery()
}

// 处理页码改变
const handleCurrentChange = (pageNum) => {
  queryParams.pageNum = pageNum
  getList()
}

// 处理每页条数改变
const handleSizeChange = (pageSize) => {
  queryParams.pageSize = pageSize
  getList()
}

// 新增按钮
const handleAdd = () => {
  resetForm()
  dialog.visible = true
  dialog.title = '新增课程'
  dialog.type = 'add'
  
  nextTick(() => {
    courseFormRef.value?.clearValidate()
  })
}

// 编辑按钮
const handleUpdate = (row) => {
  resetForm()
  dialog.visible = true
  dialog.title = '编辑课程'
  dialog.type = 'update'
  
  // 获取课程详情
  getCourseDetail(row.id)
    .then(res => {
      const data = res.data
      form.id = data.id
      form.code = data.code
      form.name = data.name
      form.credit = data.credit
      form.hours = data.hours
      form.type = data.type
      form.departmentId = data.departmentId
      form.description = data.description
    })
    .catch(err => {
      console.error('获取课程详情失败:', err)
      ElMessage.error('获取课程详情失败')
    })
  
  nextTick(() => {
    courseFormRef.value?.clearValidate()
  })
}

// 查看详情
const handleViewDetail = (row) => {
  ElMessageBox.alert(`
    <div style="text-align: left">
      <p><strong>课程编号：</strong>${row.code}</p>
      <p><strong>课程名称：</strong>${row.name}</p>
      <p><strong>学分：</strong>${row.credit}</p>
      <p><strong>学时：</strong>${row.hours}</p>
      <p><strong>课程类型：</strong>${row.typeDesc}</p>
      <p><strong>开课院系：</strong>${row.departmentName}</p>
      <p><strong>课程描述：</strong>${row.description || '无'}</p>
      <p><strong>创建时间：</strong>${row.createTime}</p>
    </div>
  `, '课程详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '确定'
  })
}

// 删除按钮
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除课程 ${row.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourse(row.id)
      ElMessage.success('删除成功')
      getList()
    } catch (err) {
      console.error('删除课程失败:', err)
      ElMessage.error('删除课程失败')
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!courseFormRef.value) return
  
  await courseFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        const saveFunc = dialog.type === 'add' ? addCourse : updateCourse
        await saveFunc(form)
        ElMessage.success(dialog.type === 'add' ? '新增成功' : '修改成功')
        dialog.visible = false
        getList()
      } catch (err) {
        console.error(dialog.type === 'add' ? '新增失败' : '修改失败', err)
        ElMessage.error(err.response?.data?.message || (dialog.type === 'add' ? '新增失败' : '修改失败'))
      }
    } else {
      console.log('表单校验不通过', fields)
    }
  })
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.code = ''
  form.name = ''
  form.credit = 3
  form.hours = 48
  form.type = 0
  form.departmentId = null
  form.description = ''
}

// 导入课程数据
const handleImport = (uploadFile) => {
  importLoading.value = true
  
  // 这里需要实现导入功能
  ElMessage.info('课程导入功能开发中')
  importLoading.value = false
  
  // 返回 false 阻止 Element Plus 的默认上传行为
  return false
}

// 导出课程数据
const handleExport = () => {
  exportLoading.value = true
  
  exportCourses(queryParams).then(res => {
    // 处理文件下载
    const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
    const fileName = `课程列表_${new Date().getTime()}.xlsx`
    
    if (navigator.msSaveBlob) {
      // IE
      navigator.msSaveBlob(blob, fileName)
    } else {
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = fileName
      link.click()
      URL.revokeObjectURL(link.href)
    }
    
    ElMessage.success('导出成功')
  }).catch(err => {
    console.error('导出失败', err)
    ElMessage.error('导出失败')
  }).finally(() => {
    exportLoading.value = false
  })
}



// 查看课程教师
const handleViewTeachers = (row) => {
  ElMessage.info(`将显示课程 ${row.name} 的教师列表，功能开发中`)
}

// 查看课程课表
const handleViewSchedule = (row) => {
  ElMessage.info(`将显示课程 ${row.name} 的课表信息，功能开发中`)
}

// 处理文件选择
const handleFileChange = (file, files) => {
  // 更新fileList
  fileList.value = files
  // 只保留最新选择的文件
  if (fileList.value.length > 1) {
    fileList.value.splice(0, fileList.value.length - 1)
  }
}

// 上传课程资料
const handleUploadMaterials = (row) => {
  selectedCourse.value = row
  showUploadDialog.value = true
  fileList.value = []
  uploadForm.description = ''
  // 获取课程资料列表
  getCourseMaterialsList()
}

// 获取课程资料列表
const getCourseMaterialsList = async () => {
  if (!selectedCourse.value) return
  
  materialsLoading.value = true
  try {
    const res = await getCourseMaterials(selectedCourse.value.id, {
      pageNum: 1,
      pageSize: 100
    })
    if (res.data && res.data.rows) {
      materialsList.value = res.data.rows
    } else {
      materialsList.value = []
    }
  } catch (err) {
    console.error('获取课程资料列表失败:', err)
    ElMessage.error('获取课程资料列表失败')
    materialsList.value = []
  } finally {
    materialsLoading.value = false
  }
}

// 下载课程资料
const downloadMaterial = (material) => {
  // 构建下载链接
  const downloadUrl = `/api/course/materials/${material.id}/download`
  window.open(downloadUrl, '_blank')
}

// 删除课程资料
const deleteMaterial = (material) => {
  ElMessageBox.confirm(`确定要删除资料 ${material.fileName} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCourseMaterial(material.id, useUserStore().userId)
      ElMessage.success('删除成功')
      // 重新获取资料列表
      getCourseMaterialsList()
    } catch (err) {
      console.error('删除资料失败:', err)
      ElMessage.error('删除资料失败')
    }
  }).catch(() => {})
}

const submitUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  if (!uploadForm.description?.trim()) {
    ElMessage.warning('请输入文件描述')
    return
  }

  uploadLoading.value = true
  try {
    const file = fileList.value[0].raw
    const formData = new FormData()

    // 🔥 严格按照后端参数顺序/类型 append
    formData.append('courseId', String(selectedCourse.value.id)) // 后端接收String，必须转字符串
    formData.append('name', file.name) // 传递原始文件名
    formData.append('description', uploadForm.description.trim()) // 传递文件描述
    formData.append('type', file.name.split('.').pop().toLowerCase()) // 自动提取文件后缀
    formData.append('file', file)

    const res = await uploadCourseMaterial(formData)
    if (res.code === 200) {
      ElMessage.success('文件上传成功')
      getCourseMaterialsList()
      fileList.value = []
      uploadForm.description = ''
    } else {
      ElMessage.error(`上传失败: ${res.message}`)
    }
  } catch (err) {
    console.error('上传异常:', err)
    ElMessage.error('文件上传失败，请检查后端日志')
  } finally {
    uploadLoading.value = false
  }
}

// 多选框选中数据
const selection = ref([])

// 处理表格选择变化
const handleSelectionChange = (val) => {
  selection.value = val
}

// 查看学生相关数据
const showStudentDialog = ref(false)
const selectedCourse = ref(null)
const studentList = ref([])
const studentTotal = ref(0)
const studentLoading = ref(false)
const studentQueryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  studentId: ''
})

// 上传资料相关数据
const showUploadDialog = ref(false)
const uploadForm = reactive({
  description: ''
})
const uploadFormRef = ref(null)
const fileList = ref([])
const uploadLoading = ref(false)

// 课程资料列表相关数据
const materialsList = ref([])
const materialsLoading = ref(false)

// 查看学生
const handleViewStudents = (row) => {
  selectedCourse.value = row
  showStudentDialog.value = true
  studentQueryParams.pageNum = 1
  getStudentList()
}

// 获取课程学生列表
const getStudentList = async () => {
  if (!selectedCourse.value) return
  
  studentLoading.value = true
  try {
    // 调用后端接口获取真实数据
    const res = await getCourseStudents(selectedCourse.value.id, studentQueryParams)
    
    if (res.data && res.data.records) {
      studentList.value = res.data.records
      studentTotal.value = res.data.total
    } else {
      studentList.value = []
      studentTotal.value = 0
    }
  } catch (err) {
    console.error('获取学生列表失败:', err)
    ElMessage.error('获取学生列表失败')
    studentList.value = []
    studentTotal.value = 0
  } finally {
    studentLoading.value = false
  }
}

// 学生查询处理
const handleStudentQuery = () => {
  studentQueryParams.pageNum = 1
  getStudentList()
}

// 重置学生查询
const resetStudentQuery = () => {
  studentQueryParams.name = ''
  studentQueryParams.studentId = ''
  handleStudentQuery()
}

// 处理学生页码改变
const handleStudentCurrentChange = (pageNum) => {
  studentQueryParams.pageNum = pageNum
  getStudentList()
}

// 处理学生每页条数改变
const handleStudentSizeChange = (pageSize) => {
  studentQueryParams.pageSize = pageSize
  getStudentList()
}

// 批量删除
const handleBatchDelete = () => {
  if (selection.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selection.value.length} 条课程记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 实际项目中可能需要调用批量删除接口
      const promises = selection.value.map(item => deleteCourse(item.id))
      await Promise.all(promises)
      
      ElMessage.success('批量删除成功')
      getList()
    } catch (err) {
      console.error('批量删除失败', err)
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 列设置
const showColumnSettings = ref(false)
const allColumns = [
  { prop: 'code', label: '课程编号' },
  { prop: 'name', label: '课程名称' },
  { prop: 'credit', label: '学分' },
  { prop: 'hours', label: '学时' },
  { prop: 'type', label: '课程类型' },
  { prop: 'departmentName', label: '开课院系' },
  { prop: 'description', label: '课程描述' },
  { prop: 'createTime', label: '创建时间' }
]
const checkedColumns = ref(['code', 'name', 'credit', 'hours', 'type', 'departmentName', 'description', 'createTime'])
const columnSettings = reactive({
  code: true,
  name: true,
  credit: true,
  hours: true,
  type: true,
  departmentName: true,
  description: true,
  createTime: true
})

// 应用列设置
const applyColumnSettings = () => {
  // 更新列设置
  for (const key in columnSettings) {
    columnSettings[key] = checkedColumns.value.includes(key)
  }
  showColumnSettings.value = false
}

// 权限检查函数
const hasPermission = (permission) => {
  // 动态获取用户权限
  const userStore = useUserStore()
  const permissions = userStore.permissions
  
  if (typeof permission === 'string') {
    return permissions.includes(permission)
  }
  
  if (permission instanceof Array && permission.length > 0) {
    return permissions.some(p => permission.includes(p))
  }
  
  return false
}
</script>

<style lang="scss" scoped>
.course-container {
  padding: 20px;
  
  .search-card {
    margin-bottom: 20px;
    
    .el-form {
      display: flex;
      flex-wrap: wrap;
      
      .el-form-item {
        margin-right: 15px;
        margin-bottom: 15px;
        min-width: 250px;
        
        &:last-child {
          margin-right: 0;
          min-width: auto;
        }
      }
    }
    
    :deep(.el-select) {
      width: 100%;
    }
    
    :deep(.el-input) {
      width: 100%;
    }
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-title {
        font-size: 16px;
        font-weight: 600;
      }
      
      .header-buttons {
        display: flex;
        gap: 10px;
      }
    }
    
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  // 表格样式优化
  :deep(.el-table) {
    width: 100%;
    overflow-x: auto;
    
    .el-table__header-wrapper,
    .el-table__body-wrapper {
      width: 100%;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.text-danger {
  color: #f56c6c;
}
</style> 