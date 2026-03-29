<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { likeApi } from '@/api/like'

// 定义组件属性
const props = defineProps({
  // 景点ID
  itemId: {
    type: Number,
    required: true
  },
  // 初始点赞状态
  initialIsLiked: {
    type: Boolean,
    default: false
  },
  // 初始点赞数
  initialLikeCount: {
    type: Number,
    default: 0
  },
  // 是否显示点赞数
  showCount: {
    type: Boolean,
    default: true
  },
  // 点赞图标大小
  size: {
    type: String as () => '' | 'default' | 'small' | 'large',
    default: 'default'
  }
})

// 定义事件
const emit = defineEmits(['update:isLiked', 'update:likeCount', 'like', 'unlike'])

// 本地状态
const isLiked = ref(props.initialIsLiked)
const likeCount = ref(props.initialLikeCount)
const loading = ref(false)

// 点赞/取消点赞
const toggleLike = async () => {
  if (loading.value) return

  loading.value = true
  try {
    if (isLiked.value) {
      // 取消点赞
      await likeApi.unlike(props.itemId)
      isLiked.value = false
      likeCount.value = Math.max(0, likeCount.value - 1)
      emit('unlike')
    } else {
      // 点赞
      await likeApi.like({ itemId: props.itemId })
      isLiked.value = true
      likeCount.value += 1
      emit('like')
    }
    // 更新父组件状态
    emit('update:isLiked', isLiked.value)
    emit('update:likeCount', likeCount.value)
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
    console.error('点赞操作失败', error)
  } finally {
    loading.value = false
  }
}

// 加载点赞状态
const loadLikeStatus = async () => {
  try {
    const status = await likeApi.status(props.itemId)
    isLiked.value = status
    emit('update:isLiked', isLiked.value)
  } catch (error) {
    console.error('获取点赞状态失败', error)
  }
}

// 加载点赞数
const loadLikeCount = async () => {
  try {
    const count = await likeApi.count(props.itemId)
    likeCount.value = count
    emit('update:likeCount', likeCount.value)
  } catch (error) {
    console.error('获取点赞数失败', error)
  }
}

// 初始化
onMounted(() => {
  // 如果没有传入初始值，则从服务端获取
  if (props.initialIsLiked === false) {
    loadLikeStatus()
  }
  if (props.initialLikeCount === 0) {
    loadLikeCount()
  }
})
</script>

<template>
  <div class="like-button">
    <el-button
      :type="isLiked ? 'danger' : 'default'"
      :icon="isLiked ? 'el-icon-star-on' : 'el-icon-star-off'"
      :size="size"
      :loading="loading"
      @click="toggleLike"
    >
      <el-icon v-if="isLiked"><StarFilled /></el-icon>
      <el-icon v-else><Star /></el-icon>
      <span v-if="showCount" class="like-count">{{ likeCount }}</span>
    </el-button>
  </div>
</template>

<style scoped>
.like-button {
  display: inline-flex;
  align-items: center;
}

.like-count {
  margin-left: 4px;
}
</style> 