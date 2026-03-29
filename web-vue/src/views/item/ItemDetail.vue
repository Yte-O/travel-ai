<template>
  <div class="item-detail">
    <div v-if="loading" class="loading-container">
      <el-skeleton animated :rows="3" :loading="loading" />
    </div>

    <div v-else-if="!item" class="error-container">
      <el-result 
        icon="error" 
        title="未找到"
        sub-title="未找到该景点信息">
        <template #extra>
          <el-button type="primary" @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <!-- 顶部导航条 -->
      <div class="item-header">
        <el-button @click="goBack" class="back-button">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="item-actions">
          <el-button type="primary" :icon="Share" @click="handleCopyLink" class="action-button">
            分享
          </el-button>
          <el-button 
            type="success" 
            @click="showReservationDialog" 
            class="action-button"
          >
            预约
          </el-button>
        </div>
      </div>
      
      <el-card class="item-card">
        <div class="item-content">
          <!-- 左侧 - 景点封面和下载区域 -->
          <div class="item-left-panel">
            <div class="item-cover-container">
              <el-image 
                v-if="item.coverUrl" 
                :src="item.coverUrl" 
                class="item-cover"
                fit="cover"
                :preview-src-list="[item.coverUrl]"
                :initial-index="0"
                :hide-on-click-modal="false"
                preview-teleported
              />
              <div v-else class="no-image-placeholder">
                <el-icon><Picture /></el-icon>
                <span>暂无封面</span>
              </div>
            </div>
            
            <div class="item-download-section" v-if="item.fileUrl">
              <el-button type="primary" @click="handleDownload" class="download-button">
                <el-icon><Download /></el-icon>
                下载附件
              </el-button>
            </div>
            
            <div class="item-interaction">
              <LikeButton 
                :itemId="item.id" 
                v-model:isLiked="isLiked" 
                v-model:likeCount="likeCount"
                @like="onLike"
                @unlike="onUnlike"
                size="large"
              />
              <FavoriteButton
                :itemId="item.id"
                v-model:isFavorite="isFavorite"
                @favorite="onFavorite"
                @unfavorite="onUnfavorite"
                size="large"
              />
            </div>
          </div>
          
          <!-- 右侧 - 景点信息 -->
          <div class="item-info">
            <h1 class="item-title">{{ item.title }}</h1>
            
            <div class="item-meta">
              <div class="meta-item">
                <span class="meta-label">所属类别</span>
                <el-tag size="large" effect="plain" class="category-tag">{{ item.category.name }}</el-tag>
              </div>
              
              <div class="meta-item">
                <span class="meta-label">发布者</span>
                <span class="meta-value">{{ item.userRealName || '未知' }}</span>
              </div>
              
              <div class="meta-item" v-if="getSpecValue('ticketPrice')">
                <span class="meta-label">门票价格</span>
                <el-tag 
                  type="primary"
                  size="large"
                  effect="plain"
                >
                  {{ getSpecValue('ticketPrice') }}
                </el-tag>
              </div>
              
              <div class="meta-timestamps">
                <div class="timestamp-item">
                  <el-icon><Calendar /></el-icon>
                  <span>创建于 {{ formatDate(item.createTime) }}</span>
                </div>
                <div class="timestamp-item">
                  <el-icon><Timer /></el-icon>
                  <span>更新于 {{ formatDate(item.updateTime) }}</span>
                </div>
              </div>
            </div>
            
            <div class="item-description-section">
              <h3 class="section-title">描述</h3>
              <div class="item-description">
                <p v-if="item.description">{{ item.description }}</p>
                <p v-else class="no-data">暂无描述</p>
              </div>
            </div>
            
            <div class="item-tags-section">
              <h3 class="section-title">标签</h3>
              <div class="item-tags">
                <el-tag 
                  v-for="tag in (item as any).tagList || []" 
                  :key="tag" 
                  size="large" 
                  effect="light"
                  type="success" 
                  class="tag" 
                  round
                >
                  {{ tag }}
                </el-tag>
                <span v-if="!(item as any).tagList || (item as any).tagList.length === 0" class="no-data">暂无标签</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      
      <!-- 景点信息卡片 -->
      <el-card class="product-specs-card" v-if="item.extraData">
        <template #header>
          <div class="specs-header">
            <el-icon class="specs-icon"><InfoFilled /></el-icon>
            <span class="specs-title">景点信息</span>
          </div>
        </template>
        <div class="specs-content">
          <div class="specs-grid">
            <!-- 第一行：门票价格、开放时间、游玩时间、最佳季节 -->
            <div class="specs-row">
              <!-- 门票价格 -->
              <div class="spec-item price-item" v-if="getSpecValue('ticketPrice')">
                <div class="spec-label">
                  <el-icon><Money /></el-icon>
                  <span>门票价格</span>
                </div>
                <div class="spec-value price-value">
                  {{ getSpecValue('ticketPrice') }}
                </div>
              </div>
              
              <!-- 开放时间 -->
              <div class="spec-item" v-if="getSpecValue('openTime')">
                <div class="spec-label">
                  <el-icon><Clock /></el-icon>
                  <span>开放时间</span>
                </div>
                <div class="spec-value">
                  {{ getSpecValue('openTime') }}
                </div>
              </div>
              
              <!-- 游玩时间 -->
              <div class="spec-item" v-if="getSpecValue('playTime')">
                <div class="spec-label">
                  <el-icon><Timer /></el-icon>
                  <span>游玩时间</span>
                </div>
                <div class="spec-value">
                  {{ getSpecValue('playTime') }}
                </div>
              </div>
              
              <!-- 最佳季节 -->
              <div class="spec-item" v-if="getSpecValue('bestSeason')">
                <div class="spec-label">
                  <el-icon><Sunny /></el-icon>
                  <span>最佳季节</span>
                </div>
                <div class="spec-value">
                  {{ getSpecValue('bestSeason') }}
                </div>
              </div>
            </div>
            
            <!-- 第二行：景点地址、交通信息、景点特色 -->
            <div class="specs-row">
              <!-- 景点地址 -->
              <div class="spec-item" v-if="getSpecValue('address')">
                <div class="spec-label">
                  <el-icon><Location /></el-icon>
                  <span>景点地址</span>
                </div>
                <div class="spec-value">
                  {{ getSpecValue('address') }}
                </div>
              </div>
              
              <!-- 交通信息 -->
              <div class="spec-item traffic-item" v-if="getSpecValue('traffic')">
                <div class="spec-label">
                  <el-icon><Van /></el-icon>
                  <span>交通信息</span>
                </div>
                <div class="spec-value traffic-value">
                  <el-tooltip 
                    :content="getSpecValue('traffic')" 
                    placement="top" 
                    :disabled="!isTrafficLong()"
                    :show-after="500"
                  >
                    <div class="traffic-text" :class="{ 'truncated': isTrafficLong() }">
                      {{ getSpecValue('traffic') }}
                    </div>
                  </el-tooltip>
                </div>
              </div>
              
              <!-- 景点特色 -->
              <div class="spec-item features-item" v-if="getFeatures().length > 0">
                <div class="spec-label">
                  <el-icon><Star /></el-icon>
                  <span>景点特色</span>
                </div>
                <div class="spec-value features-value">
                  <div class="features-tags">
                    <el-tag 
                      v-for="feature in getFeatures()" 
                      :key="feature" 
                      size="small" 
                      effect="light"
                      type="primary"
                      class="feature-tag"
                    >
                      {{ feature }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 其他规格信息 -->
            <template v-for="(value, key) in getOtherSpecs()" :key="key">
              <div class="spec-item">
                <div class="spec-label">
                  <el-icon><Document /></el-icon>
                  <span>{{ formatKey(key) }}</span>
                </div>
                <div class="spec-value">
                  <span v-if="Array.isArray(value)">{{ value.join(', ') }}</span>
                  <span v-else-if="typeof value === 'object'">
                    <el-popover placement="top" :width="300" trigger="hover">
                      <template #reference>
                        <el-button type="text" size="small">查看详情</el-button>
                      </template>
                      <pre class="json-preview">{{ JSON.stringify(value, null, 2) }}</pre>
                    </el-popover>
                  </span>
                  <span v-else>{{ value }}</span>
                </div>
              </div>
            </template>
          </div>
        </div>
      </el-card>
      
      <!-- 地图位置 -->
      <el-card class="map-card" v-if="item && hasLocation()">
        <template #header>
          <div class="map-header">
            <el-icon><MapLocation /></el-icon>
            <span>景点位置</span>
          </div>
        </template>
        <MapViewer 
          :latitude="getLocationData().latitude"
          :longitude="getLocationData().longitude"
          height="400px"
        />
      </el-card>
      
      <!-- 评论区域 -->
      <el-card class="comments-card" v-if="item">
        <template #header>
          <div class="comments-header">
            <h2 class="comments-title">评论（{{ commentCount }}）</h2>
          </div>
        </template>
        <CommentList :item-id="Number(item.id)" @update-count="updateCommentCount" />
      </el-card>
    </template>

    <!-- 预约表单对话框 -->
    <el-dialog
      v-model="purchaseDialogVisible"
      title="确认预约"
      width="500px"
      :before-close="handleReservationDialogClose"
    >
      <el-form
        ref="purchaseFormRef"
        :model="purchaseForm"
        :rules="purchaseRules"
        label-width="100px"
      >
        <div class="purchase-item-info">
          <div class="item-summary">
            <el-image
              v-if="item?.coverUrl"
              :src="item.coverUrl"
              class="item-thumbnail"
              fit="cover"
            />
            <div class="item-details">
              <h4>{{ item?.title }}</h4>
              <p class="item-price">￥{{ getItemPrice() }}</p>
            </div>
          </div>
        </div>
        
        <el-form-item label="成人数量" prop="adults">
          <el-input-number
            v-model="purchaseForm.adults"
            :min="1"
            :max="99"
            controls-position="right"
            @change="calculateTotalPrice"
          />
        </el-form-item>
        
        <el-form-item label="儿童数量" prop="children">
          <el-input-number
            v-model="purchaseForm.children"
            :min="0"
            :max="99"
            controls-position="right"
            @change="calculateTotalPrice"
          />
          <span class="children-info">儿童票价为成人票价7折</span>
        </el-form-item>
        
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="purchaseForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
            <el-option label="支付宝" value="支付宝" />
            <el-option label="微信支付" value="微信支付" />
            <el-option label="信用卡" value="信用卡" />
            <el-option label="花呗" value="花呗" />
            <el-option label="京东白条" value="京东白条" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="出行日期" prop="travelDate">
          <el-date-picker
            v-model="purchaseForm.travelDate"
            type="date"
            placeholder="请选择出行日期"
            :disabled-date="(time: Date) => time.getTime() < Date.now() - 24 * 60 * 60 * 1000"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="联系信息" prop="contactInfo">
          <el-input
            v-model="purchaseForm.contactInfo"
            placeholder="请输入姓名和手机号，如：张先生 13800138001"
          />
        </el-form-item>
        
        <div class="price-summary">
          <div class="price-item">
            <span>成人单价：</span>
            <span>￥{{ getItemPrice() }}</span>
          </div>
          <div class="price-item">
            <span>儿童单价：</span>
            <span>￥{{ Math.round(getItemPrice() * 0.7) }}</span>
          </div>
          <div class="price-item">
            <span>成人数量：</span>
            <span>{{ purchaseForm.adults }}人</span>
          </div>
          <div class="price-item">
            <span>儿童数量：</span>
            <span>{{ purchaseForm.children }}人</span>
          </div>
          <div class="price-item total-price">
            <span>总价：</span>
            <span>￥{{ purchaseForm.totalPrice }}</span>
          </div>
        </div>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="purchaseDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmReservation" 
            :loading="purchaseLoading"
          >
            确认预约
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 全局图片查看器容器，确保预览组件能够正确显示 -->
    <div class="image-viewer-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { itemApi } from '@/api/item'
import { likeApi } from '@/api/like'
import { favoriteApi } from '@/api/favorite'
import { addUserAction, type UserActionData } from '@/api/userAction'
import type { ItemVO, ItemUpdateDTO } from '@/types/item'
import { ArrowLeft, Download, Share, Picture, Calendar, Timer, InfoFilled, Money, Star, Clock, Location, Sunny, Van, MapLocation } from '@element-plus/icons-vue'
import LikeButton from '@/components/LikeButton.vue'
import FavoriteButton from '@/components/FavoriteButton.vue'
import CommentList from '@/components/CommentList.vue'
import MapViewer from '@/components/MapViewer.vue'

const route = useRoute()
const router = useRouter()
const item = ref<ItemVO | null>(null)
const loading = ref(true)
const isLiked = ref(false)
const likeCount = ref(0)
const isFavorite = ref(false)
const commentCount = ref(0)



// 预约相关响应式数据
const purchaseDialogVisible = ref(false)
const purchaseLoading = ref(false)
const purchaseFormRef = ref<FormInstance>()
const purchaseForm = ref({
  adults: 1,
  children: 0,
  paymentMethod: '',
  travelDate: '',
  contactInfo: '',
  totalPrice: 0
})

// 预约表单验证规则
const purchaseRules: FormRules = {
  adults: [
    { required: true, message: '请输入成人数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '成人数量至少为1', trigger: 'blur' }
  ],
  children: [
    { type: 'number', min: 0, message: '儿童数量不能为负数', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ],
  travelDate: [
    { required: true, message: '请选择出行日期', trigger: 'change' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系信息', trigger: 'blur' },
    { min: 5, message: '联系信息至少5个字符', trigger: 'blur' }
  ]
}

// 获取景点详情
const fetchItemDetail = async () => {
  try {
    loading.value = true
    const data = await itemApi.getById(Number(route.params.id))
    item.value = data
    
    // 设置标签列表
    if (item.value) {
      // 用any类型绕过类型检查
      const itemAny = item.value as any
      if (typeof itemAny.tags === 'string') {
        itemAny.tagList = itemAny.tags.split(',')
      } else if (Array.isArray(itemAny.tags)) {
        itemAny.tagList = itemAny.tags
      } else {
        itemAny.tagList = []
      }
    }
    
    // 记录浏览历史
    addViewRecord(item.value.id)
  } catch (error) {
    console.error('获取景点详情失败', error)
    ElMessage.error('获取景点详情失败')
  } finally {
    loading.value = false
  }
}

// 解析额外数据JSON
const parseExtraData = (extraDataStr: string): Record<string, any> => {
  if (!extraDataStr) return {}
  try {
    return JSON.parse(extraDataStr)
  } catch (e) {
    console.error('解析额外数据失败:', e)
    return { raw: extraDataStr }
  }
}

// 格式化属性名称
const formatKey = (key: string): string => {
  // 将驼峰命名或下划线命名转换为空格分隔的单词，并首字母大写
  return key
    .replace(/([A-Z])/g, ' $1') // 在大写字母前添加空格
    .replace(/_/g, ' ') // 将下划线替换为空格
    .replace(/^\s+/, '') // 移除开头的空格
    .replace(/^./, (str: string) => str.toUpperCase()) // 首字母大写
}

// 添加浏览记录
const addViewRecord = async (itemId: number): Promise<void> => {
  try {
    const actionData: UserActionData = {
      itemId: itemId,
      actionType: 0, // 0表示浏览
      extraData: JSON.stringify({
        viewTime: new Date().toISOString(),
        source: '浏览器' // 来源统一为浏览器
      })
    }
    await addUserAction(actionData)
  } catch (error) {
    console.error('添加浏览记录失败', error)
    // 不向用户提示错误，静默处理
  }
}

// 获取景点门票价格
const getItemPrice = (): number => {
  const ticketPrice = getSpecValue('ticketPrice')
  if (!ticketPrice) return 0
  
  // 从价格字符串中提取数字，例如："248元" -> 248
  const priceMatch = ticketPrice.match(/\d+/)
  return priceMatch ? Number(priceMatch[0]) : 0
}


// 获取规格值
const getSpecValue = (key: string): any => {
  if (!item.value || !item.value.extraData) return null
  try {
    const extraData = JSON.parse(item.value.extraData)
    return extraData[key]
  } catch (e) {
    return null
  }
}

// 格式化价格
const formatPrice = (price: number): string => {
  return price.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 获取景点特色数组
const getFeatures = (): string[] => {
  const features = getSpecValue('features')
  if (Array.isArray(features)) {
    return features
  }
  return []
}

// 判断交通信息是否过长
const isTrafficLong = (): boolean => {
  const traffic = getSpecValue('traffic')
  return traffic && traffic.length > 80
}

// 获取其他规格信息（排除已单独显示的字段）
const getOtherSpecs = (): Record<string, any> => {
  if (!item.value || !item.value.extraData) return {}
  try {
    const extraData = JSON.parse(item.value.extraData)
    const excludeKeys = ['ticketPrice', 'openTime', 'address', 'traffic', 'bestSeason', 'playTime', 'features', 'latitude', 'longitude']
    const otherSpecs: Record<string, any> = {}
    
    Object.keys(extraData).forEach(key => {
      if (!excludeKeys.includes(key)) {
        otherSpecs[key] = extraData[key]
      }
    })
    
    return otherSpecs
  } catch (e) {
    return {}
  }
}

// 检查是否有位置信息
const hasLocation = (): boolean => {
  const latitude = getSpecValue('latitude')
  const longitude = getSpecValue('longitude')
  return latitude !== null && longitude !== null && latitude !== undefined && longitude !== undefined
}

// 获取位置数据
const getLocationData = () => {
  return {
    latitude: getSpecValue('latitude'),
    longitude: getSpecValue('longitude')
  }
}

// 计算总价
const calculateTotalPrice = () => {
  const unitPrice = getItemPrice()
  const totalPersons = purchaseForm.value.adults + purchaseForm.value.children * 0.7 // 儿童票7折
  purchaseForm.value.totalPrice = Math.round(unitPrice * totalPersons)
}

// 显示预约对话框
const showReservationDialog = () => {
  if (!item.value) return
  
  
  // 初始化表单数据
  purchaseForm.value = {
    adults: 1,
    children: 0,
    paymentMethod: '',
    travelDate: '',
    contactInfo: '',
    totalPrice: getItemPrice()
  }
  
  purchaseDialogVisible.value = true
}

// 处理预约对话框关闭
const handleReservationDialogClose = (done: () => void) => {
  if (purchaseLoading.value) {
    ElMessage.warning('正在处理预约请求，请稍候...')
    return
  }
  done()
}

// 确认预约
const confirmReservation = async () => {
  if (!purchaseFormRef.value || !item.value) return
  
  try {
    // 验证表单
    await purchaseFormRef.value.validate()
    
    
    purchaseLoading.value = true
    
    // 生成预约号（与init.sql格式一致）
    const now = new Date()
    const dateStr = now.getFullYear().toString() + 
                   (now.getMonth() + 1).toString().padStart(2, '0') + 
                   now.getDate().toString().padStart(2, '0')
    const timeStr = Date.now().toString().slice(-4) // 取时间戳后4位
    const orderId = `RES${dateStr}${timeStr}`
    
    // 构建预约记录数据
    const actionData: UserActionData = {
      itemId: item.value.id,
      actionType: 1, // 1表示预约
      extraData: JSON.stringify({
        reservationId: orderId,
        adults: purchaseForm.value.adults,
        children: purchaseForm.value.children,
        totalPrice: purchaseForm.value.totalPrice,
        paymentMethod: purchaseForm.value.paymentMethod,
        travelDate: purchaseForm.value.travelDate,
        contactInfo: purchaseForm.value.contactInfo
      })
    }
    
    const result = await addUserAction(actionData)
    if (result) {
      
      ElMessage.success(`预约成功！预约号：${orderId}`)
      purchaseDialogVisible.value = false
      
      // 可以选择跳转到预约页面或刷新页面
      // router.push('/user/purchase-history')
    } else {
      ElMessage.error('预约失败，请稍后重试')
    }
  } catch (error) {
    console.error('预约失败', error)
    ElMessage.error('预约失败，请稍后重试')
  } finally {
    purchaseLoading.value = false
  }
}


// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
}

// 下载景点资料
const handleDownload = () => {
  if (!item.value || !item.value.fileUrl) {
    ElMessage.warning('无可下载文件')
    return
  }
  
  // 使用blob下载方式，确保在当前页面下载
  ElMessage.info('正在准备文件，请稍候...')
  
  // 创建xhr请求获取文件
  const xhr = new XMLHttpRequest();
  xhr.open('GET', item.value.fileUrl, true);
  xhr.responseType = 'blob';
  
  xhr.onload = function() {
    if (xhr.status === 200) {
      // 获取文件名
      const fileName = getFileNameFromUrl(item.value!.fileUrl);
      
      // 创建Blob链接
      const blob = new Blob([xhr.response]);
      const url = window.URL.createObjectURL(blob);
      
      // 创建下载链接
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      link.style.display = 'none';
      document.body.appendChild(link);
      
      // 触发下载
      link.click();
      
      // 清理
      window.URL.revokeObjectURL(url);
      document.body.removeChild(link);
      
      ElMessage.success('文件下载已开始');
    } else {
      ElMessage.error('下载失败，请稍后再试');
    }
  };
  
  xhr.onerror = function() {
    ElMessage.error('下载失败，请检查网络连接');
  };
  
  xhr.send();
}

// 从URL中获取文件名
const getFileNameFromUrl = (url: string): string => {
  const pathParts = url.split('/');
  let fileName = pathParts[pathParts.length - 1];
  
  // 如果URL中有查询参数，去除
  if (fileName.includes('?')) {
    fileName = fileName.split('?')[0];
  }
  
  // 如果没有扩展名，添加一个通用扩展名
  if (!fileName.includes('.')) {
    fileName += '.bin';
  }
  
  return fileName;
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 复制链接
const handleCopyLink = () => {
  const url = window.location.href
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制，可以分享给好友了')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 点赞回调
const onLike = () => {
  ElMessage.success('点赞成功')
}

// 取消点赞回调
const onUnlike = () => {
  ElMessage.success('已取消点赞')
}

// 收藏回调
const onFavorite = () => {
  ElMessage.success('收藏成功')
}

// 取消收藏回调
const onUnfavorite = () => {
  ElMessage.success('已取消收藏')
}

// 更新评论数量
const updateCommentCount = (count: number) => {
  commentCount.value = count
}

onMounted(() => {
  fetchItemDetail()
})
</script>

<style scoped>
.item-detail {
  width: 100%;
  max-width: 1500px;
  margin: 0 auto;
  padding: 10px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 5px;
}

.item-actions {
  display: flex;
  gap: 10px;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 5px;
}

.item-card {
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  background-color: #fff;
  margin-bottom: 30px;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.item-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.item-content {
  display: flex;
  flex-direction: column;
  padding: 10px;
}

.item-left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.item-cover-container {
  width: 100%;
  height: 300px;
  overflow: hidden;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background-color: #f5f7fa;
  position: relative;
}

.item-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.item-cover:hover {
  transform: scale(1.05);
}

.item-download-section {
  display: flex;
  justify-content: center;
}

.download-button {
  width: 100%;
  border-radius: 8px;
  height: 44px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.item-interaction {
  display: flex;
  justify-content: space-evenly;
  margin-top: 10px;
}

.item-info {
  padding: 10px;
}

.item-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  line-height: 1.3;
}

.item-meta {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 30px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.meta-label {
  font-size: 14px;
  color: #909399;
  min-width: 70px;
}

.meta-value {
  font-size: 15px;
  color: #606266;
}

.category-tag {
  font-size: 15px;
  padding: 6px 12px;
}

.meta-timestamps {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.timestamp-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  border-left: 4px solid #409eff;
  padding-left: 10px;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 18px;
  background-color: #409eff;
  margin-right: 10px;
  border-radius: 2px;
  display: none;
}

.item-description-section {
  margin-bottom: 30px;
}

.item-description {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  white-space: pre-wrap;
}

.item-tags-section {
  margin-bottom: 30px;
}

.item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag {
  padding: 6px 14px;
  font-size: 14px;
}

.no-data {
  color: #909399;
  font-style: italic;
}

.no-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 16px;
}

.no-image-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: #c0c4cc;
}

.loading-container, .error-container {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}

/* 响应式布局 */
@media (min-width: 768px) {
  .item-content {
    flex-direction: row;
    gap: 30px;
    padding: 20px;
  }
  
  .item-left-panel {
    width: 40%;
    margin-bottom: 0;
  }
  
  .item-cover-container {
    height: 400px;
  }
  
  .item-info {
    width: 60%;
    padding: 0;
  }
  
  .item-title {
    font-size: 28px;
  }
  
  .meta-timestamps {
    flex-direction: row;
    gap: 30px;
  }
}

@media (min-width: 1200px) {
  .item-detail {
    padding: 20px;
  }
  
  .item-content {
    padding: 30px;
  }
  
  .item-cover-container {
    height: 450px;
  }
  
  .item-title {
    font-size: 32px;
  }
}

/* 图片预览组件样式修复 */
.image-viewer-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 0;
  height: 0;
  z-index: 2009;
  pointer-events: none;
}

:deep(.el-image-viewer__mask) {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  opacity: 0.5;
  background: #000;
  z-index: 2010;
}

:deep(.el-image-viewer__wrapper) {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 2011;
}

:deep(.el-image-viewer__close) {
  z-index: 2012;
}

:deep(.el-image-viewer__canvas) {
  z-index: 2011;
}

:deep(.el-image-viewer__actions) {
  z-index: 2012;
}

:deep(.el-image-viewer__prev), 
:deep(.el-image-viewer__next) {
  z-index: 2012;
}

:deep(.el-image-viewer__btn) {
  z-index: 2012;
}

.comments-card {
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  background-color: #fff;
  margin-bottom: 30px;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comments-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

/* 景点信息卡片样式 */
.product-specs-card {
  border-radius: 12px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
  background-color: #fff;
  margin-bottom: 30px;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.product-specs-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.specs-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.specs-icon {
  color: #409eff;
  font-size: 18px;
}

.specs-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.specs-content {
  padding: 0;
}

.specs-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
}

.specs-row {
  display: grid;
  gap: 20px;
}

/* 第一行：4列平均分布 */
.specs-row:nth-child(1) {
  grid-template-columns: repeat(4, 1fr);
}

/* 第二行：3列平均分布 */
.specs-row:nth-child(2) {
  grid-template-columns: repeat(3, 1fr);
}

/* 其他行：自适应列数 */
.specs-row:nth-child(n+3) {
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.spec-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 10px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.spec-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, #409eff, #67c23a);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.spec-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.spec-item:hover::before {
  opacity: 1;
}

.spec-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

.spec-label .el-icon {
  color: #909399;
  font-size: 16px;
  transition: color 0.3s ease;
}

.spec-item:hover .spec-label .el-icon {
  color: #409eff;
}

.spec-value {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  text-align: right;
  max-width: 60%;
  word-break: break-word;
}

/* 特殊样式 */
.price-item {
  background: linear-gradient(135deg, #fff5f5 0%, #ffffff 100%);
  border-color: #f56c6c;
}

.price-item::before {
  background: linear-gradient(to bottom, #f56c6c, #e6a23c);
}

.price-value {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}

.stock-item .spec-value .el-tag {
  font-weight: 600;
}

.sales-value {
  color: #67c23a;
  font-weight: 700;
}

/* 规格参数样式 */
.specs-item {
  grid-column: span 2; /* 占据两列宽度 */
  min-width: 400px; /* 最小宽度确保内容显示 */
  flex-direction: row !important; /* 强制水平排列 */
  align-items: center !important; /* 垂直居中对齐 */
}

.specs-item .spec-label {
  flex-direction: row !important; /* 确保标签也是水平排列 */
  align-items: center !important;
  white-space: nowrap; /* 防止标签换行 */
  margin-right: 20px; /* 增加标签和内容之间的间距 */
  flex-shrink: 0; /* 防止标签被压缩 */
}

.specs-value {
  text-align: left;
  flex: 1; /* 占据剩余空间 */
  min-width: 0; /* 允许内容区域收缩 */
}

.specs-text {
  line-height: 1.6;
  color: #606266;
  font-size: 14px;
  white-space: nowrap; /* 始终单行显示 */
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%; /* 确保省略号有足够空间显示 */
  display: block; /* 确保为块级元素 */
}

.specs-text.truncated {
  cursor: help;
}

/* 景点特色标签样式 */
.features-value {
  width: 100%;
}

.features-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.feature-tag {
  margin: 0;
}

/* 交通信息样式 */
.traffic-item {
  /* 交通信息在第二行与其他元素平等显示 */
}

.traffic-value {
  width: 100%;
}

.traffic-text {
  line-height: 1.5;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
  width: 100%;
  display: block;
}

.traffic-text.truncated {
  cursor: help;
}

/* JSON预览样式 */
.json-preview {
  background-color: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  font-size: 12px;
  color: #606266;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  /* 第一行在中等屏幕上变为2列 */
  .specs-row:nth-child(1) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  /* 第二行在中等屏幕上保持3列 */
  .specs-row:nth-child(2) {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  /* 第二行在较小屏幕上变为2列 */
  .specs-row:nth-child(2) {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .specs-grid {
    gap: 15px;
    padding: 15px;
  }
  
  .specs-row,
  .specs-row:nth-child(1),
  .specs-row:nth-child(2),
  .specs-row:nth-child(n+3) {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .spec-item {
    padding: 12px 16px;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .spec-value {
    text-align: left;
    max-width: 100%;
  }
  
  .price-value {
    font-size: 16px;
  }
  
  /* 移动端规格参数样式调整 */
  .specs-item {
    padding: 16px;
    grid-column: 1 / -1; /* 移动端占满整行 */
    min-width: auto; /* 移动端取消最小宽度限制 */
    flex-direction: row !important; /* 移动端也保持水平排列 */
    align-items: center !important;
  }
  
  .specs-item .spec-label {
    flex-direction: row !important; /* 移动端标签也保持水平排列 */
    align-items: center !important;
    white-space: nowrap;
    margin-right: 15px; /* 移动端适当减少间距 */
    flex-shrink: 0; /* 防止标签被压缩 */
  }
  
  .specs-text {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .specs-grid {
    padding: 10px;
  }
  
  .spec-item {
    padding: 10px 12px;
  }
}

/* 预约对话框样式 */
.purchase-item-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.item-summary {
  display: flex;
  align-items: center;
  gap: 15px;
}

.item-thumbnail {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  flex-shrink: 0;
}

.item-details h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.item-price {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #e74c3c;
}

.stock-info {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.stock-info.out-of-stock {
  color: #f56c6c;
  font-weight: 600;
}

.children-info {
  margin-left: 10px;
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
}

.price-summary {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.price-item:last-child {
  margin-bottom: 0;
}

.total-price {
  border-top: 1px dashed #e0e0e0;
  padding-top: 8px;
  margin-top: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #e74c3c;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 5%;
  }
  
  .item-summary {
    flex-direction: column;
    align-items: flex-start;
    text-align: center;
  }
  
  .item-thumbnail {
    align-self: center;
  }
}

/* 地图卡片样式 */
.map-card {
  margin-bottom: 20px;
}

.map-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.map-header .el-icon {
  color: #409eff;
  font-size: 18px;
}
</style> 