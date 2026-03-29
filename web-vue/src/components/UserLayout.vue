<!-- 用户端布局组件 - B站风格 -->
<template>
  <div class="user-layout">
    <!-- 顶部导航栏 -->
    <header class="main-header">
      <div class="header-container">
        <!-- 左侧导航链接 -->
        <div class="header-left">
          <div class="logo">
            <img src="@/assets/images/logo.png" alt="logo" />
            <span>Travel AI 推荐</span>
          </div>
          <nav class="primary-nav">
            <router-link to="/user/home" class="nav-item">首页</router-link>
            <router-link to="/user/items" class="nav-item">景点</router-link>
            <router-link to="/user/chat" class="nav-item">AI聊天</router-link>
          </nav>
        </div>

        <!-- 中间搜索框 -->
        <div class="header-center">
          <div class="search-box">
            <el-input
              v-model="searchText"
              placeholder="搜索景点..."
              @keyup.enter="handleSearch"
              clearable
            >
              <template #prefix>
                <el-dropdown trigger="click" @command="handleSearchModeChange">
                  <div class="search-mode">
                    {{ searchMode === 'title' ? '标题' : '标签' }}
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </div>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="title">标题</el-dropdown-item>
                      <el-dropdown-item command="tag">标签</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
              <template #suffix>
                <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
                
                <el-popover
                  placement="bottom"
                  :width="320"
                  trigger="click"
                  v-model:visible="showAdvancedSearch"
                  popper-class="advanced-search-popover"
                  :teleported="true"
                  :stop-popper-mouse-event="false"
                >
                  <template #reference>
                    <el-icon class="advanced-search-icon" @click.stop><Setting /></el-icon>
                  </template>
                  <div class="advanced-search-panel">
                    <h4>高级搜索</h4>
                    <el-form :model="advancedSearchForm" label-position="top">
                      <el-form-item label="景点名称">
                        <el-input v-model="advancedSearchForm.title" placeholder="输入景点名称"></el-input>
                      </el-form-item>
                      <el-form-item label="标签">
                        <el-input v-model="advancedSearchForm.tag" placeholder="输入标签关键词"></el-input>
                      </el-form-item>
                      <el-form-item label="分类">
                        <el-select v-model="advancedSearchForm.categoryId" placeholder="选择分类" clearable style="width: 100%">
                          <el-option 
                            v-for="category in popularCategories" 
                            :key="category.id" 
                            :label="category.name" 
                            :value="category.id"
                          />
                        </el-select>
                      </el-form-item>
                      <div class="advanced-search-actions">
                        <el-button type="primary" @click="handleAdvancedSearch">搜索</el-button>
                        <el-button @click="resetAdvancedSearch">重置</el-button>
                      </div>
                    </el-form>
                  </div>
                </el-popover>
              </template>
            </el-input>
          </div>
        </div>

        <!-- 右侧用户功能区 -->
        <div class="header-right">
          <AlgoHealthCheck class="health-check" />
          <div class="user-actions">
            <router-link to="/user/favorites" class="action-item">
              <el-icon><Star /></el-icon>
              <span>我的收藏</span>
            </router-link>
            <router-link to="/user/history/browsing" class="action-item">
              <el-icon><View /></el-icon>
              <span>历史记录</span>
            </router-link>
          </div>
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatarUrl || defaultAvatar">
                {{ userInfo?.username?.substring(0, 1) }}
              </el-avatar>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="purchases">预约记录</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin">管理控制台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 分类导航菜单 -->
    <nav class="category-nav">
      <div class="category-nav-container">
        <div class="primary-categories">
          <router-link to="/user/categories" class="category-item">
            <el-icon><Menu /></el-icon>
            <span>全部分类</span>
          </router-link>
          <!-- 动态分类项 -->
          <template v-for="category in popularCategories" :key="category.id">
            <router-link :to="`/user/category/${category.id}`" class="category-item">
              <el-image
                v-if="category.iconUrl"
                :src="category.iconUrl"
                class="category-icon"
              />
              <el-icon v-else><Grid /></el-icon>
              <span>{{ category.name }}</span>
            </router-link>
          </template>
        </div>
      </div>
    </nav>

    <!-- 主内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition name="fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>

    <!-- 返回顶部按钮 -->
    <el-backtop :right="20" :bottom="20" />

    <!-- 页脚 -->
    <footer class="main-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-logo">
            <img src="@/assets/images/logo.png" alt="logo" />
            <span>Travel AI 推荐系统</span>
          </div>
          <div class="footer-info">
            <p>© 2026 Travel AI 推荐系统 版权所有</p>
            <p>本站内容仅供学习交流使用，请勿用于商业用途</p>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AlgoHealthCheck from './AlgoHealthCheck.vue'
import { categoryApi } from '@/api/category'
import { Search, Star, View, User, House, Menu, ChatDotRound, Grid, ArrowDown, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const searchText = ref('')
const showAdvancedSearch = ref(false)
const searchMode = ref('title')
const advancedSearchForm = reactive({
  title: '',
  tag: '',
  categoryId: undefined as number | undefined
})

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 是否为管理员
const isAdmin = computed(() => userStore.isAdmin())

// 热门分类（限制显示数量）
const popularCategories = ref<any[]>([])

// 使用网络图片URL替代本地资源
const defaultAvatar = 'https://pic.imgdb.cn/item/65ae0c899f345e8d03301865.jpg'

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'purchases':
      router.push('/user/history/reservation')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      break
  }
}

// 处理搜索
const handleSearch = () => {
  if (searchText.value.trim()) {
    const query: Record<string, string | number> = {}
    
    // 根据搜索模式设置查询参数
    if (searchMode.value === 'title') {
      query.keyword = searchText.value.trim()
    } else if (searchMode.value === 'tag') {
      query.tag = searchText.value.trim()
    }
    
    router.push({
      path: '/user/items',
      query
    })
    
    // 关闭高级搜索面板
    showAdvancedSearch.value = false
  }
}

// 获取热门分类
const fetchPopularCategories = async () => {
  try {
    const res = await categoryApi.page({
      current: 1,
      size: 6
    })
    if (res && res.records) {
      popularCategories.value = res.records
    }
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

// 处理高级搜索
const handleAdvancedSearch = () => {
  const query: Record<string, string | number> = {}
  
  if (advancedSearchForm.title.trim()) {
    query.keyword = advancedSearchForm.title.trim()
  }
  
  if (advancedSearchForm.tag.trim()) {
    query.tag = advancedSearchForm.tag.trim()
  }
  
  if (advancedSearchForm.categoryId !== undefined) {
    query.categoryId = advancedSearchForm.categoryId
  }
  
  router.push({
    path: '/user/items',
    query
  })
  
  // 关闭高级搜索面板
  showAdvancedSearch.value = false
}

// 重置高级搜索
const resetAdvancedSearch = () => {
  advancedSearchForm.title = ''
  advancedSearchForm.tag = ''
  advancedSearchForm.categoryId = undefined
}

// 处理搜索模式变化
const handleSearchModeChange = (command: string) => {
  searchMode.value = command
}

// 手动监听路由变化，确保页面切换时关闭弹窗
watch(() => router.currentRoute.value.fullPath, () => {
  // 路由变化时关闭高级搜索面板
  showAdvancedSearch.value = false
})

// 添加全局事件监听，确保点击页面其他区域时关闭弹窗
const documentClickHandler = (e: MouseEvent) => {
  // 如果弹窗已打开，且点击的不是弹窗内的元素，则关闭弹窗
  if (showAdvancedSearch.value) {
    const popover = document.querySelector('.advanced-search-popover')
    const target = e.target as Node
    if (popover && !popover.contains(target)) {
      showAdvancedSearch.value = false
    }
  }
}

onMounted(() => {
  fetchPopularCategories()
  
  // 监听点击事件，确保页面其他区域点击时关闭弹窗
  document.addEventListener('click', documentClickHandler)
})

// 卸载组件时清理事件监听器
onUnmounted(() => {
  document.removeEventListener('click', documentClickHandler)
})
</script>

<style>
/* 全局样式修复 */
body {
  margin: 0;
  min-height: 100vh;
  overflow-y: auto !important;
}

.el-overlay {
  overflow: hidden;
  position: fixed;
}

.el-popup-parent--hidden {
  overflow: auto !important;
  padding-right: 0 !important;
}
</style>

<style scoped>
/* 原有样式 */
.user-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f4f5f7;
  position: relative;
  z-index: 1;
}

/* 顶部导航样式 */
.main-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 64px;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 30px;
  font-size: 22px;
  font-weight: bold;
  color: #409EFF;
}

.logo img {
  height: 32px;
  margin-right: 8px;
}

.primary-nav {
  display: flex;
  align-items: center;
}

.nav-item {
  padding: 0 15px;
  font-size: 16px;
  color: #333;
  text-decoration: none;
  height: 64px;
  display: flex;
  align-items: center;
  position: relative;
}

.nav-item:hover {
  color: #409EFF;
}

.nav-item.router-link-active {
  color: #409EFF;
  font-weight: 500;
}

.nav-item.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 15px;
  right: 15px;
  height: 3px;
  background-color: #409EFF;
  border-radius: 2px 2px 0 0;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 500px;
}

.search-box {
  width: 100%;
  margin: 0 20px;
}

.search-icon {
  cursor: pointer;
  color: #909399;
}

.search-icon:hover {
  color: #409EFF;
}

.advanced-search-icon {
  cursor: pointer;
  color: #909399;
  margin-left: 8px;
}

.advanced-search-icon:hover {
  color: #409EFF;
}

.search-mode {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
}

.advanced-search-panel {
  padding: 15px;
}

.advanced-search-panel h4 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.advanced-search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-actions {
  display: flex;
  margin-right: 20px;
}

.action-item {
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 12px;
  color: #606266;
  text-decoration: none;
}

.action-item:hover {
  color: #409EFF;
}

.action-item .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.user-info {
  cursor: pointer;
  padding: 0 8px;
}

.user-dropdown {
  display: flex;
  align-items: center;
}

/* 分类导航样式 */
.category-nav {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 10px 0;
  margin-top: 64px;
}

.category-nav-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 20px;
}

.primary-categories {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 20px;
}

.category-item {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #606266;
  padding: 6px 12px;
  border-radius: 16px;
  transition: all 0.3s;
}

.category-item:hover {
  background-color: #f0f2f5;
  color: #409EFF;
}

.category-item.router-link-active {
  background-color: #ecf5ff;
  color: #409EFF;
  font-weight: 500;
}

.category-item .el-icon, .category-icon {
  font-size: 18px;
  margin-right: 8px;
}

.category-icon {
  width: 18px;
  height: 18px;
  object-fit: cover;
  border-radius: 2px;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  max-width: 1600px;
  margin: 20px auto;
  padding: 0 20px;
  width: 100%;
}

@media (max-width: 768px) {
  .main-content {
    padding: 0 10px;
  }
}

/* 页脚样式 */
.main-footer {
  background-color: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 30px 0;
  margin-top: 40px;
}

.footer-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.footer-logo {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: bold;
  color: #606266;
}

.footer-logo img {
  height: 24px;
  margin-right: 8px;
}

.footer-info {
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 