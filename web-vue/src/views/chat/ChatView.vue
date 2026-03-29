<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatApi } from '@/api/chat'
// 导入类型和组件
import type { ChatSession as ChatSessionType } from '@/types/chat'
import SessionList from './SessionList.vue'
import ChatSession from './ChatSession.vue'

const router = useRouter()
const route = useRoute()

// 状态
const currentSessionId = ref<number | null>(null)
const currentSession = ref<ChatSessionType | null>(null)
const loading = ref(false)

// 是否在小屏幕上显示会话列表
const showSessionList = ref(window.innerWidth > 768)

// 是否在小屏幕设备
const isMobileDevice = ref(window.innerWidth <= 768)

// 计算样式
const containerStyle = computed(() => {
  return {
    'chat-container': true,
    'show-sessions': showSessionList.value
  }
})

// 会话选择
const handleSelectSession = async (sessionId: number) => {
  currentSessionId.value = sessionId
  
  // 更新URL但不重新加载页面
  router.replace(`/user/chat?sessionId=${sessionId}`)
  
  // 在小屏幕上选择会话后隐藏会话列表
  if (isMobileDevice.value) {
    showSessionList.value = false
  }
}

// 会话加载完成处理
const handleSessionLoaded = (session: ChatSessionType) => {
  currentSession.value = session
}

// 创建新会话
const handleCreateSession = async () => {
  try {
    loading.value = true
    
    // 生成包含时间戳的会话名称，避免重名
    const timestamp = new Date().toLocaleString('zh-CN', {
      month: 'numeric',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric'
    })
    const sessionName = `新的聊天 (${timestamp})`
    
    const newSession = await chatApi.createSession({
      sessionName: sessionName
    })
    
    // 选择新创建的会话
    handleSelectSession(newSession.id)
    
    // 通知重新加载会话列表 (添加一个自定义事件)
    document.dispatchEvent(new CustomEvent('refresh-session-list'))
  } catch (error) {
    ElMessage.error('创建新会话失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 删除会话
const handleDeleteSession = async (session: ChatSessionType) => {
  try {
    loading.value = true
    await chatApi.deleteSession(session.id)
    ElMessage.success('删除成功')
    
    // 如果删除的是当前会话，清空当前会话
    if (currentSessionId.value === session.id) {
      currentSessionId.value = null
      currentSession.value = null
      
      // 请求重新加载会话列表
      await fetchAndSelectNewSession()
    }
  } catch (error) {
    ElMessage.error('删除会话失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取并选择新会话
const fetchAndSelectNewSession = async () => {
  try {
    // 获取最新的会话列表
    const sessions = await chatApi.getSessions()
    
    // 如果还有会话，自动选择第一个
    if (sessions.length > 0) {
      handleSelectSession(sessions[0].id)
    } else {
      // 如果没有会话了，更新URL
      router.replace('/user/chat')
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

// 返回按钮处理
const handleBack = () => {
  showSessionList.value = true
}

// 切换会话列表显示状态
const toggleSessionList = () => {
  showSessionList.value = !showSessionList.value
}

// 会话列表加载完成
const handleSessionsLoaded = (sessions: ChatSessionType[]) => {
  // 如果当前没有选中的会话，但有会话列表，自动选择第一个
  if (!currentSessionId.value && sessions.length > 0) {
    handleSelectSession(sessions[0].id)
  }
  
  // 注意：如果我们刚刚创建了一个新会话，我们期望它已经被选中了
  // 所以这里不需要额外的处理
}

// 清除消息后的处理
const handleClearMessages = async () => {
  // 清除消息后也刷新一下会话列表，因为最新消息可能发生变化
  document.dispatchEvent(new CustomEvent('refresh-session-list'))
}

// 监听窗口大小变化
const handleResize = () => {
  isMobileDevice.value = window.innerWidth <= 768
  if (window.innerWidth > 768) {
    showSessionList.value = true
  }
}

// 页面加载时初始化数据
onMounted(() => {
  // 检查URL中是否有sessionId
  const sessionIdParam = route.query.sessionId
  
  if (sessionIdParam) {
    // 如果URL中有sessionId，则加载该会话
    handleSelectSession(Number(sessionIdParam))
  }
  
  // 添加窗口大小变化监听器
  window.addEventListener('resize', handleResize)
  
  // 添加刷新会话监听器
  document.addEventListener('refresh-session-list', () => {
    console.log('收到刷新会话列表事件')
  })
})
</script>

<template>
  <div :class="containerStyle">
    <!-- 会话列表组件 -->
    <SessionList 
      v-show="showSessionList"
      :current-session-id="currentSessionId"
      :loading="loading"
      @select-session="handleSelectSession"
      @create-session="handleCreateSession"
      @delete-session="handleDeleteSession"
      @sessions-loaded="handleSessionsLoaded"
    />
    
    <!-- 聊天会话组件 -->
    <ChatSession
      :session-id="currentSessionId"
      :show-back-button="isMobileDevice"
      @back="handleBack"
      @create-session="handleCreateSession"
      @clear-messages="handleClearMessages"
      @session-loaded="handleSessionLoaded"
    >
      <template #toggle-button>
        <button @click="toggleSessionList" class="toggle-btn">
          {{ showSessionList ? '隐藏会话' : '显示会话' }}
        </button>
      </template>
    </ChatSession>
  </div>
</template>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 170px); /* 调整高度，留出更多空间给导航栏和页脚 */
  overflow: hidden;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  width: 100%;
  margin-top: 20px;
}

/* 添加toggle-btn按钮样式 */
.toggle-btn {
  display: none;
  padding: 6px 12px;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background-color: #e0e0e0;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .chat-container.show-sessions .chat-session {
    display: none;
  }
  
  .chat-container:not(.show-sessions) .session-list {
    display: none;
  }

  .toggle-btn {
    display: block;
  }
}
</style> 