<template>
  <div class="map-viewer">
    <div v-if="!hasLocation" class="no-location">
      <el-icon class="location-icon"><LocationInformation /></el-icon>
      <span>暂无地理位置信息</span>
    </div>
    
    <div v-else>
      <div class="location-info">
        <div class="location-details">
          <div class="location-item">
            <span class="label">经纬度：</span>
            <span class="value">{{ latitude }}, {{ longitude }}</span>
          </div>
        </div>
        <div class="map-actions">
          <el-button @click="openInGaode" type="primary" size="small">
            <el-icon><MapLocation /></el-icon>
            高德地图查看
          </el-button>
          <el-button @click="copyLocation" size="small">
            <el-icon><CopyDocument /></el-icon>
            复制坐标
          </el-button>
        </div>
      </div>
      
      <div ref="mapContainer" class="map-container"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { LocationInformation, MapLocation, CopyDocument } from '@element-plus/icons-vue'
import { mapConfig } from '@/config/map'

interface Props {
  latitude?: number
  longitude?: number
  height?: string
  zoom?: number
}

const props = withDefaults(defineProps<Props>(), {
  height: '300px',
  zoom: 15
})

const mapContainer = ref<HTMLDivElement>()
const map = ref<any>(null)
const marker = ref<any>(null)

// 是否有位置信息
const hasLocation = computed(() => {
  return props.latitude !== undefined && props.longitude !== undefined
})

// 初始化地图
const initMap = async () => {
  if (!hasLocation.value || !mapContainer.value) return

  try {
    // 动态加载高德地图API
    if (!window.AMap) {
      await loadAMapScript()
    }

    // 创建地图实例
    map.value = new window.AMap.Map(mapContainer.value, {
      zoom: props.zoom,
      center: [props.longitude, props.latitude],
      mapStyle: mapConfig.mapStyle,
      viewMode: '3D'
    })

    // 添加标记
    marker.value = new window.AMap.Marker({
      position: [props.longitude, props.latitude],
      title: '景点位置'
    })

    map.value.add(marker.value)

    // 添加信息窗口
    const infoWindow = new window.AMap.InfoWindow({
      content: `<div style="padding: 10px;">
        <div style="font-weight: bold; margin-bottom: 5px;">景点位置</div>
        <div style="color: #999; font-size: 12px;">
          经纬度: ${props.latitude}, ${props.longitude}
        </div>
      </div>`,
      anchor: 'bottom-center',
      offset: new window.AMap.Pixel(0, -30)
    })

    // 点击标记显示信息窗口
    marker.value.on('click', () => {
      infoWindow.open(map.value, marker.value.getPosition())
    })

    // 添加控件
    map.value.addControl(new window.AMap.Scale())
    map.value.addControl(new window.AMap.ToolBar())

    console.log('地图查看器初始化成功')
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败')
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

// 在高德地图中打开
const openInGaode = () => {
  if (!hasLocation.value) return
  
  const url = `https://uri.amap.com/marker?position=${props.longitude},${props.latitude}&name=${encodeURIComponent('景点位置')}`
  window.open(url, '_blank')
}

// 复制坐标
const copyLocation = async () => {
  if (!hasLocation.value) return
  
  const locationText = `${props.latitude}, ${props.longitude}`
  
  try {
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(locationText)
      ElMessage.success('坐标已复制到剪贴板')
    } else {
      // 降级方案
      const textArea = document.createElement('textarea')
      textArea.value = locationText
      document.body.appendChild(textArea)
      textArea.select()
      document.execCommand('copy')
      document.body.removeChild(textArea)
      ElMessage.success('坐标已复制到剪贴板')
    }
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

onMounted(async () => {
  await nextTick()
  if (hasLocation.value) {
    initMap()
  }
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
.map-viewer {
  width: 100%;
}

.no-location {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
}

.location-icon {
  font-size: 24px;
  margin-right: 8px;
}

.location-info {
  margin-bottom: 15px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.location-details {
  margin-bottom: 15px;
}

.location-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.location-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 600;
  color: #606266;
  min-width: 70px;
}

.value {
  color: #333;
  flex: 1;
}

.map-actions {
  display: flex;
  gap: 10px;
}

.map-container {
  width: 100%;
  height: v-bind(height);
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>