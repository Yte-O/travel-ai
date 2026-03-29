<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { ElMessage, ElButton, ElPopconfirm, ElInput } from 'element-plus'
import { Plus, Delete, Edit } from '@element-plus/icons-vue'
import { chatApi } from '@/api/chat'
import type { ChatSession } from '@/types/chat'

// 组件属性
const props = defineProps<{
  currentSessionId?: number | null;
  loading?: boolean;
}>();

// 组件事件
const emits = defineEmits<{
  'select-session': [sessionId: number];
  'create-session': [];
  'delete-session': [session: ChatSession];
  'sessions-loaded': [sessions: ChatSession[]];
}>();

const sessions = ref<ChatSession[]>([])
const localLoading = ref(false)

// 计算当前是否加载中
const isLoading = () => props.loading !== undefined ? props.loading : localLoading.value;

// 会话重命名相关状态
const editingSessionId = ref<number | null>(null)
const editingSessionName = ref('')

// 加载会话列表
const loadSessions = async () => {
  try {
    localLoading.value = true
    sessions.value = await chatApi.getSessions()
    emits('sessions-loaded', sessions.value)
  } catch (error) {
    ElMessage.error('加载会话列表失败')
    console.error(error)
  } finally {
    localLoading.value = false
  }
}

// 创建新会话，只发送事件，由父组件处理
const createSession = () => {
  emits('create-session')
}

// 选择会话
const selectSession = (session: ChatSession) => {
  emits('select-session', session.id)
}

// 删除会话
const deleteSession = (session: ChatSession) => {
  emits('delete-session', session)
  
  // 由于父组件可能已经处理了删除会话的逻辑
  // 这里仍需要刷新本地会话列表，确保UI同步更新
  setTimeout(() => {
    loadSessions()
  }, 300) // 短暂延迟确保后端操作完成
}

// 格式化时间
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleString()
}

// 开始重命名会话
const startRenameSession = (session: ChatSession, event: Event) => {
  event.stopPropagation() // 阻止点击事件冒泡
  editingSessionId.value = session.id
  editingSessionName.value = session.sessionName
}

// 取消重命名
const cancelRenameSession = () => {
  editingSessionId.value = null
  editingSessionName.value = ''
}

// 保存重命名
const saveRenameSession = async (session: ChatSession) => {
  if (!editingSessionName.value.trim()) {
    ElMessage.warning('会话名称不能为空')
    return
  }
  
  try {
    await chatApi.updateSession({
      id: session.id,
      sessionName: editingSessionName.value.trim()
    })
    
    // 更新本地会话列表
    const index = sessions.value.findIndex(s => s.id === session.id)
    if (index !== -1) {
      sessions.value[index].sessionName = editingSessionName.value.trim()
    }
    
    cancelRenameSession()
    ElMessage.success('重命名成功')
    
    // 刷新会话列表
    await loadSessions()
    
    // 触发刷新会话详情的事件
    document.dispatchEvent(new CustomEvent('refresh-session-detail', { detail: session.id }))
  } catch (error) {
    ElMessage.error('重命名失败')
    console.error(error)
  }
}

// 处理重命名输入框的键盘事件
const handleRenameKeydown = (session: ChatSession, event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    saveRenameSession(session)
  } else if (event.key === 'Escape') {
    cancelRenameSession()
  }
}

// 监听刷新会话列表事件
const handleRefreshSessionList = () => {
  loadSessions()
}

// 监听会话ID变化，高亮显示当前选中的会话
watch(() => props.currentSessionId, () => {
  // 可以在这里添加额外的逻辑
}, { immediate: true })

onMounted(() => {
  loadSessions()
  // 添加自定义事件监听器
  document.addEventListener('refresh-session-list', handleRefreshSessionList)
})

onUnmounted(() => {
  // 移除自定义事件监听器
  document.removeEventListener('refresh-session-list', handleRefreshSessionList)
})
</script>

<template>
  <div class="sessions-panel session-list">
    <div class="sessions-header">
      <h2>我的聊天会话</h2>
      <ElButton 
        type="primary" 
        :icon="Plus" 
        @click="createSession"
        :loading="isLoading()"
        class="create-btn"
      >
        新建会话
      </ElButton>
    </div>
    
    <div class="sessions-list">
      <div v-if="isLoading()" class="loading">
        <div class="loading-spinner"></div>
        加载中...
      </div>
      <div v-else-if="!sessions.length" class="empty">
        <div class="empty-icon">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <p>暂无会话，点击"新建会话"开始聊天吧！</p>
      </div>
      <div 
        v-else
        v-for="session in sessions" 
        :key="session.id" 
        class="session-item"
        :class="{ 'active': currentSessionId === session.id }"
        @click="selectSession(session)"
      >
        <div class="session-info">
          <!-- 正在编辑状态显示输入框 -->
          <div v-if="editingSessionId === session.id" class="session-name-editing" @click.stop>
            <ElInput 
              v-model="editingSessionName" 
              size="small" 
              @keydown.enter="saveRenameSession(session)"
              @keydown.esc="cancelRenameSession()"
              @blur="saveRenameSession(session)"
              ref="sessionNameInput"
              placeholder="输入会话名称"
              autofocus
            />
          </div>
          <!-- 非编辑状态显示会话名称 -->
          <div v-else class="session-name">{{ session.sessionName }}</div>
          <div class="session-time">{{ formatDate(session.updateTime) }}</div>
          <div class="session-preview" v-if="session.latestMessage">
            {{ session.latestMessage }}
          </div>
        </div>
        <div class="session-actions" @click.stop>
          <!-- 添加重命名按钮 -->
          <ElButton 
            type="primary" 
            :icon="Edit" 
            circle 
            size="small"
            @click="(e) => startRenameSession(session, e)"
            style="margin-right: 8px;"
            class="action-btn edit-btn"
          />
          <ElPopconfirm
            title="确定删除此会话吗？"
            @confirm="deleteSession(session)"
            confirm-button-text="确定"
            cancel-button-text="取消"
            placement="top"
          >
            <template #reference>
              <ElButton 
                type="danger" 
                :icon="Delete" 
                circle 
                size="small"
                class="action-btn delete-btn"
              />
            </template>
          </ElPopconfirm>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sessions-panel {
  width: 320px;
  min-width: 320px;
  border-right: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #fff;
  height: 100%;
}

.sessions-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e9ecef;
  background-color: #f8f9fa;
}

.sessions-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.create-btn {
  font-weight: 500;
}

.sessions-list {
  flex-grow: 1;
  overflow-y: auto;
  padding: 12px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fff;
}

.session-item:hover {
  background-color: #f8f9fa;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.session-item.active {
  background-color: #e6f7ff;
  border-color: #91d5ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-name {
  font-weight: bold;
  margin-bottom: 4px;
  color: #333;
  font-size: 15px;
}

.session-time {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 6px;
}

.session-preview {
  font-size: 13px;
  color: #595959;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.session-actions {
  margin-left: 10px;
  opacity: 0.6;
  transition: opacity 0.2s ease;
}

.session-item:hover .session-actions {
  opacity: 1;
}

.action-btn {
  transition: transform 0.2s ease;
}

.action-btn:hover {
  transform: scale(1.1);
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 40px;
  color: #8c8c8c;
  gap: 12px;
  height: 100%;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 48px;
  color: #d9d9d9;
  margin-bottom: 12px;
}

/* 会话名称编辑状态样式 */
.session-name-editing {
  margin-bottom: 8px;
  width: 100%;
}

.session-name-editing :deep(.el-input__inner) {
  height: 32px;
  font-size: 14px;
  border-radius: 6px;
  border: 1px solid #91d5ff;
  background-color: #fff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
  transition: all 0.3s;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .sessions-panel {
    width: 100%;
  }
  
  .sessions-header {
    padding: 16px;
  }
}
</style> 