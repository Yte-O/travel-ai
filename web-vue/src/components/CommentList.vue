<template>
  <div class="comment-list">
    <div class="comment-header-actions">
      <h3 class="section-title">评论列表</h3>
      <el-button type="primary" size="small" :icon="Refresh" @click="handleRefresh" :loading="loading">
        刷新评论
      </el-button>
    </div>
    <div v-if="loading" class="comment-loading">
      <el-skeleton :rows="3" animated />
    </div>
    <div v-else-if="comments.length === 0" class="comment-empty">
      <el-empty description="暂无评论" />
    </div>
    <div v-else class="comment-items">
      <div v-for="comment in comments" :key="comment.id" class="comment-item" :id="`comment-${comment.id}`" :class="{'highlight': highlightedCommentId === comment.id}">
        <div class="comment-header">
          <el-avatar :size="40" :src="comment.userInfo?.avatarUrl">
            {{ comment.userInfo?.username?.substring(0, 1).toUpperCase() }}
          </el-avatar>
          <div class="comment-info">
            <div class="comment-user">{{ comment.userInfo?.username }}</div>
            <div class="comment-time">{{ relativeTime(comment.createTime) }}</div>
          </div>
          <div v-if="canDeleteComment(comment)" class="comment-actions">
            <el-button type="danger" size="small" text @click="handleDelete(comment.id)">
              删除
            </el-button>
          </div>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        
        <div class="comment-operation-area">
          <el-button type="primary" size="small" text @click="handleReplyToComment(comment)">
            <el-icon><ChatLineRound /></el-icon> 回复
          </el-button>
        </div>
        
        <div class="reply-form" :class="{'active': replyingTo && replyingTo.id === comment.id}">
          <div class="input-with-emoji">
            <el-input
              v-model="replyContents[comment.id]"
              :placeholder="replyingTo && replyingTo.id !== comment.id ? 
                `回复 @${replyingTo.userInfo.username} (Ctrl+Enter发送)` : 
                '写下你的回复... (Ctrl+Enter发送)'"
              :rows="1"
              type="textarea"
              resize="none"
              @keydown.ctrl.enter.prevent="handleReply(comment.id)"
              @focus="handleFocusReply(comment)"
            />
            <div class="emoji-trigger" @click="toggleEmojiPicker(comment.id)">
              <el-tooltip content="插入表情" placement="top">
                <span class="emoji-icon">😊</span>
              </el-tooltip>
            </div>
          </div>
          
          <div v-if="activeEmojiPicker === comment.id" class="emoji-picker">
            <div class="emoji-list">
              <div 
                v-for="emoji in emojis" 
                :key="emoji" 
                class="emoji-item"
                @click="insertEmoji(emoji, comment.id)"
              >
                {{ emoji }}
              </div>
            </div>
          </div>
          
          <div v-if="replyingTo && replyingTo.id !== comment.id" class="reply-preview">
            <div class="quote-line"></div>
            <div class="quote-content">
              <el-text type="info" size="small">{{ truncateContent(replyingTo.content) }}</el-text>
            </div>
          </div>
          
          <div v-if="replyContents[comment.id]" class="reply-form-actions">
            <el-button v-if="replyingTo && replyingTo.id !== comment.id" size="small" @click="cancelReply">
              取消回复
            </el-button>
            <el-button type="primary" size="small" @click="handleReply(comment.id)">
              发表回复
            </el-button>
          </div>
        </div>
        
        <div v-if="comment.replies && comment.replies.length > 0" class="comment-replies">
          <div v-for="reply in comment.replies" :key="reply.id" class="reply-item" :id="`comment-${reply.id}`" :class="{'highlight': highlightedCommentId === reply.id}">
            <div class="reply-header">
              <el-avatar :size="32" :src="reply.userInfo?.avatarUrl">
                {{ reply.userInfo?.username?.substring(0, 1).toUpperCase() }}
              </el-avatar>
              <div class="reply-info">
                <div class="reply-user">
                  {{ reply.userInfo?.username }}
                  <span v-if="reply.replyToUserInfo" class="reply-to">
                    回复 <span class="reply-to-name">@{{ reply.replyToUserInfo.username }}</span>
                  </span>
                </div>
                <div class="reply-time">{{ relativeTime(reply.createTime) }}</div>
              </div>
              <div class="reply-actions">
                <el-button type="primary" size="small" text @click="handleReplyToReply(reply)">
                  <el-icon><ChatLineRound /></el-icon> 回复
                </el-button>
                <el-button v-if="canDeleteComment(reply)" type="danger" size="small" text @click="handleDelete(reply.id)">
                  删除
                </el-button>
              </div>
            </div>
            
            <div v-if="reply.replyToCommentId && getQuotedContent(reply.replyToCommentId)" class="reply-quote" @click="scrollToComment(reply.replyToCommentId)">
              <div class="quote-line"></div>
              <div class="quote-content">
                <el-text type="info" size="small">{{ getQuotedContent(reply.replyToCommentId) }}</el-text>
              </div>
            </div>
            
            <div class="reply-content">{{ reply.content }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="comment-form">
      <h3 class="form-title">发表评论</h3>
      <div class="input-with-emoji">
        <el-input
          v-model="newComment"
          placeholder="写下你的评论... (按Ctrl+Enter快速发送)"
          :rows="3"
          type="textarea"
          resize="none"
          @keydown.ctrl.enter.prevent="handleComment"
        />
        <div class="emoji-trigger" @click="toggleEmojiPicker('new')">
          <el-tooltip content="插入表情" placement="top">
            <span class="emoji-icon">😊</span>
          </el-tooltip>
        </div>
      </div>
      
      <div v-if="activeEmojiPicker === 'new'" class="emoji-picker">
        <div class="emoji-list">
          <div 
            v-for="emoji in emojis" 
            :key="emoji" 
            class="emoji-item"
            @click="insertEmoji(emoji, 'new')"
          >
            {{ emoji }}
          </div>
        </div>
      </div>
      
      <el-button type="primary" :disabled="!newComment.trim()" @click="handleComment">
        发表评论
      </el-button>
      <div class="shortcut-tip">提示：按 Ctrl+Enter 快速发送评论</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineProps, defineEmits, watch, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, ChatLineRound } from '@element-plus/icons-vue'
import { commentApi } from '@/api/comment'
import { itemApi } from '@/api/item'
import type { CommentVO } from '@/types/comment'
import type { ItemVO } from '@/types/item'
import { relativeTime } from '@/utils/date'
import { useUserStore } from '@/stores/user'

const props = defineProps<{
  itemId: number
}>()

const emit = defineEmits<{
  (e: 'update-count', count: number): void
}>()

// 评论相关状态
const comments = ref<CommentVO[]>([])
const loading = ref(false)
const newComment = ref('')
const replyContents = ref<Record<number, string>>({})

// 当前正在回复的评论
const replyingTo = ref<CommentVO | null>(null)

// 获取当前用户ID
const userStore = useUserStore()
const userId = computed(() => userStore.userInfo?.id)

// 高亮显示的评论ID
const highlightedCommentId = ref<number | null>(null)

// 表情相关
const activeEmojiPicker = ref<number | string | null>(null)
const emojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
  '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣', '😖',
  '😫', '😩', '🥺', '😢', '😭', '😤', '😠', '😡', '🤬', '😱',
  '👍', '👎', '👏', '🙌', '🤝', '💪', '❤️', '💔', '💯', '✨'
]

// 切换表情选择器显示状态
const toggleEmojiPicker = (id: number | string) => {
  if (activeEmojiPicker.value === id) {
    activeEmojiPicker.value = null
  } else {
    activeEmojiPicker.value = id
  }
}

// 插入表情
const insertEmoji = (emoji: string, id: number | string) => {
  if (id === 'new') {
    newComment.value += emoji
  } else {
    if (!replyContents.value[id as number]) {
      replyContents.value[id as number] = ''
    }
    replyContents.value[id as number] += emoji
  }
}

// 点击外部关闭表情选择器
const closeEmojiPicker = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.emoji-trigger') && !target.closest('.emoji-picker')) {
    activeEmojiPicker.value = null
  }
}

// 加载评论列表
const loadComments = async () => {
  loading.value = true
  try {
    const data = await commentApi.getTree(props.itemId)
    comments.value = data
    emit('update-count', countAllComments(data))
  } catch (error) {
    console.error('加载评论失败', error)
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

// 计算所有评论数量（包括回复）
const countAllComments = (commentList: CommentVO[]) => {
  let count = commentList.length
  for (const comment of commentList) {
    if (comment.replies && comment.replies.length > 0) {
      count += comment.replies.length
    }
  }
  return count
}

// 发表评论
const handleComment = async () => {
  if (!newComment.value.trim()) return
  
  try {
    await commentApi.add({
      itemId: props.itemId,
      content: newComment.value.trim()
    })
    ElMessage.success('评论发表成功')
    newComment.value = ''
    activeEmojiPicker.value = null
    await loadComments()
  } catch (error) {
    console.error('评论失败', error)
    ElMessage.error('评论失败')
  }
}

// 回复子评论
const handleReplyToReply = (reply: CommentVO) => {
  replyingTo.value = reply
  // 确保回复输入框在父评论下方显示
  if (reply.parentId) {
    replyContents.value[reply.parentId] = ''
  }
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
}

// 回复评论
const handleReply = async (commentId: number) => {
  const content = replyContents.value[commentId]
  if (!content || !content.trim()) return
  
  try {
    // 如果是回复子评论
    if (replyingTo.value && replyingTo.value.id !== commentId) {
      await commentApi.add({
        itemId: props.itemId,
        content: content.trim(),
        parentId: commentId, // 父评论ID保持为顶级评论ID
        replyToCommentId: replyingTo.value.id, // 被回复的评论ID
        replyToUserId: replyingTo.value.userId // 被回复的用户ID
      })
    } else {
      // 普通回复顶级评论
      await commentApi.add({
        itemId: props.itemId,
        content: content.trim(),
        parentId: commentId
      })
    }
    
    ElMessage.success('回复发表成功')
    replyContents.value[commentId] = ''
    replyingTo.value = null
    activeEmojiPicker.value = null
    await loadComments()
  } catch (error) {
    console.error('回复失败', error)
    ElMessage.error('回复失败')
  }
}

// 删除评论
const handleDelete = async (commentId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await commentApi.delete(commentId)
    ElMessage.success('删除成功')
    await loadComments()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  }
}

// 截断内容，避免过长
const truncateContent = (content: string, maxLength = 50) => {
  if (!content) return '';
  if (content.length <= maxLength) return content;
  return content.substring(0, maxLength) + '...';
}

// 获取被引用评论的内容
const getQuotedContent = (commentId: number) => {
  // 在所有评论中查找被引用的评论
  for (const comment of comments.value) {
    // 检查顶级评论
    if (comment.id === commentId) {
      return truncateContent(comment.content);
    }
    
    // 检查回复评论
    if (comment.replies) {
      for (const reply of comment.replies) {
        if (reply.id === commentId) {
          return truncateContent(reply.content);
        }
      }
    }
  }
  
  return '';
}

// 滚动到特定评论并高亮显示
const scrollToComment = (commentId: number) => {
  // 先取消之前的高亮
  highlightedCommentId.value = null;
  
  // 延迟执行，确保DOM已更新
  setTimeout(() => {
    // 找到要滚动到的元素
    const element = document.getElementById(`comment-${commentId}`);
    if (element) {
      // 滚动到元素位置，添加平滑效果
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
      
      // 设置高亮
      highlightedCommentId.value = commentId;
      
      // 3秒后取消高亮
      setTimeout(() => {
        highlightedCommentId.value = null;
      }, 3000);
    }
  }, 50);
}

// 添加用户角色计算属性
const userRole = computed(() => userStore.userInfo?.role || 0)

// 当前景点信息
const currentItem = ref<ItemVO | null>(null)

// 判断是否可以删除评论
const canDeleteComment = (comment: CommentVO) => {
  if (!userId.value) return false
  
  // 1. 管理员可以删除所有评论
  if (userStore.isAdmin()) return true
  
  // 2. 评论作者可以删除自己的评论
  if (comment.userId === userId.value) return true
  
  // 3. 景点创建者可以删除自己景点下的所有评论
  if (currentItem.value?.userId === userId.value && 
      comment.itemId === props.itemId) return true
  
  return false
}

// 加载景点信息
const loadItemInfo = async () => {
  try {
    currentItem.value = await itemApi.getById(props.itemId)
  } catch (error) {
    console.error('加载景点信息失败', error)
    ElMessage.error('加载景点信息失败')
  }
}

// 生命周期钩子
onMounted(() => {
  if (props.itemId) {
    loadComments()
    loadItemInfo() // 加载景点信息
  }
  // 添加点击事件监听器，用于关闭表情选择器
  document.addEventListener('click', closeEmojiPicker)
})

onUnmounted(() => {
  // 移除点击事件监听器
  document.removeEventListener('click', closeEmojiPicker)
})

// 监听itemId变化重新加载评论
watch(() => props.itemId, () => {
  if (props.itemId) {
    loadComments()
    loadItemInfo() // 当itemId变化时重新加载景点信息
  }
}, { immediate: true })

// 刷新评论
const handleRefresh = async () => {
  await loadComments()
  ElMessage.success('评论已刷新')
}

// 处理点击回复按钮
const handleReplyToComment = (comment: CommentVO) => {
  replyingTo.value = comment;
  // 设置回复对象
  if (comment.id && !replyContents.value[comment.id]) {
    replyContents.value[comment.id] = '';
  }
}

// 处理回复输入框获取焦点
const handleFocusReply = (comment: CommentVO) => {
  if (!replyingTo.value) {
    replyingTo.value = comment;
  }
}
</script>

<style scoped>
.comment-list {
  margin-top: 20px;
}

.comment-loading,
.comment-empty {
  padding: 20px 0;
}

.comment-items {
  margin-bottom: 20px;
}

.comment-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.comment-item:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.comment-info {
  margin-left: 12px;
  flex: 1;
}

.comment-user {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  padding: 0 0 12px 52px;
  color: #333;
}

.comment-operation-area {
  padding-left: 52px;
  margin-bottom: 12px;
}

.comment-replies {
  margin-left: 52px;
  margin-top: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
}

.reply-item {
  padding: 12px;
  margin-bottom: 10px;
  border-radius: 6px;
  background-color: #fff;
  border: 1px solid #ebeef5;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.reply-info {
  margin-left: 10px;
  flex: 1;
}

.reply-user {
  font-weight: bold;
  font-size: 13px;
  color: #303133;
}

.reply-to {
  font-weight: normal;
  color: #606266;
  margin-left: 5px;
}

.reply-to-name {
  color: #409eff;
  font-weight: 500;
}

.reply-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.reply-content {
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
  padding-left: 42px;
  color: #333;
}

.reply-form {
  display: flex;
  flex-direction: column;
  margin-top: 8px;
  margin-left: 52px;
  border-radius: 8px;
  padding: 8px;
  background-color: #f7f7f7;
  transition: background-color 0.3s;
}

.reply-form.active {
  background-color: #e6f1ff;
}

.reply-form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  gap: 8px;
}

.comment-form {
  margin-top: 30px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.form-title {
  font-size: 16px;
  margin-bottom: 15px;
  font-weight: 600;
  color: #303133;
}

.comment-form .el-button {
  margin-top: 12px;
}

.comment-actions,
.reply-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.reply-quote {
  margin: 0 0 10px 42px;
  display: flex;
  position: relative;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.reply-quote:hover {
  transform: translateX(3px);
}

.reply-quote:hover .quote-line {
  width: 4px;
}

.reply-quote:hover .quote-content {
  background-color: #e6f1ff;
}

.quote-line {
  width: 3px;
  background-color: #409eff;
  margin-right: 8px;
  border-radius: 2px;
  transition: width 0.2s;
}

.quote-content {
  font-size: 12px;
  color: #606266;
  background-color: #f0f7ff;
  padding: 8px 12px;
  border-radius: 4px;
  word-break: break-word;
  flex: 1;
  position: relative;
  transition: background-color 0.2s;
}

.quote-content::before {
  content: '"';
  font-size: 16px;
  color: #409eff;
  margin-right: 2px;
}

.quote-content::after {
  content: '"';
  font-size: 16px;
  color: #409eff;
  margin-left: 2px;
}

.reply-preview {
  margin: 8px 0;
  display: flex;
}

.comment-item.highlight,
.reply-item.highlight {
  animation: highlight-animation 3s ease;
}

@keyframes highlight-animation {
  0% { background-color: rgba(64, 158, 255, 0.1); }
  70% { background-color: rgba(64, 158, 255, 0.1); }
  100% { background-color: transparent; }
}

.input-with-emoji {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.input-with-emoji .el-input {
  flex: 1;
}

.emoji-icon {
  font-size: 18px;
  cursor: pointer;
}

.emoji-trigger {
  position: absolute;
  right: 10px;
  bottom: 10px;
  cursor: pointer;
  transition: transform 0.2s;
  z-index: 2;
}

.emoji-trigger:hover {
  transform: scale(1.2);
}

.emoji-picker {
  position: relative;
  margin-top: 5px;
  margin-bottom: 10px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.emoji-list {
  display: flex;
  flex-wrap: wrap;
  padding: 10px;
  max-height: 150px;
  overflow-y: auto;
}

.emoji-item {
  font-size: 20px;
  padding: 5px;
  cursor: pointer;
  transition: transform 0.1s, background-color 0.2s;
  border-radius: 4px;
}

.emoji-item:hover {
  background-color: #f0f7ff;
  transform: scale(1.2);
}

.comment-header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.upload-tip {
  margin-top: 12px;
  line-height: 1.5;
  color: #909399;
}

.shortcut-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style> 