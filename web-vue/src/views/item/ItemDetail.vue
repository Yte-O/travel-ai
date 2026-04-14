<template>
  <div class="item-detail">
    <div v-if="loading" class="loading-container">
      <el-skeleton animated :rows="3" :loading="loading" />
    </div>

    <div v-else-if="!item" class="error-container">
      <el-result
        icon="error"
        title="未找到"
        sub-title="未找到该景点信息"
      >
        <template #extra>
          <el-button type="primary" @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <section class="hero-header" :style="heroStyle">
        <div class="hero-overlay"></div>

        <div class="hero-topbar">
          <el-button @click="goBack" class="hero-back-button" plain>
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>

          <div class="hero-actions">
            <el-button :icon="Share" @click="handleCopyLink">分享</el-button>
            <el-button type="primary" @click="showReservationDialog">预约</el-button>
            <el-button v-if="item.fileUrl" :icon="Download" @click="handleDownload">附件</el-button>
          </div>
        </div>

        <div class="hero-content">
          <div class="hero-category">{{ item.category?.name || '未分类' }}</div>
          <h1 class="hero-title">{{ item.title }}</h1>

          <div class="hero-tags" v-if="(item as any).tagList?.length">
            <span v-for="tag in (item as any).tagList" :key="tag" class="hero-tag">{{ tag }}</span>
          </div>

          <div class="hero-meta">
            <span>上传者 {{ item.userRealName || '未知' }}</span>
            <span>更新于 {{ formatDate(item.updateTime) }}</span>
          </div>
        </div>
      </section>

      <section class="reading-layout">
        <main class="reading-left">
          <blockquote class="lead-quote">
            {{ item.description || '这是一处值得驻足停留的目的地。' }}
          </blockquote>

          <div class="interaction-bar">
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

          <section class="metrics-matrix">
            <div class="metric-block metric-price" v-if="getSpecValue('ticketPrice')">
              <div class="metric-label">
                <el-icon><Money /></el-icon>
                <span>门票价格</span>
              </div>
              <div class="metric-value">{{ getSpecValue('ticketPrice') }}</div>
            </div>

            <div class="metric-block" v-if="getSpecValue('openTime')">
              <div class="metric-label">
                <el-icon><Clock /></el-icon>
                <span>开放时间</span>
              </div>
              <div class="metric-value metric-plain">{{ getSpecValue('openTime') }}</div>
            </div>

            <div class="metric-block" v-if="getSpecValue('traffic')">
              <div class="metric-label">
                <el-icon><Van /></el-icon>
                <span>交通信息</span>
              </div>
              <div class="metric-value metric-plain">{{ getSpecValue('traffic') }}</div>
            </div>

            <div class="metric-block" v-if="getFeatures().length">
              <div class="metric-label">
                <el-icon><Star /></el-icon>
                <span>景点特色</span>
              </div>
              <div class="feature-cloud">
                <span v-for="feature in getFeatures()" :key="feature" class="feature-pill">{{ feature }}</span>
              </div>
            </div>
          </section>

          <section class="comments-section">
            <div class="comments-title-row">
              <h2>评论 {{ commentCount }}</h2>
            </div>
            <CommentList :item-id="Number(item.id)" @update-count="updateCommentCount" />
          </section>
        </main>

        <aside class="reading-right">
          <div class="sticky-location">
            <div class="location-title">
              <el-icon><MapLocation /></el-icon>
              <span>位置与导航</span>
            </div>

            <p class="location-address">{{ getSpecValue('address') || '暂无地址信息' }}</p>

            <div class="location-coord" v-if="hasLocation()">
              经纬度 {{ getLocationData().latitude }}, {{ getLocationData().longitude }}
            </div>

            <MapViewer
              v-if="hasLocation()"
              :latitude="getLocationData().latitude"
              :longitude="getLocationData().longitude"
              height="360px"
            />
          </div>
        </aside>
      </section>
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

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { itemApi } from '@/api/item'
import { likeApi } from '@/api/like'
import { favoriteApi } from '@/api/favorite'
import { addUserAction, type UserActionData } from '@/api/userAction'
import type { ItemVO, ItemUpdateDTO } from '@/types/item'
import { ArrowLeft, Download, Share, Money, Star, Clock, Van, MapLocation } from '@element-plus/icons-vue'
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

const heroStyle = computed(() => {
  const cover = item.value?.coverUrl
  if (!cover) {
    return {
      background: 'linear-gradient(120deg, #4b5563 0%, #1f2937 100%)'
    }
  }
  return {
    backgroundImage: `linear-gradient(rgba(6, 12, 22, 0.42), rgba(6, 12, 22, 0.58)), url(${cover})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center'
  }
})



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

// 获取景点特色数组
const getFeatures = (): string[] => {
  const features = getSpecValue('features')
  if (Array.isArray(features)) {
    return features
  }
  return []
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
  min-height: 100%;
  background: #f9fafb;
}

.loading-container,
.error-container {
  display: flex;
  justify-content: center;
  padding: 72px 0;
}

.hero-header {
  position: relative;
  width: 100%;
  height: 40vh;
  min-height: 320px;
  padding: 24px clamp(20px, 5vw, 72px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
  background-repeat: no-repeat;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(5, 8, 16, 0.12) 0%, rgba(5, 8, 16, 0.55) 100%);
}

.hero-topbar,
.hero-content {
  position: relative;
  z-index: 1;
}

.hero-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-back-button {
  background: rgba(255, 255, 255, 0.18);
  border: none;
  color: #ffffff;
}

.hero-actions {
  display: flex;
  gap: 10px;
}

.hero-category {
  display: inline-flex;
  font-size: 14px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.9;
  margin-bottom: 10px;
}

.hero-title {
  margin: 0;
  font-size: clamp(30px, 6vw, 56px);
  line-height: 1.08;
  font-weight: 800;
}

.hero-tags {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-tag {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 13px;
}

.hero-meta {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

.reading-layout {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px clamp(20px, 5vw, 72px) 56px;
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1fr);
  gap: 38px;
}

.lead-quote {
  margin: 0;
  padding: 0;
  border: none;
  font-size: clamp(22px, 2.8vw, 34px);
  line-height: 1.45;
  font-weight: 650;
  color: #111827;
}

.interaction-bar {
  margin-top: 22px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.metrics-matrix {
  margin-top: 34px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.metric-block {
  background: #ffffff;
  padding: 18px 20px;
  border-radius: 18px;
}

.metric-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 14px;
}

.metric-value {
  margin-top: 10px;
  font-size: clamp(30px, 4vw, 46px);
  line-height: 1;
  font-weight: 800;
  color: #111827;
}

.metric-plain {
  font-size: 18px;
  line-height: 1.45;
  font-weight: 600;
  color: #1f2937;
}

.metric-price .metric-value {
  color: #d9480f;
}

.feature-cloud {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-pill {
  font-size: 13px;
  color: #1f2937;
  background: #eef2f7;
  padding: 6px 10px;
  border-radius: 999px;
}

.comments-section {
  margin-top: 36px;
  background: #ffffff;
  border-radius: 20px;
  padding: 22px;
}

.comments-title-row h2 {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 800;
  color: #111827;
}

.sticky-location {
  position: sticky;
  top: 24px;
  background: #ffffff;
  border-radius: 20px;
  padding: 22px;
}

.location-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.location-address {
  margin: 14px 0 8px;
  color: #374151;
  line-height: 1.65;
}

.location-coord {
  margin-bottom: 14px;
  color: #9ca3af;
  font-size: 13px;
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

@media (max-width: 1200px) {
  .reading-layout {
    grid-template-columns: 1fr;
  }

  .sticky-location {
    position: static;
  }
}

@media (max-width: 768px) {
  .hero-header {
    min-height: 280px;
    height: 38vh;
  }

  .hero-topbar {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .metrics-matrix {
    grid-template-columns: 1fr;
  }

  .comments-section,
  .sticky-location {
    padding: 16px;
    border-radius: 16px;
  }
}
</style> 