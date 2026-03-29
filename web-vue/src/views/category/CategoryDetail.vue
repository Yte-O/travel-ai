<template>
  <div class="category-detail">
    <el-card v-if="category" class="main-card">
      <template #header>
        <div class="card-header">
          <span class="category-title">{{ category.name }}</span>
          <el-button @click="goBack" text>
            <el-icon class="mr-1"><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </template>
      
      <div class="category-header">
        <div class="category-icon-container">
          <img v-if="category.iconUrl" :src="category.iconUrl" class="category-icon" />
          <div v-else class="no-image-placeholder">
            <el-icon><Picture /></el-icon>
            <span>暂无封面</span>
          </div>
        </div>
        <div class="category-info">
          <p class="category-description">{{ category.description }}</p>
          <div class="category-meta-container">
            <p class="category-meta"><el-icon><Calendar /></el-icon> 创建时间：{{ formatDate(category.createTime) }}</p>
            <p class="category-meta"><el-icon><Timer /></el-icon> 更新时间：{{ formatDate(category.updateTime) }}</p>
          </div>
        </div>
      </div>

      <el-divider>
        <span class="divider-content">景点列表</span>
      </el-divider>

      <!-- 景点列表 -->
      <el-row :gutter="20">
        <el-col v-for="item in items" 
                :key="item.id"
                :xs="24" :sm="12" :md="8" :lg="6" :xl="4.8"
                :class="{ 'mb-20': true }">
          <el-card shadow="hover" @click="handleItemClick(item)" class="item-card">
            <div class="item-img-container">
              <img v-if="item.coverUrl" :src="item.coverUrl" class="item-image" />
              <div v-else class="no-image-placeholder">
                <el-icon><Picture /></el-icon>
                <span>暂无封面</span>
              </div>
            </div>
            <h3 class="item-title">{{ item.title }}</h3>
            <div class="item-description">{{ item.description }}</div>
            <div class="item-tags">
              <el-tag 
                v-for="tag in getTagsArray(item.tags)" 
                :key="tag" 
                size="small" 
                effect="light"
                type="success" 
                class="tag" 
                round
              >
                {{ tag }}
              </el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div v-if="loading" class="loading-container">
        <el-skeleton animated :rows="3" :loading="loading" />
      </div>

      <div v-if="!loading && items.length === 0" class="empty-container">
        <el-empty description="该类别下暂无景点" />
      </div>
    </el-card>

    <div v-if="!category && !loading" class="error-container">
      <el-result 
        icon="error" 
        title="未找到"
        sub-title="未找到该类别信息">
        <template #extra>
          <el-button type="primary" @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>

    <div v-if="loading && !category" class="loading-container">
      <el-skeleton animated :rows="3" :loading="loading" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { categoryApi } from '@/api/category'
import { itemApi } from '@/api/item'
import type { CategoryVO, ItemVO } from '@/types/item'
import { Picture, ArrowLeft, Calendar, Timer } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const category = ref<CategoryVO | null>(null)
const items = ref<ItemVO[]>([])
const loading = ref(false)

// 获取类别详情
const fetchCategoryDetail = async (id: number) => {
  if (!id) {
    ElMessage.error('类别ID无效')
    return
  }

  try {
    loading.value = true
    const res = await categoryApi.getById(id)
    category.value = res || null
  } catch (error) {
    console.error('获取类别详情失败', error)
    ElMessage.error('获取类别详情失败')
    category.value = null
  } finally {
    loading.value = false
  }
}

// 获取类别下的景点列表
const fetchCategoryItems = async (id: number) => {
  if (!id) return

  try {
    loading.value = true
    const res = await itemApi.listByCategoryId(id)
    items.value = res || []
  } catch (error) {
    console.error('获取景点列表失败', error)
    ElMessage.error('获取景点列表失败')
  } finally {
    loading.value = false
  }
}

// 使用watchEffect监听路由参数变化
watchEffect(() => {
  const categoryId = Number(route.params.id)
  if (categoryId) {
    fetchCategoryDetail(categoryId)
    fetchCategoryItems(categoryId)
  }
})

// 点击景点卡片
const handleItemClick = (item: ItemVO) => {
  router.push({
    name: 'UserItemDetail',
    params: { id: item.id }
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
}

// 处理标签，支持字符串或数组类型
const getTagsArray = (tags: string | string[] | undefined | null): string[] => {
  if (!tags) return []
  
  // 如果已经是数组，直接返回
  if (Array.isArray(tags)) return tags.filter(Boolean)
  
  // 如果是字符串，按逗号分割
  return tags.split(',').map(tag => tag.trim()).filter(Boolean)
}
</script>

<style scoped>
.category-detail {
  width: 100%;
  max-width: 1500px;
  margin: 0 auto;
  padding: 10px;
}

.main-card {
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

.main-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.category-header {
  display: flex;
  flex-direction: column;
  margin-bottom: 24px;
}

.category-icon-container {
  width: 120px;
  height: 120px;
  margin-right: 24px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.category-icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.category-icon:hover {
  transform: scale(1.05);
}

.category-info {
  flex: 1;
}

.category-description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 16px;
  color: #303133;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.category-meta-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-meta {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.category-meta .el-icon {
  margin-right: 8px;
}

.divider-content {
  font-size: 18px;
  font-weight: 600;
  color: #409EFF;
}

.mb-20 {
  margin-bottom: 20px;
}

.mr-1 {
  margin-right: 4px;
}

.item-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.item-img-container {
  overflow: hidden;
  border-radius: 4px;
  flex: none;
}

.item-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.item-card:hover .item-image {
  transform: scale(1.05);
}

.item-title {
  font-size: 16px;
  font-weight: bold;
  margin-top: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #303133;
}

.item-description {
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: #606266;
  margin-top: 8px;
  margin-bottom: 8px;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  margin-top: auto;
  padding-top: 8px;
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

.error-container {
  padding: 40px 0;
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
  border-radius: 8px;
  font-size: 14px;
}

.no-image-placeholder .el-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

@media (min-width: 768px) {
  .category-detail {
    padding: 20px;
  }

  .category-header {
    flex-direction: row;
  }
  
  .category-icon-container {
    width: 140px;
    height: 140px;
  }
  
  .item-image {
    height: 220px;
  }
  
  .category-meta-container {
    flex-direction: row;
    gap: 20px;
  }
}

@media (min-width: 992px) {
  .category-icon-container {
    width: 160px;
    height: 160px;
  }
}
</style> 