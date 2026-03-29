<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { favoriteApi } from '@/api/favorite'

// 定义组件属性
const props = defineProps({
  // 景点ID
  itemId: {
    type: Number,
    required: true
  },
  // 初始收藏状态
  initialIsFavorite: {
    type: Boolean,
    default: false
  },
  // 按钮文本
  text: {
    type: Boolean,
    default: false
  },
  // 按钮大小
  size: {
    type: String as () => '' | 'default' | 'small' | 'large',
    default: 'default'
  }
})

// 定义事件
const emit = defineEmits(['update:isFavorite', 'favorite', 'unfavorite'])

// 本地状态
const isFavorite = ref(props.initialIsFavorite)
const loading = ref(false)

// 收藏/取消收藏
const toggleFavorite = async () => {
  if (loading.value) return

  loading.value = true
  try {
    if (isFavorite.value) {
      // 取消收藏
      await favoriteApi.remove(props.itemId)
      isFavorite.value = false
      emit('unfavorite')
    } else {
      // 收藏
      await favoriteApi.add({ itemId: props.itemId })
      isFavorite.value = true
      emit('favorite')
    }
    // 更新父组件状态
    emit('update:isFavorite', isFavorite.value)
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
    console.error('收藏操作失败', error)
  } finally {
    loading.value = false
  }
}

// 加载收藏状态
const loadFavoriteStatus = async () => {
  try {
    const status = await favoriteApi.status(props.itemId)
    isFavorite.value = status
    emit('update:isFavorite', isFavorite.value)
  } catch (error) {
    console.error('获取收藏状态失败', error)
  }
}

// 初始化
onMounted(() => {
  // 如果没有传入初始值，则从服务端获取
  if (props.initialIsFavorite === false) {
    loadFavoriteStatus()
  }
})
</script>

<template>
  <div class="favorite-button">
    <el-button
      :type="isFavorite ? 'warning' : 'default'"
      :size="size"
      :loading="loading"
      :text="text"
      @click="toggleFavorite"
    >
      <el-icon v-if="isFavorite"><StarFilled /></el-icon>
      <el-icon v-else><Star /></el-icon>
      <span class="favorite-text">{{ isFavorite ? '已收藏' : '收藏' }}</span>
    </el-button>
  </div>
</template>

<style scoped>
.favorite-button {
  display: inline-flex;
  align-items: center;
}

.favorite-text {
  margin-left: 4px;
}
</style> 