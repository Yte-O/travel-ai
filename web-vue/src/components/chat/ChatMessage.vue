<script setup lang="ts">
import { computed } from 'vue'
import type { ChatMessage } from '@/types/chat'
import MarkdownIt from 'markdown-it'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

// 创建markdown-it实例
const md = new MarkdownIt({
  html: true,
  breaks: true,
  linkify: true
})

// 获取用户信息
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 组件属性
const props = defineProps<{
  message: ChatMessage
}>()

// 计算消息样式
const messageClass = computed(() => {
  return {
    'message': true,
    'message-user': props.message.role === 'user',
    'message-assistant': props.message.role === 'assistant'
  }
})

// 格式化显示时间
const formattedTime = computed(() => {
  if (!props.message.messageTime) return ''
  
  const date = new Date(props.message.messageTime)
  return date.toLocaleString()
})

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (!props.message.content || props.message.content.trim().length === 0) {
    return ''
  }
  return md.render(props.message.content)
})
</script>

<template>
  <div :class="messageClass">
    <div class="avatar-container">
      <div class="avatar" :class="message.role === 'user' ? 'avatar-user' : 'avatar-ai'">
        <img v-if="message.role === 'assistant'" src="@/assets/images/logo.png" alt="AI" class="avatar-img" />
        <img v-else-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" alt="用户" class="avatar-img" />
        <UserFilled v-else />
      </div>
    </div>
    <div class="message-wrapper">
      <div class="message-header">
        <div class="message-role">{{ message.role === 'user' ? '我' : 'AI助手' }}</div>
        <div v-if="message.role === 'assistant'" class="message-time">{{ formattedTime }}</div>
      </div>
      <div class="message-content">
        <div v-if="message.content && message.content.trim().length > 0" v-html="renderedContent"></div>
        <p v-else class="empty-content">{{ message.role === 'assistant' ? '等待AI响应...' : '空白消息' }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  width: 100%;
}

.message-user {
  flex-direction: row-reverse;
}

.avatar-container {
  flex-shrink: 0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  color: #fff;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-user {
  background-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.avatar-ai {
  background-color: #52c41a;
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.2);
}

.message-wrapper {
  max-width: calc(100% - 60px);
  padding: 10px 14px;
  border-radius: 12px;
  box-sizing: border-box;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;
}

.message-user .message-wrapper {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  margin-right: 10px;
  border-top-right-radius: 2px;
}

.message-assistant .message-wrapper {
  background-color: #fff;
  border: 1px solid #f0f0f0;
  margin-left: 10px;
  border-top-left-radius: 2px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 12px;
}

.message-user .message-header {
  justify-content: flex-start;
}

.message-role {
  font-weight: bold;
  color: #333;
}

.message-time {
  color: #999;
  font-size: 11px;
}

.message-content {
  word-break: break-word;
  overflow-wrap: break-word;
  line-height: 1.5;
  max-width: 100%;
  font-size: 14px;
  color: #333;
}

.message-content :deep(pre) {
  background-color: #f7f9fb;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  white-space: pre-wrap;
  max-width: 100%;
  margin: 8px 0;
  border: 1px solid #e8eaed;
}

.message-content :deep(code) {
  background-color: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  word-break: break-all;
  font-size: 13px;
  color: #476582;
}

.message-content :deep(blockquote) {
  border-left: 4px solid #dfe2e5;
  padding: 0 12px;
  margin: 12px 0;
  color: #666;
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 0 4px 4px 0;
}

.message-content :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 8px 0;
}

.message-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
  border-radius: 4px;
  overflow: hidden;
}

.message-content :deep(th), .message-content :deep(td) {
  border: 1px solid #e8eaed;
  padding: 8px;
  text-align: left;
}

.message-content :deep(th) {
  background-color: #f7f9fb;
}

.message-content :deep(tr:nth-child(2n)) {
  background-color: #f9fafb;
}

.message-content :deep(ul), .message-content :deep(ol) {
  padding-left: 20px;
  margin: 10px 0;
}

.message-content :deep(li) {
  margin-bottom: 5px;
}

.empty-content {
  color: #999;
  font-style: italic;
  margin: 0;
}

/* 创建聊天气泡的小尾巴 */
.message-user .message-wrapper::after {
  content: "";
  position: absolute;
  top: 14px;
  right: -10px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent transparent transparent #e6f7ff;
}

.message-assistant .message-wrapper::after {
  content: "";
  position: absolute;
  top: 14px;
  left: -10px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent #fff transparent transparent;
}
</style> 