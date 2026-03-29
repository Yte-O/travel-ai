<template>
  <div class="item-list">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>景点浏览</span>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :model="queryForm" inline>
          <el-form-item label="景点名称">
            <el-input v-model="queryForm.title" placeholder="输入景点名称" @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="所属类别">
            <el-select
              v-model="queryForm.categoryId"
              placeholder="请选择类别"
              clearable
              style="width: 150px;"
              @keyup.enter="handleSearch"
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
            <el-input v-model="queryForm.tag" placeholder="输入标签" @keyup.enter="handleSearch"/>
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

      <!-- 景点网格 -->
      <el-row :gutter="16">
        <el-col v-for="item in items" 
                :key="item.id"
                :xs="24" :sm="12" :md="8" :lg="6" :xl="4.8"
                :class="{ 'mb-16': true }">
          <el-card shadow="hover" @click="handleItemClick(item)" class="item-card">
            <div class="item-img-container">
              <el-image
                v-if="item.coverUrl"
                :src="item.coverUrl"
                fit="contain"
                class="item-image"
                :preview-src-list="[item.coverUrl]"
                :initial-index="0"
                :hide-on-click-modal="false"
                preview-teleported
              />
              <div v-else class="no-image-placeholder">
                <el-icon><Picture /></el-icon>
                <span>暂无封面</span>
              </div>
            </div>
            <h3 class="item-name">{{ item.title }}</h3>
            <div class="item-meta">
              <el-tag type="info" effect="plain">{{ item.category.name }}</el-tag>
            </div>
            <div class="item-description">{{ item.description }}</div>
            <div class="item-tags">
              <el-tag 
                v-for="tag in (item as any).tagList || []" 
                :key="tag" 
                size="small" 
                effect="light"
                type="success" 
                class="tag" 
                round
              >
                {{ tag }}
              </el-tag>
              <span v-if="!(item as any).tagList || (item as any).tagList.length === 0" class="no-tags">暂无标签</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div v-if="loading" class="loading-container">
        <el-skeleton animated :rows="3" :loading="loading" />
      </div>

      <div v-if="!loading && items.length === 0" class="empty-container">
        <el-empty description="暂无景点数据" />
      </div>

      <!-- 分页器 -->
      <el-pagination
        v-if="!loading && items.length > 0"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 30, 40]"
        :total="pagination.itemCount"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handlePageSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>

    <!-- 全局图片查看器容器，确保预览组件能够正确显示 -->
    <div class="image-viewer-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { itemApi } from '@/api/item'
import { categoryApi } from '@/api/category'
import type { ItemVO, ItemQueryDTO, CategoryVO } from '@/types/item'
import { Picture } from '@element-plus/icons-vue'

const router = useRouter()
const items = ref<ItemVO[]>([])
const loading = ref(false)
const categories = ref<CategoryVO[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

// 查询表单
const queryForm = reactive<ItemQueryDTO>({
  title: '',
  categoryId: undefined,
  tag: '',
  current: 1,
  size: 10
})

// 类别选项
const categoryOptions = computed(() => {
  return categories.value.map(item => ({
    label: item.name,
    value: item.id
  }))
})

// 获取景点列表
const fetchItems = async () => {
  try {
    loading.value = true
    queryForm.current = pagination.page
    queryForm.size = pagination.pageSize
    const res = await itemApi.page(queryForm)
    items.value = res.records || []
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
    const res = await categoryApi.list()
    categories.value = res || []
  } catch (error) {
    console.error('获取类别列表失败', error)
    ElMessage.error('获取类别列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchItems()
}

// 重置搜索
const resetSearch = () => {
  queryForm.title = ''
  queryForm.categoryId = undefined
  queryForm.tag = ''
  pagination.page = 1
  fetchItems()
}

// 页码变化
const handlePageChange = (page: number) => {
  pagination.page = page
  fetchItems()
}

// 每页条数变化
const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchItems()
}

// 点击景点卡片
const handleItemClick = (item: ItemVO) => {
  router.push({
    name: 'UserItemDetail',
    params: { id: item.id }
  })
}

// 页面加载时初始化数据
onMounted(() => {
  // 检查URL中是否有关键字参数
  const { keyword, categoryId, tag } = router.currentRoute.value.query;
  
  if (keyword) {
    queryForm.title = decodeURIComponent(keyword as string);
  }
  
  if (categoryId && !isNaN(Number(categoryId))) {
    queryForm.categoryId = Number(categoryId);
  }
  
  if (tag) {
    queryForm.tag = decodeURIComponent(tag as string);
  }
  
  fetchItems();
  fetchCategories();
})
</script>

<style scoped>
.item-list {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
}

.mb-16 {
  margin-bottom: 16px;
}

.item-card {
  cursor: pointer;
  transition: all 0.3s;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.item-img-container {
  overflow: hidden;
  border-radius: 4px;
  background-color: #f5f7fa;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.item-image {
  width: 100%;
  height: 200px;
  object-fit: contain;
  transition: transform 0.3s;
}

.item-card:hover .item-image {
  transform: scale(1.05);
}

.item-name {
  font-size: 16px;
  font-weight: bold;
  margin-top: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  margin-top: 8px;
  margin-bottom: 8px;
}

.item-description {
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: #606266;
  margin-bottom: 8px;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  margin-top: 8px;
}

.tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.empty-container {
  padding: 40px 0;
}

.ml-4 {
  margin-left: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.no-image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 4px;
  font-size: 14px;
}

.no-image-placeholder .el-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.no-tags {
  color: #909399;
  font-size: 13px;
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
</style> 