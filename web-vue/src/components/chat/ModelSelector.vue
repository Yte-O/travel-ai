<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElSelect, ElOption } from 'element-plus'
import type { ChatModel } from '@/types/chat'
import { llmApi } from '@/api/chat'

// 组件属性
const props = defineProps<{
  modelValue: string
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

// 创建本地计算属性用于v-model代替直接绑定到props
const localModelValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 模型列表
const models = ref<ChatModel[]>([])
const loading = ref(false)

// 加载模型列表
const loadModels = async () => {
  try {
    loading.value = true
    models.value = await llmApi.getModels()
  } catch (error) {
    console.error('加载模型列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载模型列表
onMounted(() => {
  loadModels()
})
</script>

<template>
  <div class="model-selector">
    <ElSelect 
      v-model="localModelValue" 
      placeholder="选择模型" 
      :loading="loading"
      size="small"
      style="width: 180px"
    >
      <ElOption
        v-for="model in models"
        :key="model.key"
        :label="model.name"
        :value="model.key"
      />
    </ElSelect>
  </div>
</template>

<style scoped>
.model-selector {
  margin-bottom: 8px;
  min-width: 180px;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select-dropdown__item) {
  padding-right: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style> 