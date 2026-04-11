<template>
  <div class="user-layout">
    <div class="bg-orb orb-1"></div>
    <div class="bg-orb orb-2"></div>

    <header class="main-header glass-card">
      <div class="header-top">
        <div class="brand-block">
          <img src="@/assets/images/logo.png" alt="logo" />
          <div class="brand-meta">
            <strong>TRAVEL AI</strong>
            <span>探索 · 洞察 · 推荐</span>
          </div>
        </div>

        <nav class="primary-nav">
          <router-link to="/user/home" class="nav-pill">首页</router-link>
          <router-link to="/user/items" class="nav-pill">景点</router-link>
          <router-link to="/user/chat" class="nav-pill">AI行程</router-link>
        </nav>

        <div class="right-tools">
          <AlgoHealthCheck class="health-check" />
          <router-link to="/user/favorites" class="quick-link">
            <el-icon><Star /></el-icon>
            收藏
          </router-link>
          <router-link to="/user/history/browsing" class="quick-link">
            <el-icon><View /></el-icon>
            足迹
          </router-link>
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-chip">
              <el-avatar :size="34" :src="userInfo?.avatarUrl || defaultAvatar">
                {{ userInfo?.username?.substring(0, 1) }}
              </el-avatar>
              <span>{{ userInfo?.realName || userInfo?.username || '游客' }}</span>
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

      <div class="search-row">
        <div class="search-box">
          <el-input
            v-model="searchText"
            placeholder="输入目的地、主题或标签，开始你的旅行灵感..."
            @keyup.enter="handleSearch"
            clearable
            size="large"
          >
            <template #prefix>
              <el-dropdown trigger="click" @command="handleSearchModeChange">
                <div class="search-mode">
                  {{ searchMode === 'title' ? '按标题' : '按标签' }}
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="title">按标题</el-dropdown-item>
                    <el-dropdown-item command="tag">按标签</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template #suffix>
              <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
              <el-popover
                placement="bottom"
                :width="340"
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
    </header>

    <nav class="category-nav glass-card">
      <router-link to="/user/categories" class="category-item is-primary">
        <el-icon><Menu /></el-icon>
        <span>全部分类</span>
      </router-link>
      <template v-for="category in popularCategories" :key="category.id">
        <router-link :to="`/user/category/${category.id}`" class="category-item">
          <el-image v-if="category.iconUrl" :src="category.iconUrl" class="category-icon" />
          <el-icon v-else><Grid /></el-icon>
          <span>{{ category.name }}</span>
        </router-link>
      </template>
    </nav>

    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition name="fade-up" mode="out-in">
          <component :is="Component" :key="route.fullPath" class="section-enter" />
        </transition>
      </router-view>
    </main>

    <el-backtop :right="24" :bottom="26" />

    <footer class="main-footer glass-card">
      <div class="footer-left">
        <img src="@/assets/images/logo.png" alt="logo" />
        <div>
          <strong>Travel AI 推荐系统</strong>
          <p>让每次出行都更像被理解，而不只是被搜索。</p>
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
import { Search, Star, View, Menu, Grid, ArrowDown, Setting } from '@element-plus/icons-vue'

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
body {
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
.user-layout {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  padding: 14px 16px 12px;
  z-index: 1;
}

.bg-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(62px);
  z-index: -1;
  pointer-events: none;
  opacity: 0.62;
}

.orb-1 {
  width: 380px;
  height: 380px;
  background: #ffe2ba;
  top: -140px;
  left: -120px;
}

.orb-2 {
  width: 460px;
  height: 460px;
  background: #c4f4ec;
  bottom: -220px;
  right: -140px;
}

.main-header {
  position: sticky;
  top: 10px;
  z-index: 15;
  border-radius: 22px;
  border: 1px solid rgba(26, 38, 26, 0.08);
  padding: 14px;
}

.header-top {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
  align-items: center;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.brand-block img {
  width: 34px;
  height: 34px;
}

.brand-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.brand-meta strong {
  font-size: 16px;
  letter-spacing: 0.8px;
}

.brand-meta span {
  margin-top: 2px;
  font-size: 11px;
  color: #627062;
}

.primary-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.nav-pill {
  display: flex;
  align-items: center;
  height: 38px;
  padding: 0 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(20, 39, 28, 0.08);
  font-weight: 600;
  color: #334033;
  transition: all 0.2s;
}

.nav-pill:hover,
.nav-pill.router-link-active {
  background: #184f45;
  color: #f7fffc;
}

.right-tools {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.quick-link {
  height: 34px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border-radius: 10px;
  color: #425442;
  background: rgba(255, 255, 255, 0.52);
  font-size: 13px;
  transition: all 0.2s;
}

.quick-link:hover {
  background: #184f45;
  color: #fff;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  min-height: 38px;
  border-radius: 12px;
  padding: 2px 8px 2px 2px;
  background: rgba(255, 255, 255, 0.64);
}

.user-chip span {
  font-size: 13px;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.search-row {
  margin-top: 12px;
}

.search-box {
  width: 100%;
}

.search-mode {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
  color: #4b5c4b;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
}

.search-icon,
.advanced-search-icon {
  cursor: pointer;
  color: #798979;
  transition: color 0.2s;
}

.search-icon:hover,
.advanced-search-icon:hover {
  color: #145e51;
}

.advanced-search-panel {
  padding: 10px 12px;
}

.advanced-search-panel h4 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #2f3f2f;
  font-size: 16px;
  border-bottom: 1px solid #e4ece4;
  padding-bottom: 10px;
}

.advanced-search-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding: 10px;
  border-radius: 18px;
  border: 1px solid rgba(26, 41, 31, 0.08);
}

.category-item {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: #415441;
  padding: 7px 12px;
  border-radius: 999px;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.54);
  border: 1px solid rgba(20, 40, 26, 0.07);
}

.category-item:hover {
  background: #1f5f53;
  color: #fff;
}

.category-item.is-primary {
  background: #ef7b3f;
  color: #fff;
  border-color: transparent;
}

.category-item.router-link-active {
  background: #145f53;
  color: #fff;
  font-weight: 600;
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

.main-content {
  flex: 1;
  max-width: none;
  margin: 16px 0 0;
  padding: 0;
  width: 100%;
}

.main-footer {
  margin-top: 18px;
  min-height: 74px;
  border-radius: 18px;
  border: 1px solid rgba(26, 36, 26, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.footer-left img {
  width: 30px;
  height: 30px;
}

.footer-left strong {
  font-size: 14px;
}

.footer-left p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #627062;
}

.footer-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-size: 12px;
  color: #5f6f5f;
}

.fade-up-enter-active,
.fade-up-leave-active {
  transition: all 0.24s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 1180px) {
  .header-top {
    grid-template-columns: 1fr;
  }

  .primary-nav,
  .right-tools {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .main-content {
    margin-top: 14px;
  }
}

@media (max-width: 768px) {
  .user-layout {
    padding: 10px;
  }

  .main-header {
    top: 6px;
    padding: 10px;
  }

  .brand-meta span {
    display: none;
  }

  .footer-right {
    display: none;
  }
}
</style> 