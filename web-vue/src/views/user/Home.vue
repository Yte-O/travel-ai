<template>
  <div class="user-home">
    <section class="hero-grid">
      <article class="hero-main glass-card" @click="handleBannerClick(heroBanner)">
        <el-image :src="heroBanner.imageUrl" fit="cover" class="hero-image" />
        <div class="hero-mask">
          <p class="hero-kicker">AI TRAVEL CURATION</p>
          <h1>{{ heroBanner.title }}</h1>
          <span>从兴趣到路线，生成你自己的旅行节奏。</span>
        </div>
      </article>

      <aside class="hero-side glass-card">
        <div class="traveler">
          <el-avatar :size="56" :src="userInfo?.avatarUrl || defaultAvatar">
            {{ userInfo?.username?.substring(0, 1) }}
          </el-avatar>
          <div>
            <h3>{{ userInfo?.realName || userInfo?.username || '游客' }}</h3>
            <p>{{ userInfo?.role === 1 ? '管理员权限已开启' : '今日适合探索新的目的地' }}</p>
          </div>
        </div>

        <div class="stat-row">
          <div>
            <strong>{{ userStats.favorites || 0 }}</strong>
            <span>收藏</span>
          </div>
          <div>
            <strong>{{ userStats.history || 0 }}</strong>
            <span>浏览</span>
          </div>
          <div>
            <strong>{{ userStats.reservations || 0 }}</strong>
            <span>预约</span>
          </div>
        </div>

        <div class="recent-chat">
          <div class="recent-chat-head">
            <label>与 AI 最近对话</label>
            <el-button link type="primary" @click="goToChat">查看全部聊天</el-button>
          </div>

          <div v-if="recentMessages.length > 0" class="recent-chat-body">
            <div
              v-for="(message, index) in recentMessages"
              :key="`${message.id || 'temp'}-${index}`"
              class="recent-message"
              :class="message.role"
            >
              <span class="role-tag">{{ message.role === 'assistant' ? 'AI' : '我' }}</span>
              <p>{{ message.content }}</p>
            </div>
          </div>
          <el-empty v-else description="还没有最近对话，来聊聊行程吧" :image-size="50" />

          <div class="recent-chat-input">
            <el-input
              v-model="quickChatInput"
              placeholder="问问 AI：帮我安排一日游"
              :disabled="!canQuickChat || quickChatLoading"
              @keyup.enter="sendQuickChat"
            />
            <el-button
              type="primary"
              :loading="quickChatLoading"
              :disabled="!canQuickChat"
              @click="sendQuickChat"
            >
              发送
            </el-button>
          </div>
        </div>

        <el-button class="chat-entry" type="primary" round @click="goToChat">
          <el-icon><ChatDotRound /></el-icon>
          打开 AI 行程助手
        </el-button>
      </aside>
    </section>

    <section class="category-strip glass-card">
      <header>
        <h2>灵感分类</h2>
        <router-link to="/user/categories">查看全部</router-link>
      </header>
      <div class="category-track">
        <button
          v-for="category in popularCategories"
          :key="category.id"
          class="category-chip"
          @click="goToCategoryDetail(category.id)"
        >
          <el-image v-if="category.iconUrl" :src="category.iconUrl" class="chip-icon" />
          <el-icon v-else><Grid /></el-icon>
          <span>{{ category.name }}</span>
        </button>
      </div>
    </section>

    <section class="content-grid">
      <div class="recommend-river glass-card">
        <header class="block-head block-head--recommend">
          <h2>为你准备的推荐流</h2>
          <div class="recommend-actions">
            <div class="mode-switch mode-switch-inline">
              <label>推荐模式</label>
              <el-radio-group v-model="recommendationType" size="small" @change="fetchRecommendedItems">
                <el-radio-button value="user">相似用户</el-radio-button>
                <el-radio-button value="content">相似景点</el-radio-button>
              </el-radio-group>
            </div>
            <router-link to="/user/items">更多景点</router-link>
          </div>
        </header>

        <div v-if="recommendationsLoading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <template v-else>
          <el-empty v-if="recommendedItems.length === 0" description="暂无推荐景点" />

          <div v-else class="river-cards">
            <article
              v-for="(item, index) in recommendedItems"
              :key="item.id"
              class="river-card"
              :class="{ feature: index === 0 }"
              @click="goToItemDetail(item.id)"
            >
              <el-image :src="item.coverUrl || defaultCover" fit="cover" class="river-cover">
                <template #error>
                  <div class="image-placeholder"><el-icon><Picture /></el-icon></div>
                </template>
              </el-image>
              <div class="river-info">
                <h3>{{ item.title }}</h3>
                <p>{{ item.userRealName || item.username || '未知用户' }}</p>
                <div class="river-meta">
                  <span><el-icon><View /></el-icon>{{ formatNumber(item.views || 0) }}</span>
                  <span><el-icon><Star /></el-icon>{{ formatNumber(item.favorites || 0) }}</span>
                </div>
              </div>
            </article>
          </div>
        </template>
      </div>

      <aside class="rank-board glass-card">
        <header class="block-head">
          <h2>热度榜</h2>
          <router-link to="/user/items?sort=popular">完整榜单</router-link>
        </header>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="4" animated />
        </div>

        <template v-else>
          <el-empty v-if="topHotItems.length === 0" description="暂无热门景点" />

          <ol v-else class="rank-list">
            <li v-for="(item, index) in topHotItems" :key="item.id" @click="goToItemDetail(item.id)">
              <span class="rank-index">{{ index + 1 }}</span>
              <div class="rank-title">
                <strong>{{ item.title }}</strong>
                <p>{{ item.userRealName || '未知用户' }}</p>
              </div>
              <span class="rank-score">{{ formatNumber((item.views || 0) + (item.favorites || 0)) }}</span>
            </li>
          </ol>
        </template>
      </aside>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Picture, View, Star, Grid, ChatDotRound } from '@element-plus/icons-vue'
import { itemApi } from '@/api/item'
import { categoryApi } from '@/api/category'
import { favoriteApi } from '@/api/favorite'
import { pageMyActions, getItemViewCount } from '@/api/userAction'
import { recommendationApi } from '@/api/recommendation'
import { chatApi, llmApi } from '@/api/chat'
import { fileRequest } from '@/api/file_request'
import { ElMessage } from 'element-plus'
import type { ChatMessage } from '@/types/chat'

// 使用网络图片URL替代本地资源
const defaultAvatar = 'https://pic.imgdb.cn/item/65ae0c899f345e8d03301865.jpg'
const defaultCover = 'https://pic.imgdb.cn/item/65ae0c899f345e8d033018ab.jpg'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const recommendationsLoading = ref(false) // 单独的推荐加载状态
const userInfo = computed(() => userStore.userInfo)
const recommendationType = ref('user') // 推荐类型：相似用户或相似景点
const quickChatSessionId = ref<number | null>(null)
const recentMessages = ref<ChatMessage[]>([])
const quickChatInput = ref('')
const quickChatLoading = ref(false)
const quickChatModel = 'qwen-turbo'
const canQuickChat = computed(() => !!userStore.userInfo?.id)

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
const heroBanner = computed(() => banners.value[0])
const topHotItems = computed(() => popularItems.value.slice(0, 6))

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

const ensureQuickChatSession = async () => {
  if (quickChatSessionId.value) return quickChatSessionId.value

  const sessions = await chatApi.getSessions()
  if (sessions.length > 0) {
    quickChatSessionId.value = sessions[0].id
    return quickChatSessionId.value
  }

  const timestamp = new Date().toLocaleString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  })
  const newSession = await chatApi.createSession({
    sessionName: `首页快速对话 (${timestamp})`
  })
  quickChatSessionId.value = newSession.id
  return quickChatSessionId.value
}

const loadRecentChatMessages = async () => {
  if (!canQuickChat.value) {
    recentMessages.value = []
    return
  }

  try {
    const sessionId = await ensureQuickChatSession()
    const messages = await chatApi.getMessages({ sessionId })
    recentMessages.value = messages.slice(-4)
  } catch (error) {
    console.error('加载最近对话失败', error)
  }
}

const sendQuickChat = async () => {
  if (quickChatLoading.value) return

  if (!canQuickChat.value) {
    ElMessage.warning('请先登录后再使用对话功能')
    return
  }

  const content = quickChatInput.value.trim()
  if (!content) return

  quickChatLoading.value = true
  try {
    const sessionId = await ensureQuickChatSession()
    const userMessage: ChatMessage = {
      sessionId,
      role: 'user',
      content,
      model: quickChatModel,
      messageTime: new Date().toISOString()
    }

    recentMessages.value = [...recentMessages.value, userMessage].slice(-4)
    quickChatInput.value = ''

    const aiDbMessage = await chatApi.sendMessage({
      sessionId,
      content,
      model: quickChatModel
    })

    const llmResponse = await llmApi.chat(quickChatModel, recentMessages.value)

    if (aiDbMessage.id) {
      await chatApi.updateMessageContent(aiDbMessage.id, llmResponse)
    }

    const assistantMessage: ChatMessage = {
      id: aiDbMessage.id,
      sessionId,
      role: 'assistant',
      content: llmResponse,
      model: quickChatModel,
      messageTime: aiDbMessage.messageTime
    }
    recentMessages.value = [...recentMessages.value, assistantMessage].slice(-4)
    document.dispatchEvent(new CustomEvent('refresh-session-list'))
  } catch (error) {
    console.error('首页快速对话失败', error)
    ElMessage.error('发送失败，请稍后重试')
    await loadRecentChatMessages()
  } finally {
    quickChatLoading.value = false
  }
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
      fetchUserStats(),
      loadRecentChatMessages()
    ])
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.user-home {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, 0.9fr);
  gap: 14px;
}

.hero-main {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  min-height: 320px;
  border: 1px solid rgba(30, 45, 30, 0.08);
}

.hero-image {
  width: 100%;
  height: 100%;
  filter: saturate(1.08);
}

.hero-mask {
  position: absolute;
  inset: auto 0 0;
  padding: 22px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(6, 15, 10, 0.74) 78%);
  color: #fff;
}

.hero-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 1.1px;
  opacity: 0.85;
}

.hero-mask h1 {
  margin: 8px 0 8px;
  font-size: clamp(24px, 3vw, 38px);
  line-height: 1.12;
}

.hero-mask span {
  display: block;
  font-size: 14px;
  opacity: 0.92;
}

.hero-side {
  border-radius: 20px;
  border: 1px solid rgba(30, 45, 30, 0.08);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.traveler {
  display: flex;
  align-items: center;
  gap: 10px;
}

.traveler h3 {
  margin: 0;
  font-size: 18px;
}

.traveler p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #5c6b5c;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.stat-row div {
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(18, 34, 20, 0.08);
  border-radius: 12px;
  text-align: center;
  padding: 8px;
}

.stat-row strong {
  display: block;
  font-size: 18px;
}

.stat-row span {
  font-size: 12px;
  color: #617161;
}

.mode-switch {
  display: grid;
  gap: 8px;
}

.mode-switch-inline {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mode-switch label {
  font-size: 12px;
  color: #5d6f5d;
  font-weight: 600;
}

.chat-entry {
  margin-top: 8px;
}

.recent-chat {
  border: 1px solid rgba(18, 34, 20, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  padding: 10px;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  gap: 8px;
}

.recent-chat-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recent-chat-head label {
  font-size: 12px;
  color: #5d6f5d;
  font-weight: 600;
}

.recent-chat-body {
  flex: 1;
  min-height: 170px;
  overflow-y: auto;
  display: grid;
  gap: 6px;
}

.recent-message {
  border: 1px solid rgba(20, 34, 20, 0.08);
  border-radius: 10px;
  padding: 6px 8px;
  background: #fff;
}

.recent-message.user {
  background: #edf8f6;
}

.recent-message.assistant {
  background: #f7f7ff;
}

.role-tag {
  display: inline-block;
  font-size: 11px;
  color: #2f5d56;
  font-weight: 700;
  margin-bottom: 2px;
}

.recent-message p {
  margin: 0;
  font-size: 12px;
  color: #334433;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.recent-chat-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
}

.category-strip,
.recommend-river,
.rank-board {
  border-radius: 20px;
  border: 1px solid rgba(24, 37, 24, 0.08);
  padding: 14px;
}

.category-strip header,
.block-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.block-head--recommend {
  gap: 10px;
  flex-wrap: wrap;
}

.recommend-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.category-strip h2,
.block-head h2 {
  margin: 0;
  font-size: 18px;
}

.category-strip a,
.block-head a {
  color: #12584d;
  font-size: 13px;
  font-weight: 600;
}

.category-track {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.category-chip {
  border: 1px solid rgba(22, 37, 22, 0.08);
  background: rgba(255, 255, 255, 0.58);
  border-radius: 999px;
  height: 38px;
  padding: 0 12px;
  white-space: nowrap;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
}

.category-chip:hover {
  background: #145a4f;
  color: #fff;
}

.chip-icon {
  width: 18px;
  height: 18px;
  border-radius: 3px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, 0.9fr);
  gap: 14px;
}

.river-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.river-card {
  border: 1px solid rgba(22, 34, 22, 0.08);
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.58);
  transition: transform 0.2s;
}

.river-card:hover {
  transform: translateY(-2px);
}

.river-card.feature {
  grid-column: span 2;
}

.river-cover {
  width: 100%;
  height: 150px;
}

.river-card.feature .river-cover {
  height: 210px;
}

.river-info {
  padding: 10px;
}

.river-info h3 {
  margin: 0;
  font-size: 15px;
  line-height: 1.3;
}

.river-info p {
  margin: 6px 0 0;
  font-size: 12px;
  color: #657565;
}

.river-meta {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #556555;
}

.river-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rank-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 8px;
}

.rank-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 54px;
  border: 1px solid rgba(20, 34, 20, 0.08);
  border-radius: 12px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.56);
  cursor: pointer;
  transition: all 0.2s;
}

.rank-list li:hover {
  transform: translateX(2px);
}

.rank-index {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #e9f3ff;
  color: #1f5b9d;
  font-weight: 700;
}

.rank-title {
  flex: 1;
  min-width: 0;
}

.rank-title strong {
  display: block;
  font-size: 14px;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-title p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #677667;
}

.rank-score {
  font-size: 13px;
  color: #2f5d56;
  font-weight: 600;
}

.loading-container {
  padding: 20px 0;
}

@media (max-width: 1200px) {
  .hero-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .river-cards {
    grid-template-columns: 1fr;
  }

  .river-card.feature {
    grid-column: span 1;
  }
}

@media (max-width: 576px) {
  .hero-main {
    min-height: 260px;
  }

  .hero-mask h1 {
    font-size: 24px;
  }

  .stat-row {
    grid-template-columns: 1fr;
  }

  .recent-chat-input {
    grid-template-columns: 1fr;
  }

  .mode-switch-inline {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style> 