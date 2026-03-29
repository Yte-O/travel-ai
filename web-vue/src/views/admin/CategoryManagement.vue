<template>
  <div class="category-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">类别管理</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon> 添加类别
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="queryForm" inline @submit.prevent="handleSearch">
          <el-form-item label="类别名称">
            <el-input 
              v-model="queryForm.name" 
              placeholder="输入类别名称" 
              clearable
              @keyup.enter.prevent="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button @click="resetSearch" class="ml-4">
              <el-icon><RefreshRight /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        highlight-current-row
        style="width: 100%"
      >
        <el-table-column prop="name" label="类别名称" width="150" show-overflow-tooltip />
        <el-table-column label="类别图标" width="120" align="center">
          <template #default="scope">
            <div v-if="scope.row.iconUrl" class="icon-preview">
              <el-image
                :src="scope.row.iconUrl"
                style="width: 60px; height: 60px; object-fit: cover"
                :preview-src-list="[scope.row.iconUrl]"
                :initial-index="0"
                fit="cover"
                loading="lazy"
                class="icon-image"
                :hide-on-click-modal="false"
                preview-teleported
              />
            </div>
            <div v-else class="no-icon">
              <el-icon><Picture /></el-icon>
              <span>暂无图标</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="类别描述" min-width="100" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="400" align="center" fixed="right">
          <template #default="scope">
            <el-button type="info" size="small" @click="viewCategoryDetail(scope.row)" plain>
              <el-icon><View /></el-icon> 详情
            </el-button>
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)" plain>
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)" plain>
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空数据展示 -->
      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />

      <!-- 分页 -->
      <div class="pagination-container" v-if="pagination.itemCount > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.itemCount"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
        />
      </div>

      <!-- 添加/编辑对话框 -->
      <el-dialog 
        v-model="showDialog" 
        :title="dialogTitle" 
        width="600px"
        destroy-on-close
        @closed="resetForm"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          status-icon
        >
          <el-form-item label="类别名称" prop="name">
            <el-input 
              v-model="form.name" 
              placeholder="请输入类别名称" 
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="类别图标" prop="iconObjectKey">
            <div class="upload-container">
              <el-upload
                ref="uploadRef"
                class="icon-uploader"
                :http-request="customRequest"
                :show-file-list="false"
                accept="image/*"
                action="#"
                :auto-upload="true"
                :limit="1"
                drag
              >
                <div v-if="previewUrl" class="preview-wrapper">
                  <img :src="previewUrl" class="preview-image" />
                  <div class="preview-mask">
                    <el-icon class="preview-icon"><Edit /></el-icon>
                  </div>
                </div>
                <template v-else>
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-text">拖拽图片到此处或<em>点击上传</em></div>
                </template>
              </el-upload>
              <div class="upload-actions" v-if="previewUrl">
                <el-button type="danger" size="small" @click="handleRemoveIcon" plain>
                  <el-icon><Delete /></el-icon> 删除图标
                </el-button>
              </div>
              <div class="upload-tip">
                <el-text type="info" size="small">推荐尺寸: 200x200px，支持jpg、png、gif格式，不超过10MB</el-text>
                <br />
                <el-text type="info" size="small">类别图标为可选项</el-text>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="类别描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              placeholder="请输入类别描述"
              :rows="4"
              maxlength="255"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showDialog = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
              确认
            </el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 删除确认对话框 -->
      <el-dialog
        v-model="showDeleteDialog"
        title="删除确认"
        width="400px"
        destroy-on-close
      >
        <div class="delete-confirm-content">
          <el-icon class="warning-icon"><WarningFilled /></el-icon>
          <div class="delete-confirm-text">
            <p class="delete-title">确定要删除该类别吗？</p>
            <p class="delete-desc">类别名称: {{ deleteItem?.name }}</p>
            <p class="delete-warning">注意：如果该类别下有关联的景点，将无法删除。</p>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showDeleteDialog = false">取消</el-button>
            <el-button type="danger" @click="confirmDelete" :loading="deleteLoading">
              确认删除
            </el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>

    <!-- 全局图片查看器容器，确保预览组件能够正确显示 -->
    <div class="image-viewer-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadRequestOptions } from 'element-plus'
import { categoryApi } from '@/api/category'
import { fileRequest } from '@/api/file_request'
import type { CategoryVO, CategoryAddDTO, CategoryUpdateDTO, CategoryQueryDTO } from '@/types/item'
import { 
  Plus, 
  Edit, 
  Delete, 
  Search, 
  RefreshRight, 
  Picture,
  View, 
  WarningFilled,
  UploadFilled
} from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const deleteLoading = ref(false)
const tableData = ref<CategoryVO[]>([])
const showDialog = ref(false)
const showDeleteDialog = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance | null>(null)
const uploadRef = ref()
const previewUrl = ref('')
const deleteItem = ref<CategoryVO | null>(null)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

// 查询表单
const queryForm = reactive<CategoryQueryDTO>({
  name: '',
  current: 1,
  size: 10
})

// 表单
const form = reactive<CategoryAddDTO & { id?: number }>({
  name: '',
  iconObjectKey: '',
  iconBucket: '',
  description: ''
})

// 表单校验规则
const rules = {
  name: [
    { required: true, message: '请输入类别名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度应为2-50个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入类别描述', trigger: 'blur' },
    { max: 255, message: '长度不能超过255个字符', trigger: 'blur' }
  ]
}

// 获取类别列表
const fetchData = async () => {
  try {
    loading.value = true
    queryForm.current = pagination.page
    queryForm.size = pagination.pageSize
    const res = await categoryApi.page(queryForm)
    tableData.value = res.records || []
    pagination.itemCount = res.total || 0
  } catch (error) {
    console.error('获取类别列表失败', error)
    ElMessage.error('获取类别列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  queryForm.name = ''
  pagination.page = 1
  fetchData()
}

// 页码变化
const handlePageChange = (page: number) => {
  pagination.page = page
  fetchData()
}

// 页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchData()
}

// 打开添加对话框
const openAddDialog = () => {
  dialogType.value = 'add'
  resetForm()
  showDialog.value = true
}

// 打开编辑对话框
const openEditDialog = async (row: CategoryVO) => {
  dialogType.value = 'edit'
  resetForm()
  form.id = row.id
  form.name = row.name
  form.description = row.description
  previewUrl.value = row.iconUrl || ''
  showDialog.value = true
  
  // 获取完整信息
  try {
    const res = await categoryApi.getById(row.id)
    if (res) {
      form.iconObjectKey = res.iconObjectKey || ''
      form.iconBucket = res.iconBucket || ''
    }
  } catch (error) {
    console.error('获取类别详情失败', error)
    ElMessage.error('获取类别详情失败')
  }
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.iconObjectKey = ''
  form.iconBucket = ''
  form.description = ''
  previewUrl.value = ''
}

// 自定义上传请求
const customRequest = (options: UploadRequestOptions): Promise<any> => {
  if (!options.file) return Promise.reject(new Error('没有文件'))
  return new Promise((resolve, reject) => {
    uploadIcon(options.file as File)
      .then(res => {
        if (options.onSuccess) {
          options.onSuccess(res)
        }
        resolve(res)
      })
      .catch(err => {
        if (options.onError) {
          options.onError(err)
        }
        reject(err)
      })
  })
}

// 上传图标
const uploadIcon = async (file: File) => {
  try {
    submitLoading.value = true
    
    // 验证文件类型和大小
    const isImage = /\.(jpg|jpeg|png|gif)$/i.test(file.name)
    if (!isImage) {
      ElMessage.error('只能上传jpg、png、gif格式的图片')
      return Promise.reject(new Error('文件格式不正确'))
    }

    const isLt2M = file.size / 1024 / 1024 < 10
    if (!isLt2M) {
      ElMessage.error('图片大小不能超过2MB')
      return Promise.reject(new Error('文件大小超过限制'))
    }
    
    // 创建预览URL并上传
    previewUrl.value = URL.createObjectURL(file)
    
    // 使用fileRequest直接上传文件到文件服务器
    const bucketName = 'category-icon'
    const res = await fileRequest.upload(bucketName, file, false)
    
    if (res) {
      // 从fileRequest响应设置表单值
      form.iconObjectKey = res.objectKey || ''
      form.iconBucket = res.bucket || bucketName
      ElMessage.success('图标上传成功')
      return Promise.resolve(res)
    } else {
      ElMessage.warning('上传成功但返回数据格式有误')
      return Promise.reject(new Error('返回数据格式有误'))
    }
  } catch (error) {
    console.error('上传图标失败', error)
    ElMessage.error('上传图标失败')
    // 清除预览URL
    previewUrl.value = ''
    return Promise.reject(error)
  } finally {
    submitLoading.value = false
  }
}

// 提交表单
const handleSubmit = () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid, fields) => {
    if (!valid) {
      console.log('表单验证失败:', fields)
      return
    }
    
    try {
      submitLoading.value = true
      
      if (dialogType.value === 'add') {
        // 使用表单数据构建提交对象
        const categoryData = {
          name: form.name,
          description: form.description,
          // 如果没有上传图标，则这些字段为空字符串
          iconBucket: form.iconBucket || '',
          iconObjectKey: form.iconObjectKey || ''
        }
        
        await categoryApi.add(categoryData)
        ElMessage.success('添加类别成功')
      } else {
        const updateData: CategoryUpdateDTO = {
          id: form.id!,
          name: form.name,
          description: form.description,
          // 如果没有上传图标，则这些字段为空字符串
          iconObjectKey: form.iconObjectKey || '',
          iconBucket: form.iconBucket || ''
        }
        await categoryApi.update(updateData)
        ElMessage.success('更新类别成功')
      }
      
      showDialog.value = false
      fetchData()
    } catch (error) {
      console.error(dialogType.value === 'add' ? '添加类别失败' : '更新类别失败', error)
      ElMessage.error(dialogType.value === 'add' ? '添加类别失败' : '更新类别失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 打开删除确认对话框
const handleDelete = (row: CategoryVO) => {
  deleteItem.value = row
  showDeleteDialog.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!deleteItem.value) return
  
  try {
    deleteLoading.value = true
    await categoryApi.delete(deleteItem.value.id)
    ElMessage.success('删除类别成功')
    showDeleteDialog.value = false
    fetchData()
  } catch (error: any) {
    console.error('删除类别失败', error)
    
    // 根据错误信息给出更友好的提示
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error('删除类别失败，请稍后重试')
    }
  } finally {
    deleteLoading.value = false
  }
}

// 计算属性：对话框标题
const dialogTitle = computed(() => {
  return dialogType.value === 'add' ? '添加类别' : '编辑类别'
})

// 查看类别详情
const viewCategoryDetail = (category: CategoryVO) => {
  router.push({
    name: 'UserCategoryDetail',
    params: { id: category.id }
  })
}

// 删除图标
const handleRemoveIcon = async () => {
  try {
    if (form.iconBucket && form.iconObjectKey) {
      // 先删除实际文件
      await fileRequest.delete(form.iconBucket, form.iconObjectKey)
      ElMessage.success('图标文件已从服务器删除')
    }
    // 然后清空表单字段
    form.iconObjectKey = ''
    form.iconBucket = ''
    previewUrl.value = ''
    ElMessage.success('图标已移除')
  } catch (error) {
    console.error('删除图标文件失败', error)
    ElMessage.error('删除图标文件失败，但本地记录已清除')
    // 即使删除失败，也清空表单字段
    form.iconObjectKey = ''
    form.iconBucket = ''
    previewUrl.value = ''
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.category-management {
  width: 100%;
  position: relative;
  z-index: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.search-area {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.icon-preview {
  display: flex;
  justify-content: center;
  align-items: center;
}

.icon-image {
  border-radius: 4px;
  transition: transform 0.3s;
  cursor: pointer;
}

.icon-image:hover {
  transform: scale(1.05);
}

.no-icon {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #909399;
  font-size: 12px;
  margin: 0 auto;
}

.no-icon .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.upload-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.icon-uploader {
  width: 200px;
  height: 200px;
}

.icon-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 200px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.icon-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.icon-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0;
  border: none;
}

.preview-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.preview-wrapper:hover .preview-mask {
  opacity: 1;
}

.preview-icon {
  color: #fff;
  font-size: 28px;
}

.upload-icon {
  font-size: 32px;
  color: #c0c4cc;
  margin-bottom: 8px;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  text-align: center;
  padding: 0 20px;
}

.upload-text em {
  color: #409EFF;
  font-style: normal;
}

.upload-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-start;
}

.upload-tip {
  margin-top: 12px;
  line-height: 1.5;
  color: #909399;
}

.ml-4 {
  margin-left: 16px;
}

.delete-confirm-content {
  display: flex;
  align-items: flex-start;
  padding: 10px 0;
}

.warning-icon {
  font-size: 28px;
  color: #f56c6c;
  margin-right: 16px;
  margin-top: 4px;
}

.delete-confirm-text {
  flex: 1;
}

.delete-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
}

.delete-desc {
  margin-bottom: 8px;
  color: #606266;
}

.delete-warning {
  color: #f56c6c;
  font-size: 14px;
}

/* 图片预览组件样式修复 */
:deep(.el-image-viewer__mask) {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  opacity: 0.5;
  background: #000;
  z-index: 2010;
}

:deep(.el-image-viewer__wrapper) {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 2011;
}

:deep(.el-image-viewer__close) {
  z-index: 2012;
}

:deep(.el-image-viewer__canvas) {
  z-index: 2011;
}

:deep(.el-image-viewer__actions) {
  z-index: 2012;
}

:deep(.el-image-viewer__prev), 
:deep(.el-image-viewer__next) {
  z-index: 2012;
}

:deep(.el-image-viewer__btn) {
  z-index: 2012;
}

.image-viewer-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 0;
  height: 0;
  z-index: 2009;
  pointer-events: none;
}
</style> 