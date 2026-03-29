<template>
  <div class="category-list">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>类别浏览</span>
        </div>
      </template>
      
      <el-row :gutter="16">
        <el-col 
          v-for="category in categories" 
          :key="category.id"
          :xs="24" :sm="12" :md="8" :lg="6" :xl="4.8"
          :class="{ 'mb-16': true }">
          <el-card 
            shadow="hover" 
            @click="handleCategoryClick(category)" 
            class="category-card">
            <div class="category-img-container">
              <img v-if="category.iconUrl" :src="category.iconUrl" class="category-image" />
              <div v-else class="no-image-placeholder">
                <el-icon><Picture /></el-icon>
                <span>暂无封面</span>
              </div>
            </div>
            <h3 class="category-name">{{ category.name }}</h3>
            <div class="category-description">{{ category.description }}</div>
          </el-card>
        </el-col>
      </el-row>

      <div v-if="loading" class="loading-container">
        <el-skeleton animated :rows="3" :loading="loading" />
      </div>

      <div v-if="!loading && categories.length === 0" class="empty-container">
        <el-empty description="暂无类别数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { categoryApi } from '@/api/category'
import type { CategoryVO } from '@/types/item'
import { Picture } from '@element-plus/icons-vue'

const router = useRouter()
const categories = ref<CategoryVO[]>([])
const loading = ref(false)

// 获取所有类别
const fetchCategories = async () => {
  try {
    loading.value = true
    const res = await categoryApi.list()
    categories.value = res || []
  } catch (error) {
    console.error('获取类别列表失败', error)
    ElMessage.error('获取类别列表失败')
  } finally {
    loading.value = false
  }
}

// 点击类别卡片
const handleCategoryClick = (category: CategoryVO) => {
  router.push({
    name: 'UserCategoryDetail',
    params: { id: category.id }
  })
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-list {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mb-16 {
  margin-bottom: 16px;
}

.category-card {
  cursor: pointer;
  transition: all 0.3s;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.category-img-container {
  overflow: hidden;
  border-radius: 4px;
}

.category-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: transform 0.3s;
}

.category-card:hover .category-image {
  transform: scale(1.05);
}

.category-name {
  font-size: 16px;
  font-weight: bold;
  margin-top: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-description {
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: #606266;
  margin-top: 8px;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.empty-container {
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
  border-radius: 4px;
  font-size: 14px;
}

.no-image-placeholder .el-icon {
  font-size: 36px;
  margin-bottom: 8px;
}
</style> 