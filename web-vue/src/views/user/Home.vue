<!-- 用户首页 -->
<template>
  <div class="user-home">
    <!-- 轮播图区域 -->
    <div class="banner-section">
      <el-carousel 
        :interval="4000" 
        type="card" 
        height="200px" 
        indicator-position="outside"
        :autoplay="true"
      >
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="banner-item" @click="handleBannerClick(banner)">
            <el-image 
              :src="banner.imageUrl" 
              fit="cover" 
              class="banner-image"
              :preview-src-list="[banner.imageUrl]"
              :initial-index="0"
            />
            <div class="banner-title">{{ banner.title }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <div class="home-content">
      <!-- 左侧内容区 -->
      <div class="main-section">
        <!-- 推荐景点 -->
        <div class="section-container">
          <div class="section-header">
            <div class="section-title-container">
              <h2 class="section-title">推荐景点</h2>
              <div class="recommendation-type-selector">
                <el-radio-group v-model="recommendationType" size="small" @change="fetchRecommendedItems">
                                <el-radio-button value="user">相似用户</el-radio-button>
              <el-radio-button value="content">相似景点</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            <router-link to="/user/items" class="more-link">
              更多
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
          
          <div class="item-grid">
            <div 
              v-for="item in recommendedItems" 
              :key="item.id" 
              class="item-card"
              @click="goToItemDetail(item.id)"
            >
              <div class="item-cover">
                <el-image 
                  :src="item.coverUrl || defaultCover" 
                  fit="cover" 
                  class="cover-image"
                  loading="lazy"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="item-stats">
                  <span class="stat">
                    <el-icon><View /></el-icon> {{ formatNumber(item.views || 0) }}
                  </span>
                  <span class="stat">
                    <el-icon><Star /></el-icon> {{ formatNumber(item.favorites || 0) }}
                  </span>
                </div>
              </div>
              <div class="item-info">
                <div class="item-title">{{ item.title }}</div>
                <div class="item-meta">
                  <span class="uploader">{{ item.userRealName || item.username || '未知用户' }}</span>
                  <span v-if="item.categoryName" class="item-category">
                    <el-tag size="small" effect="plain">{{ item.categoryName }}</el-tag>
                  </span>
                </div>
              </div>
            </div>
          </div>

          <el-empty 
            v-if="recommendedItems.length === 0 && !recommendationsLoading" 
            description="暂无推荐景点" 
          />
          <div v-if="recommendationsLoading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
        </div>

        <!-- 热门景点 -->
        <div class="section-container">
          <div class="section-header">
            <h2 class="section-title">热门景点</h2>
            <router-link to="/user/items?sort=popular" class="more-link">
              更多
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
          
          <div class="item-grid">
            <div 
              v-for="item in popularItems" 
              :key="item.id" 
              class="item-card"
              @click="goToItemDetail(item.id)"
            >
              <div class="item-cover">
                <el-image 
                  :src="item.coverUrl || defaultCover" 
                  fit="cover" 
                  class="cover-image"
                  loading="lazy"
                >
                  <template #error>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="item-stats">
                  <span class="stat">
                    <el-icon><View /></el-icon> {{ formatNumber(item.views || 0) }}
                  </span>
                  <span class="stat">
                    <el-icon><Star /></el-icon> {{ formatNumber(item.favorites || 0) }}
                  </span>
                </div>
              </div>
              <div class="item-info">
                <div class="item-title">{{ item.title }}</div>
                <div class="item-meta">
                  <span class="uploader">{{ item.userRealName || '未知用户' }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-empty 
            v-if="popularItems.length === 0 && !loading" 
            description="暂无热门景点" 
          />
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="sidebar-section">
        <!-- 用户信息卡片 -->
        <div class="user-card">
          <div class="user-profile">
            <el-avatar :size="50" :src="userInfo?.avatarUrl || defaultAvatar">
              {{ userInfo?.username?.substring(0, 1) }}
            </el-avatar>
            <div class="user-details">
              <div class="username">{{ userInfo?.realName || userInfo?.username || '游客' }}</div>
              <div class="user-role">{{ userInfo?.role === 1 ? '管理员' : '普通用户' }}</div>
            </div>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-value">{{ userStats.favorites || 0 }}</div>
              <div class="stat-label">收藏</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userStats.history || 0 }}</div>
              <div class="stat-label">浏览历史</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ userStats.reservations || 0 }}</div>
              <div class="stat-label">已预约</div>
            </div>
          </div>
        </div>

        <!-- 热门分类 -->
        <div class="sidebar-card">
          <div class="card-header">
            <h3>热门分类</h3>
            <router-link to="/user/categories" class="more-link">
              更多
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
          <div class="category-list">
            <div 
              v-for="category in popularCategories" 
              :key="category.id" 
              class="category-item"
              @click="goToCategoryDetail(category.id)"
            >
              <div class="category-icon">
                <el-image 
                  v-if="category.iconUrl" 
                  :src="category.iconUrl" 
                  fit="cover"
                  loading="lazy"
                />
                <el-icon v-else><Grid /></el-icon>
              </div>
              <div class="category-name">{{ category.name }}</div>
            </div>
          </div>
          <el-empty 
            v-if="popularCategories.length === 0 && !loading" 
            description="暂无分类" 
            :image-size="60"
          />
        </div>

        <!-- AI 聊天入口 -->
        <div class="chat-card">
          <div class="chat-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="chat-info">
            <div class="chat-title">AI 智能助手</div>
            <div class="chat-desc">有任何问题，随时向AI提问</div>
          </div>
          <el-button 
            type="primary" 
            size="small" 
            @click="goToChat"
            class="chat-button"
            round
          >
            开始聊天
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ArrowRight, Picture, View, Star, Grid, ChatDotRound } from '@element-plus/icons-vue'
import { itemApi } from '@/api/item'
import { categoryApi } from '@/api/category'
import { favoriteApi } from '@/api/favorite'
import { pageMyActions, getItemViewCount } from '@/api/userAction'
import { recommendationApi } from '@/api/recommendation'
import { fileRequest } from '@/api/file_request'
import { ElMessage } from 'element-plus'

// 使用网络图片URL替代本地资源
const defaultAvatar = 'https://pic.imgdb.cn/item/65ae0c899f345e8d03301865.jpg'
const defaultCover = 'https://pic.imgdb.cn/item/65ae0c899f345e8d033018ab.jpg'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const recommendationsLoading = ref(false) // 单独的推荐加载状态
const userInfo = computed(() => userStore.userInfo)
const recommendationType = ref('user') // 推荐类型：相似用户或相似景点

// 用户统计数据
const userStats = ref({
  favorites: 0,
  history: 0,
  reservations: 0
})

// 轮播图数据
const banners = ref([
  {
    id: 1,
    title: 'Travel AI 推荐系统正式上线',
    imageUrl: 'https://cdn.pixabay.com/photo/2022/12/07/23/28/way-7642285_960_720.jpg',
    link: '/user/items'
  },
  {
    id: 2,
    title: '发现各种优质旅游景点',
    imageUrl: 'https://cdn.pixabay.com/photo/2021/10/09/00/15/landscape-6692712_960_720.jpg',
    link: '/user/categories'
  },
  {
    id: 3,
    title: 'AI智能助手随时解答',
    imageUrl: 'https://cdn.pixabay.com/photo/2023/12/30/21/14/fields-8478994_960_720.jpg',
    link: '/user/chat'
  }
])

// 推荐景点
const recommendedItems = ref<any[]>([])

// 热门景点
const popularItems = ref<any[]>([])

// 热门分类
const popularCategories = ref<any[]>([])

// 格式化数字（如：1000 -> 1k）
const formatNumber = (num: number) => {
  if (num < 1000) return num
  if (num < 10000) return (num / 1000).toFixed(1) + 'k'
  return (num / 10000).toFixed(1) + 'w'
}

// 处理轮播图点击
const handleBannerClick = (banner: any) => {
  router.push(banner.link)
}

// 跳转到景点详情页
const goToItemDetail = (id: number) => {
  router.push(`/user/item/${id}`)
}

// 跳转到分类详情页
const goToCategoryDetail = (id: number) => {
  router.push(`/user/category/${id}`)
}

// 跳转到聊天页面
const goToChat = () => {
  router.push('/user/chat')
}

// 获取推荐景点
const fetchRecommendedItems = async () => {
  try {
    recommendationsLoading.value = true
    recommendedItems.value = []
    
    // 检查用户是否登录
    if (!userStore.userInfo?.id) {
      // 如果未登录，获取默认推荐
      const res = await itemApi.page({
        current: 1,
        size: 8
      })
      recommendedItems.value = res.records
    } else {
      // 根据推荐类型调用不同的API
      let items = []
      if (recommendationType.value === 'user') {
        // 调用相似用户推荐接口
        items = await recommendationApi.getForUser(userStore.userInfo.id, 8)
      } else {
        // 调用相似景点推荐接口
        items = await recommendationApi.getContentForUser(userStore.userInfo.id, 8)
      }
      
      if (items && items.length > 0) {
        recommendedItems.value = items
        // 为推荐景点生成图片URL
        for (const item of recommendedItems.value) {
          if (item.coverBucket && item.coverObjectKey) {
            item.coverUrl = fileRequest.getFileUrl(item.coverBucket, item.coverObjectKey)
          }
        }
      } else {
        // 如果推荐结果为空，获取默认推荐
        const res = await itemApi.page({
          current: 1,
          size: 8
        })
        recommendedItems.value = res.records
        
        if (items.length === 0) {
          ElMessage.info(`暂无${recommendationType.value === 'user' ? '相似用户' : '相似景点'}推荐，已显示默认推荐`)
        }
      }
    }
    
    // 获取每个景点的收藏数和浏览数
    for (const item of recommendedItems.value) {
      try {
        // 获取收藏数
        const favoriteCount = await favoriteApi.getItemFavoriteCount(item.id)
        item.favorites = favoriteCount
        
        // 获取浏览数
        const viewCount = await getItemViewCount(item.id)
        item.views = viewCount
      } catch (error) {
        console.error(`获取景点${item.id}的统计数据失败`, error)
      }
    }
  } catch (error) {
    console.error('获取推荐景点失败', error)
    ElMessage.error('获取推荐景点失败')
  } finally {
    recommendationsLoading.value = false
  }
}

// 获取热门景点
const fetchPopularItems = async () => {
  try {
    const res = await itemApi.page({
      current: 1,
      size: 100 // 获取更多项目用于排序
    })
    let items = res.records
    
    // 获取每个景点的收藏数和浏览数
    for (const item of items) {
      try {
        // 获取收藏数
        const favoriteCount = await favoriteApi.getItemFavoriteCount(item.id)
        item.favorites = favoriteCount || 0
        
        // 获取浏览数
        const viewCount = await getItemViewCount(item.id)
        item.views = viewCount || 0
      } catch (error) {
        console.error(`获取景点${item.id}的统计数据失败`, error)
        // 确保有默认值
        item.favorites = item.favorites || 0
        item.views = item.views || 0
      }
    }
    
    // 根据浏览数和收藏数进行加权排序
    items.sort((a, b) => {
      // 计算热门度：收藏数*1.5 + 浏览数*0.7
      const scoreA = ((a.favorites ?? 0) * 1.5) + ((a.views ?? 0) * 0.7)
      const scoreB = ((b.favorites ?? 0) * 1.5) + ((b.views ?? 0) * 0.7)
      return scoreB - scoreA // 降序排序
    })
    
    // 取前8个作为热门景点
    popularItems.value = items.slice(0, 8)
  } catch (error) {
    console.error('获取热门景点失败', error)
  }
}

// 获取热门分类
const fetchPopularCategories = async () => {
  try {
    const res = await categoryApi.page({
      current: 1,
      size: 6
    })
    popularCategories.value = res.records
  } catch (error) {
    console.error('获取热门分类失败', error)
  }
}

// 获取用户统计信息
const fetchUserStats = async () => {
  try {
    // 获取收藏数量
    const favoriteRes = await favoriteApi.getUserFavoriteItemIds();
    userStats.value.favorites = favoriteRes.length;
    
    // 获取浏览历史数量
    const historyRes = await pageMyActions({
      current: 1,
      size: 1,
      actionType: 0 // 0表示浏览
    });
    userStats.value.history = historyRes.total;
    
    // 获取预约数量
    const purchaseRes = await pageMyActions({
      current: 1,
      size: 1,
      actionType: 1 // 1表示预约
    });
    userStats.value.reservations = purchaseRes.total;
  } catch (error) {
    console.error('获取用户统计数据失败', error);
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchRecommendedItems(),
      fetchPopularItems(),
      fetchPopularCategories(),
      fetchUserStats()
    ])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.user-home {
  padding-top: 10px;
}

/* 轮播图区域 */
.banner-section {
  margin-bottom: 30px;
}

.banner-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  transition: transform 0.3s;
}

.banner-item:hover .banner-image {
  transform: scale(1.05);
}

.banner-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 10px 15px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  color: white;
  font-size: 16px;
  font-weight: bold;
}

/* 内容区域布局 */
.home-content {
  display: flex;
  gap: 20px;
}

.main-section {
  flex: 1;
}

.sidebar-section {
  width: 300px;
}

/* 内容区块通用样式 */
.section-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  color: #303133;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background-color: #409EFF;
  border-radius: 2px;
}

.recommendation-type-selector {
  margin-left: 12px;
  margin-top: 4px;
}

.more-link {
  color: #909399;
  font-size: 14px;
  text-decoration: none;
  display: flex;
  align-items: center;
}

.more-link:hover {
  color: #409EFF;
}

.more-link .el-icon {
  margin-left: 4px;
  font-size: 12px;
}

/* 景点卡片布局 */
.item-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.item-card {
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.3s;
  background-color: white;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.item-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  background-color: #f0f2f5;
  border-radius: 6px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.item-card:hover .cover-image {
  transform: scale(1.05);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #c0c4cc;
}

.image-placeholder .el-icon {
  font-size: 24px;
}

.item-stats {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 6px 8px;
  display: flex;
  justify-content: space-between;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
  color: white;
  font-size: 12px;
}

.stat {
  display: flex;
  align-items: center;
}

.stat .el-icon {
  margin-right: 4px;
  font-size: 12px;
}

.item-info {
  padding: 10px 0 5px;
}

.item-title {
  font-size: 14px;
  line-height: 1.4;
  font-weight: 500;
  margin-bottom: 5px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 40px;
}

.item-meta {
  color: #909399;
  font-size: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.uploader {
  display: flex;
  align-items: center;
}

.item-category {
  margin-left: 8px;
}

/* 用户信息卡片 */
.user-card,
.sidebar-card,
.chat-card {
  background: white;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.user-profile {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.user-details {
  margin-left: 15px;
}

.username {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
}

.user-role {
  font-size: 12px;
  color: #909399;
}

.user-stats {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid #f0f2f5;
  padding-top: 15px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 卡片通用标题 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

/* 分类列表 */
.category-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.category-list .category-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 6px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: all 0.2s;
}

.category-list .category-item:hover {
  background-color: #ecf5ff;
}

.category-icon {
  width: 28px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 8px;
  color: #409EFF;
}

.category-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.category-name {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* AI 聊天卡片 */
.chat-card {
  display: flex;
  align-items: center;
  padding: 15px;
  background: linear-gradient(135deg, #e0f2ff, #e6f7ff);
  border-radius: 8px;
}

.chat-icon {
  font-size: 28px;
  color: #409EFF;
  margin-right: 15px;
}

.chat-info {
  flex: 1;
}

.chat-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.chat-desc {
  font-size: 12px;
  color: #606266;
}

.chat-button {
  margin-left: 10px;
}

/* 加载状态 */
.loading-container {
  padding: 20px 0;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .item-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .home-content {
    flex-direction: column;
  }
  
  .sidebar-section {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .item-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .more-link {
    margin-top: 10px;
  }
}

@media (max-width: 576px) {
  .item-grid {
    grid-template-columns: 1fr;
  }
}
</style> 