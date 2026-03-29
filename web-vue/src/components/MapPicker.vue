<template>
  <div class="map-picker">
    <div class="map-controls">
      <div class="map-instructions">
        <el-icon><InfoFilled /></el-icon>
        <span>请在地图上点击选择景点位置</span>
      </div>
      <el-button v-if="latitude && longitude" @click="clearLocation" type="danger" plain>
        清除位置
      </el-button>
    </div>
    
    <div class="location-info" v-if="latitude && longitude">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-input v-model="latitude" placeholder="纬度" readonly>
            <template #prepend>纬度</template>
          </el-input>
        </el-col>
        <el-col :span="12">
          <el-input v-model="longitude" placeholder="经度" readonly>
            <template #prepend>经度</template>
          </el-input>
        </el-col>
      </el-row>
    </div>
    
    <div ref="mapContainer" class="map-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { mapConfig } from '@/config/map'

interface Props {
  modelValue?: {
    latitude?: number
    longitude?: number
  }
  height?: string
}

interface Emits {
  (e: 'update:modelValue', value: { latitude: number; longitude: number }): void
}

const props = withDefaults(defineProps<Props>(), {
  height: '400px'
})

const emit = defineEmits<Emits>()

const mapContainer = ref<HTMLDivElement>()
const map = ref<any>(null)
const marker = ref<any>(null)

// 双向绑定的位置信息
const latitude = ref<number | undefined>(props.modelValue?.latitude)
const longitude = ref<number | undefined>(props.modelValue?.longitude)

// 监听属性变化
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    latitude.value = newValue.latitude
    longitude.value = newValue.longitude
    
    if (newValue.latitude && newValue.longitude && map.value) {
      updateMapLocation(newValue.latitude, newValue.longitude)
    }
  }
}, { immediate: true })

// 监听位置变化，向外发送事件
watch([latitude, longitude], ([lat, lng]) => {
  if (lat && lng) {
    emit('update:modelValue', {
      latitude: lat,
      longitude: lng
    })
  }
})

// 初始化地图
const initMap = async () => {
  try {
    // 动态加载高德地图API
    if (!window.AMap) {
      await loadAMapScript()
    }

    // 创建地图实例
    map.value = new window.AMap.Map(mapContainer.value, {
      zoom: mapConfig.defaultZoom,
      center: [116.397428, 39.90923], // 默认北京
      mapStyle: mapConfig.mapStyle
    })


    // 添加地图点击事件
    map.value.on('click', handleMapClick)

    // 如果有初始位置，设置地图中心和标记
    if (latitude.value && longitude.value) {
      updateMapLocation(latitude.value, longitude.value)
    }

    console.log('地图初始化成功')
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败，请检查网络连接')
  }
}

// 动态加载高德地图脚本
const loadAMapScript = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    if (window.AMap) {
      resolve()
      return
    }

    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=${mapConfig.version}&key=${mapConfig.key}&plugin=${mapConfig.plugins.join(',')}`
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('高德地图API加载失败'))
    document.head.appendChild(script)
  })
}

// 处理地图点击事件
const handleMapClick = (e: any) => {
  const { lng, lat } = e.lnglat
  updateLocationInfo(lat, lng)
}

// 更新位置信息
const updateLocationInfo = (lat: number, lng: number) => {
  latitude.value = lat
  longitude.value = lng
  // 更新地图标记
  updateMapLocation(lat, lng)
}

// 更新地图位置和标记
const updateMapLocation = (lat: number, lng: number) => {
  if (!map.value) return

  const position = [lng, lat]
  
  // 设置地图中心
  map.value.setCenter(position)
  map.value.setZoom(15)

  // 清除旧标记
  if (marker.value) {
    map.value.remove(marker.value)
  }

  // 添加新标记
  marker.value = new window.AMap.Marker({
    position: position,
    draggable: true,
    cursor: 'move'
  })

  map.value.add(marker.value)

  // 添加标记拖拽事件
  marker.value.on('dragend', (e: any) => {
    const { lng, lat } = e.lnglat
    updateLocationInfo(lat, lng)
  })
}


// 清除位置
const clearLocation = () => {
  latitude.value = undefined
  longitude.value = undefined
  
  if (marker.value && map.value) {
    map.value.remove(marker.value)
    marker.value = null
  }
  
  // 重置地图中心
  if (map.value) {
    map.value.setCenter([116.397428, 39.90923])
    map.value.setZoom(mapConfig.defaultZoom)
  }
}

onMounted(async () => {
  await nextTick()
  initMap()
})

onUnmounted(() => {
  if (map.value) {
    map.value.destroy()
  }
})

// 全局类型声明
declare global {
  interface Window {
    AMap: any
  }
}
</script>

<style scoped>
.map-picker {
  width: 100%;
}

.map-controls {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.map-instructions {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.map-instructions .el-icon {
  color: #409eff;
}

.location-info {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.map-container {
  width: 100%;
  height: v-bind(height);
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>