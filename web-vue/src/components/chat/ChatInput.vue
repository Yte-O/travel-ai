<script setup lang="ts">
import { ref } from 'vue'
import { ElInput, ElButton } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'

// 组件属性
defineProps<{
  loading?: boolean
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'send', value: string): void
}>()

// 输入内容
const message = ref('')

// 发送消息
const sendMessage = () => {
  if (!message.value.trim()) return
  emit('send', message.value)
  message.value = ''
}

// 监听回车键
const handleKeyDown = (e: Event) => {
  const keyEvent = e as KeyboardEvent
  if (keyEvent.key === 'Enter' && !keyEvent.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}
</script>

<template>
  <div class="chat-input">
    <div class="input-container">
      <ElInput
        v-model="message"
        type="textarea"
        :rows="2"
        placeholder="输入消息内容，按Enter键发送，Shift+Enter换行..."
        resize="none"
        @keydown="handleKeyDown"
        :disabled="loading"
        class="message-textarea"
      />
      <div class="input-actions">
        <ElButton 
          type="primary" 
          @click="sendMessage" 
          :loading="loading" 
          :disabled="!message.trim()"
          class="send-button"
          size="small"
        >
          <Promotion class="send-icon" />
          发送
        </ElButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-input {
  width: 100%;
  margin-top: auto;
}

.input-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;
}

.input-container:focus-within {
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.12);
}

.message-textarea {
  width: 100%;
}

.message-textarea :deep(.el-textarea__inner) {
  border: none;
  border-radius: 0;
  padding: 12px;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  background-color: #fff;
  transition: all 0.3s ease;
}

.message-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: none;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 6px 12px;
  background-color: #f9fafb;
  border-top: 1px solid #edf2f7;
}

.send-button {
  display: flex;
  align-items: center;
  padding: 6px 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.send-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.15);
}

.send-icon {
  margin-right: 4px;
  font-size: 12px;
}
</style> 