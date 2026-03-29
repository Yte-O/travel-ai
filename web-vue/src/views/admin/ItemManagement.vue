<template>
  <div class="item-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>景点管理</span>
          <el-button type="primary" @click="openAddDialog">
            添加景点
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="queryForm" inline>
          <el-form-item label="景点名称">
            <el-input v-model="queryForm.title" placeholder="输入景点名称" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="所属类别">
            <el-select
              v-model="queryForm.categoryId"
              placeholder="请选择类别"
              clearable
              :options="categoryOptions"
              @keyup.enter="handleSearch"
              style="width: 150px;"
            >
              <el-option 
                v-for="item in categoryOptions" 
                :key="item.value" 
                :label="item.label" 
                :value="item.value" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="queryForm.tags" placeholder="输入标签关键词，多个标签用英文逗号分隔" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="发布者">
            <el-input v-model="queryForm.userRealName" placeholder="输入发布者姓名" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              搜索
            </el-button>
            <el-button class="ml-4" @click="resetSearch">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
      >
        <el-table-column prop="title" label="景点名称" width="150" />
        <el-table-column label="景点封面" width="150">
          <template #default="scope">
            <div v-if="scope.row.coverUrl" class="cover-preview">
              <el-image
                :src="scope.row.coverUrl"
                style="width: 60px; height: 60px; object-fit: cover"
                :preview-src-list="[scope.row.coverUrl]"
                :initial-index="0"
                fit="cover"
                class="cover-image"
                :hide-on-click-modal="false"
                preview-teleported
              />
            </div>
            <div v-else class="no-cover">
              <el-icon><Picture /></el-icon>
              <span>暂无封面</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属类别" width="150">
          <template #default="scope">
            {{ scope.row.category ? scope.row.category.name : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="120">
          <template #default="scope">
            {{ scope.row.userRealName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="标签" width="220">
          <template #default="scope">
            <div class="tags-container">
              <el-tag
                v-for="(tag, index) in scope.row.tagList"
                :key="index"
                type="success"
                effect="light"
                round
                class="item-tag"
              >
                {{ tag }}
              </el-tag>
              <span v-if="!scope.row.tagList || scope.row.tagList.length === 0" class="no-tags">暂无标签</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="景点描述" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button type="info" size="small" @click="viewAttractionDetail(scope.row)">详情</el-button>
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.itemCount"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <!-- 添加/编辑对话框 -->
      <el-dialog
        v-model="showDialog"
        :title="dialogType === 'add' ? '添加景点' : '编辑景点'"
        width="600px"
        :before-close="cancelDialog"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="item-form"
        >
          <!-- 景点名称 -->
          <el-form-item label="景点名称" prop="title">
            <el-input v-model="form.title" placeholder="请输入景点名称" />
          </el-form-item>

          <!-- 所属类别 -->
          <el-form-item label="所属类别" prop="categoryId">
            <el-select v-model="form.categoryId" placeholder="请选择所属类别" class="w-full">
              <el-option
                v-for="item in categoryOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>

          <!-- 景点描述 -->
          <el-form-item label="景点描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入景点描述"
            />
          </el-form-item>

          <!-- 标签 -->
          <el-form-item label="标签" prop="tags">
            <el-input v-model="form.tags" placeholder="请输入标签，使用逗号分隔" />
          </el-form-item>
          
          <!-- 景点信息字段 -->
          <el-divider content-position="left">
            <span style="color: #409eff; font-weight: bold;">景点信息</span>
          </el-divider>

          <el-form-item label="门票价格" prop="ticketPrice">
            <el-input v-model="form.ticketPrice" placeholder="如：248元（可选）" />
          </el-form-item>

          <el-form-item label="开放时间" prop="openTime">
            <el-input v-model="form.openTime" placeholder="如：07:00-18:00（可选）" />
          </el-form-item>

          <el-form-item label="景点地址" prop="address">
            <el-input v-model="form.address" placeholder="请输入景点详细地址（可选）" />
          </el-form-item>

          <!-- 地图位置选择 -->
          <el-form-item label="地理位置">
            <MapPicker 
              v-model="locationData"
              height="350px"
            />
          </el-form-item>

          <el-form-item label="交通信息" prop="traffic">
            <el-input
              v-model="form.traffic"
              type="textarea"
              :rows="2"
              placeholder="如：飞机：XX机场；火车：XX火车站；公交：XX路线（可选）"
            />
          </el-form-item>

          <el-form-item label="最佳季节" prop="bestSeason">
            <el-input v-model="form.bestSeason" placeholder="如：春秋两季（可选）" />
          </el-form-item>

          <el-form-item label="游玩时间" prop="playTime">
            <el-input v-model="form.playTime" placeholder="如：2-3天（可选）" />
          </el-form-item>

          <el-form-item label="景点特色" prop="features">
            <el-input
              v-model="form.features"
              type="textarea"
              :rows="2"
              placeholder="请用逗号分隔多个特色，如：奇峰异石, 云海奇观, 玻璃栈道（可选）"
            />
          </el-form-item>

          <!-- 上传封面 -->
          <el-form-item label="封面">
            <el-upload
              :http-request="customCoverRequest"
              class="upload-component"
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              accept="image/*"
            >
              <div v-if="coverPreviewUrl" class="preview-container">
                <div class="preview-image">
                  <img :src="coverPreviewUrl" alt="封面预览" />
                </div>
                <div class="preview-actions">
                  <el-button
                    type="primary"
                    size="small"
                    @click.stop="previewCover"
                  >
                    预览
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click.stop="removeUploadedCover"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              <el-button v-else type="primary">上传封面</el-button>
            </el-upload>
          </el-form-item>

          <!-- 上传文件 -->
          <el-form-item label="文件">
            <el-upload
              :http-request="customFileRequest"
              class="upload-component"
              :show-file-list="false"
              :before-upload="beforeFileUpload"
            >
              <div v-if="uploadedFileName" class="uploaded-file">
                <el-icon><document /></el-icon>
                <span class="ml-2">{{ uploadedFileName }}</span>
                <div class="file-actions">
                  <el-button
                    type="danger"
                    size="small"
                    @click.stop="removeUploadedFile"
                  >
                    删除
                  </el-button>
                </div>
              </div>
              <el-button v-else type="primary">上传文件</el-button>
            </el-upload>
          </el-form-item>
        </el-form>

        <template #footer>
          <span class="dialog-footer">
            <el-button @click="cancelDialog">取消</el-button>
            <el-button type="primary" @click="submitForm">确定</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadRequestOptions } from 'element-plus'
import { itemApi } from '@/api/item'
import { categoryApi } from '@/api/category'
import { fileRequest } from '@/api/file_request'
import type { ItemVO, ItemAddDTO, ItemUpdateDTO, CategoryVO } from '@/types/item'
import { Picture, Document } from '@element-plus/icons-vue'
import MapPicker from '@/components/MapPicker.vue'

const router = useRouter()

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<ItemVO[]>([])
const showDialog = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance | null>(null)
const coverPreviewUrl = ref('')
const categories = ref<CategoryVO[]>([])
const uploadedFileName = ref('')

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

// 查询表单
const queryForm = ref({
  title: '',
  categoryId: undefined,
  tags: '',
  userRealName: '',
  current: 1,
  size: 10
})

// 表单
const form = reactive<ItemAddDTO & { 
  id?: number;
  // 标准extraData字段
  ticketPrice?: string;
  openTime?: string;
  address?: string;
  traffic?: string;
  bestSeason?: string;
  playTime?: string;
  features?: string;
  latitude?: number;
  longitude?: number;
}>({
  title: '',
  description: '',
  coverObjectKey: '',
  coverBucket: '',
  fileObjectKey: '',
  fileBucket: '',
  tags: '',
  extraData: '',
  categoryId: undefined as unknown as number,
  // 标准extraData字段
  ticketPrice: '',
  openTime: '',
  address: '',
  traffic: '',
  bestSeason: '',
  playTime: '',
  features: '',
  latitude: undefined,
  longitude: undefined
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入景点名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择所属类别', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入景点描述', trigger: 'blur' }
  ]
}

// 类别选项
const categoryOptions = computed(() => {
  return categories.value.map((item: CategoryVO) => ({
    label: item.name,
    value: item.id
  }))
})

// 地理位置数据双向绑定
const locationData = computed({
  get() {
    if (form.latitude && form.longitude) {
      return {
        latitude: form.latitude,
        longitude: form.longitude
      }
    }
    return undefined
  },
  set(value: { latitude: number; longitude: number } | undefined) {
    if (value) {
      form.latitude = value.latitude
      form.longitude = value.longitude
    } else {
      form.latitude = undefined
      form.longitude = undefined
    }
  }
})

// 获取景点列表
const fetchData = async () => {
  try {
    loading.value = true
    const params = {
      title: queryForm.value.title,
      categoryId: queryForm.value.categoryId,
      tag: queryForm.value.tags,
      userRealName: queryForm.value.userRealName,
      current: pagination.page,
      size: pagination.pageSize
    }
    const res = await itemApi.page(params)
    tableData.value = res.records || []
    pagination.itemCount = res.total || 0
  } catch (error) {
    console.error('获取景点列表失败', error)
    ElMessage.error('获取景点列表失败')
  } finally {
    loading.value = false
  }
}

// 获取所有类别
const fetchCategories = async () => {
  try {
    const data = await categoryApi.list()
    categories.value = data
  } catch (error) {
    console.error('获取类别列表失败', error)
    ElMessage.error('获取类别列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置搜索
const resetSearch = () => {
  queryForm.value.title = ''
  queryForm.value.categoryId = undefined
  queryForm.value.tags = ''
  queryForm.value.userRealName = ''
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
const openEditDialog = async (row: ItemVO) => {
  dialogType.value = 'edit'
  resetForm()
  form.id = row.id
  form.title = row.title
  form.description = row.description
  form.categoryId = row.category.id
  
  // 使用后端返回的tags字段
  // 由于TypeScript类型定义可能没有包含tagList，需要使用类型断言
  form.tags = typeof row.tags === 'string' ? row.tags : ((row as any).tagList || []).join(',')
  
  coverPreviewUrl.value = row.coverUrl
  showDialog.value = true
  
  // 获取完整信息
  try {
    const data = await itemApi.getById(row.id)
    // 使用类型断言解决TypeScript类型检查问题
    form.coverObjectKey = (data as any).coverObjectKey || ''
    form.coverBucket = (data as any).coverBucket || ''
    form.fileObjectKey = (data as any).fileObjectKey || ''
    form.fileBucket = (data as any).fileBucket || ''
    form.extraData = (data as any).extraData || ''
    
    // 解析extraData中的景点信息
    if (form.extraData) {
      try {
        const extraData = JSON.parse(form.extraData)
        // 标准extraData字段
        form.ticketPrice = extraData.ticketPrice || ''
        form.openTime = extraData.openTime || ''
        form.address = extraData.address || ''
        form.traffic = extraData.traffic || ''
        form.bestSeason = extraData.bestSeason || ''
        form.playTime = extraData.playTime || ''
        form.features = Array.isArray(extraData.features) ? extraData.features.join(', ') : (extraData.features || '')
        // 地理位置信息
        form.latitude = extraData.latitude || undefined
        form.longitude = extraData.longitude || undefined
      } catch (e) {
        console.error('解析extraData失败', e)
      }
    }
    
    // 如果有文件，尝试从objectKey中提取文件名
    if (form.fileObjectKey) {
      // 通常objectKey包含文件名，可以从最后一个/或最后一个_之后提取
      const filename = form.fileObjectKey.split(/[\/\_]/).pop() || '';
      // 尝试解码URL编码的文件名
      try {
        uploadedFileName.value = decodeURIComponent(filename);
      } catch (e) {
        uploadedFileName.value = filename;
      }
    }
  } catch (error) {
    console.error('获取景点详情失败', error)
    ElMessage.error('获取景点详情失败')
  }
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.title = ''
  form.description = ''
  form.coverObjectKey = ''
  form.coverBucket = ''
  form.fileObjectKey = ''
  form.fileBucket = ''
  form.tags = ''
  form.extraData = ''
  form.categoryId = undefined as unknown as number
  // 标准extraData字段
  form.ticketPrice = ''
  form.openTime = ''
  form.address = ''
  form.traffic = ''
  form.bestSeason = ''
  form.playTime = ''
  form.features = ''
  form.latitude = undefined
  form.longitude = undefined
  coverPreviewUrl.value = ''
  uploadedFileName.value = ''
}

// 自定义上传封面请求
const customCoverRequest = (options: UploadRequestOptions): Promise<any> => {
  if (!options.file) return Promise.reject(new Error('没有文件'))
  return new Promise((resolve, reject) => {
    uploadCover(options.file as File)
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

// 自定义上传文件请求
const customFileRequest = (options: UploadRequestOptions): Promise<any> => {
  if (!options.file) return Promise.reject(new Error('没有文件'))
  return new Promise((resolve, reject) => {
    uploadFile(options.file as File)
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

// 上传封面
const uploadCover = async (file: File) => {
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
      ElMessage.error('图片大小不能超过10MB')
      return Promise.reject(new Error('文件大小超过限制'))
    }
    
    // 使用fileRequest直接上传
    const bucketName = 'item-cover'
    const res = await fileRequest.upload(bucketName, file, false)
    
    if (res) {
      form.coverObjectKey = res.objectKey || ''
      form.coverBucket = res.bucket || bucketName
      ElMessage.success('封面上传成功')
      
      // 创建预览URL
      coverPreviewUrl.value = URL.createObjectURL(file)
      return Promise.resolve(res)
    } else {
      ElMessage.warning('封面上传成功但返回数据格式有误')
      return Promise.reject(new Error('返回数据格式有误'))
    }
  } catch (error) {
    console.error('上传封面失败', error)
    ElMessage.error('上传封面失败')
    return Promise.reject(error)
  } finally {
    submitLoading.value = false
  }
}

// 上传文件
const uploadFile = async (file: File) => {
  try {
    submitLoading.value = true
    
    // 保存文件名
    uploadedFileName.value = file.name
    
    // 使用fileRequest直接上传
    const bucketName = 'item-file'
    const res = await fileRequest.upload(bucketName, file, false)
    
    if (res) {
      form.fileObjectKey = res.objectKey || ''
      form.fileBucket = res.bucket || bucketName
      ElMessage.success('文件上传成功')
      return Promise.resolve(res)
    } else {
      ElMessage.warning('文件上传成功但返回数据格式有误')
      return Promise.reject(new Error('返回数据格式有误'))
    }
  } catch (error) {
    console.error('上传文件失败', error)
    ElMessage.error('上传文件失败')
    return Promise.reject(error)
  } finally {
    submitLoading.value = false
  }
}


// 取消对话框
const cancelDialog = () => {
  // 关闭对话框
  showDialog.value = false
  // 重置表单数据
  resetForm()
}

// 上传前验证封面
const beforeCoverUpload = (file: File) => {
  // 验证文件类型和大小
  const isImage = /\.(jpg|jpeg|png|gif)$/i.test(file.name)
  if (!isImage) {
    ElMessage.error('只能上传jpg、png、gif格式的图片')
    return false
  }

  const isLt2M = file.size / 1024 / 1024 < 10
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  
  return true
}

// 预览封面
const previewCover = () => {
  if (coverPreviewUrl.value) {
    // 创建一个新窗口预览图片
    window.open(coverPreviewUrl.value)
  }
}

// 移除已上传的封面
const removeUploadedCover = async () => {
  try {
    if (form.coverBucket && form.coverObjectKey) {
      // 先删除实际文件
      await fileRequest.delete(form.coverBucket, form.coverObjectKey)
    }
    // 清空表单字段
    form.coverObjectKey = ''
    form.coverBucket = ''
    coverPreviewUrl.value = ''
    ElMessage.success('封面已移除')
  } catch (error) {
    console.error('删除封面文件失败', error)
    ElMessage.error('删除封面文件失败，但本地记录已清除')
    // 即使删除失败，也清空表单字段
    form.coverObjectKey = ''
    form.coverBucket = ''
    coverPreviewUrl.value = ''
  }
}

// 上传前验证文件
const beforeFileUpload = (file: File) => {
  // 文件大小限制为10MB
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  return true
}

// 移除已上传的文件
const removeUploadedFile = async () => {
  try {
    if (form.fileBucket && form.fileObjectKey) {
      // 先删除实际文件
      await fileRequest.delete(form.fileBucket, form.fileObjectKey)
    }
    // 清空表单字段
    form.fileObjectKey = ''
    form.fileBucket = ''
    uploadedFileName.value = ''
    ElMessage.success('文件已移除')
  } catch (error) {
    console.error('删除文件失败', error)
    ElMessage.error('删除文件失败，但本地记录已清除')
    // 即使删除失败，也清空表单字段
    form.fileObjectKey = ''
    form.fileBucket = ''
    uploadedFileName.value = ''
  }
}

// 提交表单
const submitForm = () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      submitLoading.value = true
      
      // 使用表单数据的副本，不修改原始数据
      const formData = { ...form }
      
      // 构建extraData JSON，使用标准格式
      const extraData: any = {}
      
      // 标准extraData字段
      if (formData.ticketPrice) extraData.ticketPrice = formData.ticketPrice
      if (formData.openTime) extraData.openTime = formData.openTime
      if (formData.address) extraData.address = formData.address
      if (formData.traffic) extraData.traffic = formData.traffic
      if (formData.bestSeason) extraData.bestSeason = formData.bestSeason
      if (formData.playTime) extraData.playTime = formData.playTime
      if (formData.features) {
        // 将逗号分隔的字符串转换为数组
        extraData.features = formData.features.split(',').map(f => f.trim()).filter(f => f)
      }
      // 地理位置信息
      if (formData.latitude !== undefined && formData.longitude !== undefined) {
        extraData.latitude = formData.latitude
        extraData.longitude = formData.longitude
      }
      
      const extraDataJson = JSON.stringify(extraData)
      
      if (dialogType.value === 'add') {
        // 添加操作发送处理后的表单数据
        await itemApi.add({
          ...formData,
          tags: formData.tags, // 保持原字符串格式，后端会处理分割
          extraData: extraDataJson,
          // 如果没有上传封面或文件，则这些字段为空字符串
          coverObjectKey: formData.coverObjectKey || '',
          coverBucket: formData.coverBucket || '',
          fileObjectKey: formData.fileObjectKey || '',
          fileBucket: formData.fileBucket || ''
        })
        ElMessage.success('添加成功')
      } else {
        // 更新操作发送处理后的表单数据
        const updateData: ItemUpdateDTO = {
          id: formData.id!,
          title: formData.title,
          description: formData.description,
          tags: formData.tags, // 保持原字符串格式，后端会处理分割
          categoryId: formData.categoryId,
          // 添加额外数据字段
          extraData: extraDataJson,
          // 如果没有上传封面或文件，则这些字段为空字符串
          coverObjectKey: formData.coverObjectKey || '',
          coverBucket: formData.coverBucket || '',
          fileObjectKey: formData.fileObjectKey || '',
          fileBucket: formData.fileBucket || ''
        }
        await itemApi.update(updateData)
        ElMessage.success('更新成功')
      }
      
      showDialog.value = false
      fetchData()
    } catch (error) {
      console.error(dialogType.value === 'add' ? '添加失败' : '更新失败', error)
      ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 删除景点
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该景点吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消', 
      type: 'warning'
    })
    
    await itemApi.delete(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 计算属性：对话框标题

// 查看景点详情
const viewAttractionDetail = (item: ItemVO) => {
  router.push({
    name: 'UserItemDetail',
    params: { id: item.id }
  })
}



onMounted(() => {
  fetchData()
  fetchCategories()
})
</script>

<style scoped>
.item-management {
  width: 100%;
  position: relative;
  z-index: 1;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.preview-image {
  width: 100%;
  max-width: 200px;
  height: 150px;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 10px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.preview-image img:hover {
  transform: scale(1.05);
}

.preview-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 5px;
}

.ml-4 {
  margin-left: 16px;
}

.mt-2 {
  margin-top: 8px;
}

.mt-4 {
  margin-top: 16px;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.file-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.file-uploader .el-upload:hover {
  border-color: #409EFF;
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

.cover-preview {
  display: flex;
  justify-content: center;
  align-items: center;
}

.cover-image {
  border-radius: 4px;
  transition: transform 0.3s;
  cursor: pointer;
}

.cover-image:hover {
  transform: scale(1.05);
}

.no-cover {
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

.no-cover .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 4px 0;
}

.item-tag {
  margin-right: 0;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-tags {
  color: #909399;
  font-size: 13px;
}

.form-tips {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.upload-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.cover-uploader {
  width: 240px;
  height: 240px;
}

.cover-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 240px;
  height: 240px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.cover-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0;
  border: none;
}

.cover-preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
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

.file-uploader {
  width: 360px;
}

.file-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 360px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100px;
}

.file-uploader :deep(.el-upload:hover) {
  border-color: #409EFF;
}

.file-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0;
  border: none;
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

.file-info-container {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 16px;
}

.file-icon {
  font-size: 32px;
  color: #409EFF;
  margin-right: 16px;
}

.file-info {
  display: flex;
  flex-direction: column;
}

.file-name {
  font-size: 16px;
  color: #303133;
  margin-bottom: 4px;
  font-weight: 500;
  word-break: break-all;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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


</style> 