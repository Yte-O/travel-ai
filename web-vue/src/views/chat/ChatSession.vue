<script setup lang="ts">
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { ElMessage, ElButton, ElMessageBox, ElSwitch, ElTooltip } from 'element-plus'
import { Plus, ArrowLeft } from '@element-plus/icons-vue'
import type { ChatMessage, ChatSession } from '@/types/chat'
import type { RecommendedItemVO } from '@/types/item'
import { chatApi, llmApi } from '@/api/chat'
import { recommendationApi } from '@/api/recommendation'
import { useUserStore } from '@/stores/user'
import ChatMessageComponent from '@/components/chat/ChatMessage.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import ModelSelector from '@/components/chat/ModelSelector.vue'

// 组件属性
const props = defineProps<{
  sessionId?: number | null;
  showBackButton?: boolean;
}>();

// 组件事件
const emits = defineEmits<{
  'back': [];
  'create-session': [];
  'clear-messages': [];
  'session-loaded': [session: ChatSession];
}>();

const session = ref<ChatSession | null>(null)
const messages = ref<ChatMessage[]>([])
const loading = ref(false)
const selectedModel = ref('qwen-turbo')
const messagesContainer = ref<HTMLElement | null>(null)
const showRecommendations = ref(false)
const recommendedItems = ref<RecommendedItemVO[]>([])
const recommendKeywords = ref<string[]>([])
const isLoadingRecommendations = ref(false)
const userStore = useUserStore()
const recommendationMessage = ref('')
const isHistoryItems = ref(false)
const enableGraphRAG = ref(true)

// 加载会话信息
const loadSession = async () => {
  if (!props.sessionId) return;
  
  try {
    session.value = await chatApi.getSession(props.sessionId)
    emits('session-loaded', session.value)
  } catch (error) {
    ElMessage.error('加载会话信息失败')
    console.error(error)
  }
}

// 加载消息历史
const loadMessages = async () => {
  if (!props.sessionId) return;
  
  try {
    messages.value = await chatApi.getMessages({ sessionId: props.sessionId })
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载消息历史失败')
    console.error(error)
  }
}

// 发送消息
const sendMessage = async (content: string) => {
  if (loading.value || !content.trim() || !props.sessionId) return
  
  // 检查是否是推荐请求
  const isRecommendRequest = content.toLowerCase().includes('给我推荐一些')
  
  const userMessage: ChatMessage = {
    sessionId: props.sessionId,
    role: 'user',
    content,
    model: selectedModel.value
  }
  
  // 添加用户消息并立即更新视图
  messages.value = [...messages.value, userMessage]
  await nextTick()
  scrollToBottom()
  
  // 用于可能需要回滚的临时消息索引
  const tempMessageIndex = messages.value.length - 1
  
  try {
    loading.value = true
    
    if (isRecommendRequest) {
      // 每次推荐请求前清空之前的推荐内容
      recommendedItems.value = []
      recommendKeywords.value = []
      recommendationMessage.value = ''
      showRecommendations.value = false
      
      // 处理推荐请求 - 不存储到数据库，仅在前端展示
      try {
        // 添加助手回复消息到界面但不发送到后端
        const tempMessage: ChatMessage = {
          sessionId: props.sessionId,
          role: 'assistant',
          content: '正在为您寻找相关旅游景点推荐...',
          model: selectedModel.value,
          messageTime: new Date().toISOString()
        }
        
        // 添加AI消息并立即更新视图
        messages.value = [...messages.value, tempMessage]
        await nextTick()
        scrollToBottom()
        
        // 直接获取推荐
        await handleRecommendation()
      } catch (error) {
        console.error('旅游景点推荐失败:', error)
        
        // 添加错误提示
        const errorMessage: ChatMessage = {
          sessionId: props.sessionId,
          role: 'assistant',
          content: '抱歉，旅游景点推荐功能暂时不可用，请稍后再试。',
          model: selectedModel.value,
          messageTime: new Date().toISOString()
        }
        messages.value[messages.value.length - 1] = errorMessage
        messages.value = [...messages.value]
      }
    } else {
      // 如果是普通聊天，确保推荐内容被清空
      recommendedItems.value = []
      recommendKeywords.value = []
      recommendationMessage.value = ''
      showRecommendations.value = false
      
      // 处理普通聊天请求 - 存储到数据库
      // 后端返回的是AI消息，而不是用户消息
      const aiResponse = await chatApi.sendMessage({
        sessionId: props.sessionId,
        content,
        model: selectedModel.value
      })
      
      // 显示AI正在输入的消息
      const tempMessage: ChatMessage = {
        id: aiResponse.id, // 使用后端返回的AI消息ID
        sessionId: props.sessionId,
        role: 'assistant',
        content: '正在思考...',
        model: selectedModel.value,
        messageTime: aiResponse.messageTime
      }
      
      // 添加AI消息并立即更新视图
      messages.value = [...messages.value, tempMessage]
      await nextTick()
      scrollToBottom()
      
      // 根据GraphRAG开关选择不同的响应处理方式
      try {
        if (enableGraphRAG.value) {
          await handleGraphRAGResponse(tempMessage)
        } else {
          await handleNormalResponse(tempMessage)
        }
        
        // 通知刷新会话列表，因为最新消息已经更新
        document.dispatchEvent(new CustomEvent('refresh-session-list'))
      } catch (responseError) {
        console.error('获取AI响应失败:', responseError)
        // 如果AI响应失败但我们已经创建了消息，就不要删除用户的消息，只需更新AI消息状态
        const updatedMessage = { ...tempMessage }
        updatedMessage.content = '抱歉，AI响应生成失败，请重试。'
        
        // 找到并替换消息数组中的消息对象
        const index = messages.value.findIndex(msg => msg.id === tempMessage.id)
        if (index !== -1) {
          messages.value[index] = updatedMessage
          messages.value = [...messages.value]
        }
      }
    }
  } catch (error) {
    console.error('处理消息失败:', error)
    ElMessage.error('处理消息失败')
    
    // 发送失败，回滚用户消息
    messages.value.splice(tempMessageIndex)
    messages.value = [...messages.value]
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 处理普通响应
const handleNormalResponse = async (tempMessage: ChatMessage) => {
  try {
    // 获取所有对话历史记录，用于发送给LLM
    const historyMessages = messages.value.slice(0, messages.value.length - 1)
    
    // 调用LLM接口
    const response = await llmApi.chat(selectedModel.value, historyMessages)
    
    // 更新临时消息
    const updatedMessage = { ...tempMessage }
    updatedMessage.content = response
    
    // 更新原始消息对象
    tempMessage.content = response
    
    // 找到并替换消息数组中的消息对象
    const index = messages.value.findIndex(msg => msg.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index] = updatedMessage
      // 强制Vue刷新视图
      messages.value = [...messages.value]
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }
    
    // 更新消息到数据库
    await updateAiMessageInDatabase(tempMessage.id!, response)
  } catch (error) {
    console.error('获取LLM响应失败:', error)
    
    const updatedMessage = { ...tempMessage }
    updatedMessage.content = '抱歉，发生了错误，请重试。'
    
    // 更新原始消息对象
    tempMessage.content = updatedMessage.content
    
    // 找到并替换消息数组中的消息对象
    const index = messages.value.findIndex(msg => msg.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index] = updatedMessage
      // 强制Vue刷新视图
      messages.value = [...messages.value]
    }
    
    throw error
  }
}

// 处理GraphRAG响应
const handleGraphRAGResponse = async (tempMessage: ChatMessage) => {
  try {
    console.log('GraphRAG模式已启用，正在搜索相关内容...')
    
    // 获取所有对话历史记录，用于发送给LLM
    const historyMessages = messages.value.slice(0, messages.value.length - 1)
    
    // 调用GraphRAG增强聊天接口
    const result = await llmApi.chatWithGraphRAG(selectedModel.value, historyMessages)
    
    // 处理搜索信息并构建显示内容
    let displayContent = result.response
    
    // 如果有搜索信息，添加到回复前面
    if (result.searchInfo) {
      const { keywords, search_status, message } = result.searchInfo
      let searchInfoText = ''
      
      if (search_status === 'no_keywords') {
        searchInfoText = `🔍 **搜索信息**: ${message}\n\n`
      } else if (search_status === 'not_found') {
        searchInfoText = `🔍 **搜索信息**: ${message}\n📝 **搜索关键词**: ${keywords.join(', ')}\n\n`
      } else if (search_status === 'found') {
        searchInfoText = `🔍 **搜索信息**: ${message}\n📝 **搜索关键词**: ${keywords.join(', ')}\n\n`
      }
      
      displayContent = searchInfoText + result.response
    }
    
    // 更新临时消息
    const updatedMessage = { ...tempMessage }
    updatedMessage.content = displayContent
    
    // 更新原始消息对象
    tempMessage.content = displayContent
    
    // 找到并替换消息数组中的消息对象
    const index = messages.value.findIndex(msg => msg.id === tempMessage.id)
    if (index !== -1) {
      messages.value[index] = updatedMessage
      // 强制Vue刷新视图
      messages.value = [...messages.value]
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }
    
    // 更新消息到数据库
    await updateAiMessageInDatabase(tempMessage.id!, displayContent)
  } catch (error) {
    console.error('GraphRAG响应失败，回退到普通模式:', error)
    // 如果GraphRAG失败，回退到普通响应
    await handleNormalResponse(tempMessage)
  }
}

// 更新AI消息内容到数据库
const updateAiMessageInDatabase = async (messageId: number, content: string) => {
  try {
    if (!messageId) {
      console.error('更新AI消息到数据库失败: 消息ID未定义')
      return
    }
    await chatApi.updateMessageContent(messageId, content)
  } catch (error) {
    console.error('更新AI消息到数据库失败:', error)
  }
}

// 修改推荐处理方法
const handleRecommendation = async () => {
  try {
    // 再次确保之前的推荐内容被清空
    recommendedItems.value = []
    recommendKeywords.value = []
    recommendationMessage.value = ''
    isHistoryItems.value = false
    
    isLoadingRecommendations.value = true
    showRecommendations.value = false // 先隐藏推荐区域，等有结果再显示
    
    // 检查用户是否登录
    if (!userStore.isLoggedIn() || !userStore.userInfo?.id) {
      ElMessage.warning('请先登录后再使用旅游景点推荐功能')
      isLoadingRecommendations.value = false
      
      // 更新最后一条消息
      if (messages.value.length > 0) {
        const lastIndex = messages.value.length - 1
        const lastMessage = { ...messages.value[lastIndex] }
        lastMessage.content = '请先登录后再使用旅游景点推荐功能'
        messages.value[lastIndex] = lastMessage
        messages.value = [...messages.value]
      }
      return
    }
    
    // 使用userStore获取用户ID
    const userId = userStore.userInfo.id
    
    // 获取推荐
    const result = await recommendationApi.getFromChat(
      messages.value.map(msg => ({ role: msg.role, content: msg.content })),
      userId,
      selectedModel.value
    )
    
    // 设置推荐结果
    recommendedItems.value = result.items
    recommendKeywords.value = result.keywords
    recommendationMessage.value = result.message || ''
    isHistoryItems.value = result.isHistoryItems || false
    
    // 更新最后一条消息内容为推荐结果的说明
    const lastIndex = messages.value.length - 1
    if (lastIndex >= 0) {
      const lastMessage = { ...messages.value[lastIndex] }
      
      // 如果没有推荐结果，显示提示
      if (!result.items || result.items.length === 0) {
        const noRecommendMsg = result.keywords.length > 0 
          ? `没有找到与"${result.keywords.join('、')}"相关的新旅游景点，为您推荐了一些您可能感兴趣的景点` 
          : '未能从对话中提取到有效的旅游景点关键词，为您推荐一些您可能感兴趣的景点'
        
        lastMessage.content = noRecommendMsg
        messages.value[lastIndex] = lastMessage
        messages.value = [...messages.value]
        return
      }
      
      // 有推荐结果，更新消息并显示推荐区域
      const keywordsInfo = isHistoryItems.value
        ? (result.message || '为您推荐您可能感兴趣的旅游景点') // 使用后端返回的消息，提供默认值
        : `根据关键词: ${result.keywords.join(', ')}，为您找到了${result.items.length}个旅游景点推荐`
      
      lastMessage.content = keywordsInfo
      messages.value[lastIndex] = lastMessage
      messages.value = [...messages.value]
      
      // 只有在有推荐结果时才显示推荐区域
      showRecommendations.value = true
    }
  } catch (error) {
    console.error('获取旅游景点推荐失败:', error)
    
    // 更新最后一条消息
    if (messages.value.length > 0) {
      const lastIndex = messages.value.length - 1
      const lastMessage = { ...messages.value[lastIndex] }
      
      // 错误处理，区分未登录和其他错误
      if (error && typeof error === 'object' && 'response' in error && 
          error.response && typeof error.response === 'object' && 'data' in error.response && 
          error.response.data && typeof error.response.data === 'object' && 'msg' in error.response.data && 
          error.response.data.msg === '用户ID不能为空') {
        lastMessage.content = '请先登录后再使用旅游景点推荐功能'
        ElMessage.warning('请先登录后再使用旅游景点推荐功能')
      } else {
        lastMessage.content = '获取旅游景点推荐失败，请稍后再试'
        ElMessage.error('获取旅游景点推荐失败')
      }
      
      messages.value[lastIndex] = lastMessage
      messages.value = [...messages.value]
    }
  } finally {
    isLoadingRecommendations.value = false
  }
}

// 添加前往景点详情页的方法
const goToItemDetail = (itemId: number) => {
  // 使用window.open在新标签页打开
  window.open(`/user/item/${itemId}`, '_blank')
}

// 返回会话列表
const goBack = () => {
  emits('back')
}

// 创建新会话
const createNewSession = () => {
  emits('create-session')
}

// 清除聊天记录
const clearMessages = async () => {
  if (!props.sessionId) return
  
  try {
    await ElMessageBox.confirm('确定要清空所有聊天记录吗？此操作不可恢复', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    await chatApi.clearSessionMessages(props.sessionId)
    messages.value = []
    ElMessage.success('聊天记录已清空')
    
    // 通知父组件清除消息完成
    emits('clear-messages')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空聊天记录失败')
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听会话ID变化
watch(() => props.sessionId, async (newId, oldId) => {
  if (newId && newId !== oldId) {
    await loadSession();
    await loadMessages();
  }
}, { immediate: true });

// 监听会话详情刷新事件
const handleRefreshSessionDetail = (event: CustomEvent) => {
  const refreshSessionId = event.detail
  if (refreshSessionId === props.sessionId) {
    loadSession()
  }
}

// 页面加载时初始化数据
onMounted(async () => {
  if (props.sessionId) {
    await loadSession();
    await loadMessages();
  }
  
  // 添加自定义事件监听器
  document.addEventListener('refresh-session-detail', handleRefreshSessionDetail as EventListener)
})

// 组件卸载时移除事件监听器
onUnmounted(() => {
  document.removeEventListener('refresh-session-detail', handleRefreshSessionDetail as EventListener)
})
</script>

<template>
  <div class="chat-panel chat-session">
    <div class="chat-header">
      <ElButton v-if="showBackButton" link @click="goBack" :icon="ArrowLeft">返回</ElButton>
      
      <!-- 添加切换按钮插槽 -->
      <slot name="toggle-button"></slot>
      
      <h2 v-if="session">{{ session.sessionName }}</h2>
      <div v-else class="no-session-title">请选择或创建一个会话</div>
      
      <div class="chat-actions" v-if="sessionId">
        <!-- GraphRAG开关 -->
        <div class="graph-rag-toggle">
          <span class="toggle-label">GraphRAG:</span>
          <el-switch 
            v-model="enableGraphRAG" 
            size="small"
            active-text="开启"
            inactive-text="关闭"
            inline-prompt
          />
        </div>
        <ElButton type="danger" @click="clearMessages" plain size="small" :loading="loading">清空记录</ElButton>
        <ElButton type="primary" @click="createNewSession" :icon="Plus" circle />
      </div>
    </div>
    
    <div class="model-options" v-if="sessionId">
      <ModelSelector v-model="selectedModel" />
    </div>
    
    <div class="messages-container" ref="messagesContainer" v-if="sessionId">
      <template v-if="messages.length">
        <ChatMessageComponent 
          v-for="(message, index) in messages" 
          :key="index" 
          :message="message" 
        />
        
        <!-- 推荐内容展示 -->
        <div v-if="showRecommendations && recommendedItems.length > 0" class="recommendations-container">
          <div class="recommendations-header">
            <h3>{{ isHistoryItems ? '您可能感兴趣的旅游景点' : '为您推荐的旅游景点' }}</h3>
            <div v-if="recommendKeywords.length > 0 && !isHistoryItems" class="keywords-container">
              <span class="keyword-label">关键词：</span>
              <el-tag 
                v-for="(keyword, idx) in recommendKeywords" 
                :key="idx" 
                size="small" 
                type="info" 
                effect="plain"
                class="keyword-tag"
              >
                {{ keyword }}
              </el-tag>
            </div>
            <div v-if="recommendationMessage" class="recommendation-message">
              {{ recommendationMessage }}
            </div>
          </div>
          
          <div class="recommendation-items">
            <div 
              v-for="item in recommendedItems" 
              :key="item.id" 
              class="recommendation-item"
              @click="goToItemDetail(item.id)"
            >
              <div class="item-title">{{ item.title }}</div>
              <div class="item-category" v-if="item.categoryName">
                <el-tag size="small" type="success" effect="plain">{{ item.categoryName }}</el-tag>
              </div>
              <div class="item-description" v-if="item.description">
                {{ item.description.length > 50 ? item.description.substring(0, 50) + '...' : item.description }}
              </div>
              <!-- 显示历史交互标签 -->
              <div v-if="isHistoryItems && item.interactionLabel" class="item-interaction">
                <el-tag size="small" type="warning" effect="plain">{{ item.interactionLabel }}</el-tag>
              </div>
              <div class="item-score" v-else>
                热度评分: {{ item.score }}
              </div>
            </div>
          </div>
          
          <div v-if="isLoadingRecommendations" class="recommendations-loading">
            <el-skeleton :rows="3" animated />
          </div>
        </div>
      </template>
      <div v-else class="empty-message">
        <div class="empty-chat-icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <p>开始新的对话吧！</p>
        <p class="recommendation-tip">
          <el-tag type="info" effect="plain">提示：输入"给我推荐一些..."可以获取旅游景点推荐</el-tag>
        </p>
      </div>
    </div>
    
    <div class="no-session" v-else>
      <div class="welcome-icon">
        <i class="el-icon-s-promotion"></i>
      </div>
      <p>请在左侧选择一个会话或创建新会话</p>
      <ElButton type="primary" @click="createNewSession" class="welcome-btn">创建新会话</ElButton>
    </div>
    
    <div class="input-container" v-if="sessionId">
      <div class="input-tip" v-if="messages.length > 0">
        <div class="tip-row">
          <el-tooltip content="输入'给我推荐一些...'可触发旅游景点推荐功能" placement="top">
            <el-tag size="small" effect="plain" type="info">
              <i class="el-icon-info"></i> 输入"给我推荐一些..."可获取旅游景点推荐
            </el-tag>
          </el-tooltip>
          <el-tooltip 
            content="开启GraphRAG后，AI会先搜索图数据库中的相关内容再进行回复" 
            placement="top"
          >
            <el-tag size="small" effect="plain" :type="enableGraphRAG ? 'success' : 'warning'">
              <i class="el-icon-connection"></i> GraphRAG: {{ enableGraphRAG ? '已开启' : '已关闭' }}
            </el-tag>
          </el-tooltip>
        </div>
      </div>
      <ChatInput @send="sendMessage" :loading="loading" />
    </div>
  </div>
</template>

<style scoped>
.chat-panel {
  flex-grow: 1;
  flex-shrink: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #fff;
  height: 100%;
  min-width: 0;
}

.chat-header {
  padding: 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e9ecef;
  gap: 12px;
  background-color: #f8f9fa;
}

.chat-header h2 {
  margin: 0;
  flex-grow: 1;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.no-session-title {
  color: #8c8c8c;
  font-size: 16px;
}

.model-options {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #e9ecef;
  gap: 20px;
  background-color: #fafafa;
  justify-content: space-between;
}

.model-options .model-selector {
  flex: 1;
  min-width: 200px;
  max-width: 300px;
}

.messages-container {
  flex-grow: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: #f9fafc;
  word-break: break-word;
  overflow-wrap: break-word;
  scroll-behavior: smooth;
  background-image: linear-gradient(rgba(255, 255, 255, 0.8) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(255, 255, 255, 0.8) 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: center center;
}

/* 推荐内容样式 */
.recommendations-container {
  margin-top: 20px;
  background-color: #f0f9ff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e1f3ff;
}

.recommendations-header {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}

.recommendations-header h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #1989fa;
}

.keywords-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.keyword-label {
  color: #606266;
  font-size: 13px;
}

.keyword-tag {
  margin-right: 4px;
}

.recommendation-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.recommendation-item {
  background-color: white;
  border-radius: 6px;
  padding: 12px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s;
}

.recommendation-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.item-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: #303133;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 40px;
}

.item-category {
  margin-bottom: 8px;
}

.item-description {
  margin-bottom: 8px;
}

.item-score {
  font-size: 12px;
  color: #909399;
}

.recommendation-message {
  margin-top: 10px;
  color: #606266;
  font-size: 13px;
}

.recommendations-loading {
  margin-top: 10px;
}

.empty-message {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #8c8c8c;
  gap: 12px;
}

.recommendation-tip {
  margin-top: 8px;
  font-size: 14px;
}

.empty-chat-icon, .welcome-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 12px;
}

.no-session {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #8c8c8c;
  gap: 16px;
  background-color: #f5f7fa;
}

.welcome-btn {
  padding: 12px 24px;
  font-weight: 500;
}

.input-container {
  padding: 16px 24px;
  border-top: 1px solid #e9ecef;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.03);
}

.input-tip {
  margin-bottom: 8px;
  display: flex;
  justify-content: flex-end;
}

.tip-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.graph-rag-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: rgba(255, 255, 255, 0.8);
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e1e4e8;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.toggle-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .chat-header {
    padding: 16px;
  }
  
  .messages-container {
    padding: 16px;
  }
  
  .input-container {
    padding: 16px;
  }
  
  .recommendation-items {
    grid-template-columns: 1fr;
  }
}

/* 添加交互标签样式 */
.item-interaction {
  margin-top: 8px;
  font-size: 12px;
  color: #e6a23c;
}
</style>