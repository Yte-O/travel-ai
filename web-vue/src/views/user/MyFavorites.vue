<template>
  <div class="my-favorites">
    <div class="favorites-header">
      <h3>我的收藏</h3>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="favorites.length === 0" class="empty-container">
      <el-empty description="暂无收藏" />
    </div>
    
    <div v-else class="favorites-list">
      <div 
        v-for="favorite in favorites" 
        :key="favorite.id" 
        class="favorite-item"
        :class="{ 'has-cover': favorite.item?.coverUrl }"
      >
        <div class="favorite-content">
          <!-- 景点封面图 - 可点击 -->
          <div class="favorite-cover" @click="navigateToDetail(favorite.itemId)">
            <el-image 
              v-if="favorite.item?.coverUrl" 
              :src="favorite.item.coverUrl" 
              fit="cover"
              class="item-cover"
            />
            <div v-else class="no-image">
              <el-icon><Picture /></el-icon>
            </div>
            <div class="cover-overlay">
              <el-icon class="view-icon"><View /></el-icon>
              <span>查看详情</span>
            </div>
          </div>
          
          <div class="favorite-info">
            <h3 class="item-title">
              <router-link :to="`/user/item/${favorite.itemId}`">{{ favorite.item?.title }}</router-link>
            </h3>
            
            <p 
              v-if="favorite.item?.description" 
              class="item-description"
              @click="navigateToDetail(favorite.itemId)"
            >
              {{ truncateDescription(favorite.item.description) }}
            </p>
            
            <div class="item-meta">
              <div class="meta-left">
                <el-tag v-if="favorite.item?.category" size="small" type="info">{{ favorite.item.category.name }}</el-tag>
                <span class="favorite-time">收藏于: {{ formatTime(favorite.createTime) }}</span>
              </div>
              
              <div class="meta-right">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="navigateToDetail(favorite.itemId)"
                >
                  <el-icon><View /></el-icon> 查看详情
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleRemoveFavorite(favorite.itemId)"
                >
                  <el-icon><Delete /></el-icon> 取消收藏
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { favoriteApi } from '@/api/favorite'
import type { FavoriteVO } from '@/types/favorite'
import { Picture, View, Delete } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/date'

const router = useRouter()
const favorites = ref<FavoriteVO[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取用户收藏列表
const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await favoriteApi.page(currentPage.value, pageSize.value)
    favorites.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('获取收藏列表失败', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 导航到景点详情页
const navigateToDetail = (itemId: number) => {
  router.push(`/user/item/${itemId}`)
}

// 取消收藏
const handleRemoveFavorite = (itemId: number) => {
  ElMessageBox.confirm('确定要取消收藏吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await favoriteApi.remove(itemId)
      ElMessage.success('取消收藏成功')
      // 刷新列表
      fetchFavorites()
    } catch (error) {
      console.error('取消收藏失败', error)
      ElMessage.error('取消收藏失败')
    }
  }).catch(() => {})
}

// 格式化时间
const formatTime = (dateStr: string) => {
  return formatDate(new Date(dateStr), 'YYYY-MM-DD HH:mm')
}

// 截断描述文字
const truncateDescription = (text: string, maxLength: number = 100) => {
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchFavorites()
}

// 处理每页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchFavorites()
}

onMounted(() => {
  fetchFavorites()
})
</script>

<style scoped>
.my-favorites {
  width: 100%;
  padding: 0;
  background: #f5f7fa;
  border-radius: 4px;
}

.favorites-header {
  background: #fff;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  border-radius: 4px 4px 0 0;
}

.favorites-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.loading-container, 
.empty-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
  background: #fff;
  border-radius: 0 0 4px 4px;
}

.favorites-list {
  background: #fff;
  padding: 0 0 16px 0;
  border-radius: 0 0 4px 4px;
}

.favorite-item {
  margin-bottom: 0;
  transition: all 0.3s;
  background: #fff;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.favorite-item:hover {
  background-color: #f9fafc;
}

.favorite-item:last-child {
  border-bottom: none;
}

.favorite-content {
  display: flex;
  gap: 16px;
}

.favorite-cover {
  position: relative;
  width: 120px;
  height: 90px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f7fa;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.item-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.favorite-cover:hover .item-cover {
  transform: scale(1.05);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.favorite-cover:hover .cover-overlay {
  opacity: 1;
}

.view-icon {
  font-size: 20px;
  margin-bottom: 5px;
}

.no-image {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.no-image .el-icon {
  font-size: 32px;
}

.favorite-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 500;
}

.item-title a {
  color: #303133;
  text-decoration: none;
  transition: color 0.3s;
}

.item-title a:hover {
  color: #409eff;
}

.item-description {
  margin: 0 0 12px;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  cursor: pointer;
  transition: color 0.3s;
}

.item-description:hover {
  color: #409eff;
}

.item-meta {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-right {
  display: flex;
  gap: 8px;
}

.favorite-time {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  padding: 0 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-button) {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style> 