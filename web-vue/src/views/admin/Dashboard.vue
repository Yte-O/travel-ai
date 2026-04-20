<!-- 管理员统计仪表盘 -->
<template>
  <div class="admin-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1>数据统计仪表盘</h1>
          <p>实时展示系统运营数据和用户行为分析</p>
        </div>
        <div class="header-controls">
          <div class="data-mode-switch">
            <el-tooltip :content="`切换数据模式（当前：${useRealData ? '真实数据' : '模拟数据'}）`" placement="left">
              <el-switch
                v-model="useRealData"
                size="small"
                @change="switchDataMode"
              />
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据概览卡片 -->
    <div class="overview-cards">
      <!-- 用户板块 -->
      <el-card class="overview-card merged-card" v-loading="overviewLoading">
        <div class="card-header">
          <h3>用户</h3>
          <span class="growth-header">较上月增长</span>
        </div>
        <div class="merged-content">
          <div class="metric-item">
            <div class="card-content">
              <div class="card-icon user-icon">
                <svg viewBox="0 0 1024 1024">
                  <path d="M512 74.666667C270.933333 74.666667 74.666667 270.933333 74.666667 512S270.933333 949.333333 512 949.333333 949.333333 753.066667 949.333333 512 753.066667 74.666667 512 74.666667zM512 245.333333c83.2 0 149.333333 66.133333 149.333333 149.333333S595.2 544 512 544s-149.333333-66.133333-149.333333-149.333333S428.8 245.333333 512 245.333333z m0 618.666667c-110.933333 0-209.066667-53.333333-268.8-138.666667 2.133333-89.6 179.2-138.666667 268.8-138.666666s266.666667 49.066667 268.8 138.666666C721.066667 810.666667 622.933333 864 512 864z"/>
                </svg>
              </div>
              <div class="card-info">
                <h3>总用户数</h3>
                <p class="number">{{ overviewData.totalUsers }}</p>
              </div>
            </div>
            <div class="metric-growth" :class="{ 'positive': getGrowthPercent('totalUsers') >= 0, 'negative': getGrowthPercent('totalUsers') < 0 }">
              {{ getGrowthPercent('totalUsers') >= 0 ? '+' : '' }}{{ getGrowthPercent('totalUsers').toFixed(1) }}%
            </div>
          </div>
          <div class="metric-item">
            <div class="card-content">
              <div class="card-icon action-icon">
                <svg viewBox="0 0 1024 1024">
                  <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64z m0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z m5.4-528.9L457.9 408c-5.1 5.1-13.8 1.5-13.8-5.7V294.8c0-8.8 7.2-16 16-16s16 7.2 16 16v64.9l31.1-33.8c6.2-6.8 16.8-6.8 23 0 6.2 6.8 6.2 17.8 0 24.6L517.4 365.1z m-62.2 186.1c-2.1-2.1-2.1-5.5 0-7.6l60.6-60.6c2.1-2.1 5.5-2.1 7.6 0l60.6 60.6c2.1 2.1 2.1 5.5 0 7.6l-60.6 60.6c-2.1 2.1-5.5 2.1-7.6 0l-60.6-60.6z"/>
                </svg>
              </div>
              <div class="card-info">
                <h3>总行为数</h3>
                <p class="number">{{ overviewData.totalActions }}</p>
              </div>
            </div>
            <div class="metric-growth" :class="{ 'positive': getGrowthPercent('totalActions') >= 0, 'negative': getGrowthPercent('totalActions') < 0 }">
              {{ getGrowthPercent('totalActions') >= 0 ? '+' : '' }}{{ getGrowthPercent('totalActions').toFixed(1) }}%
            </div>
          </div>
        </div>
      </el-card>

      <!-- 景点板块 -->
      <el-card class="overview-card merged-card" v-loading="overviewLoading">
        <div class="card-header">
          <h3>景点</h3>
          <span class="growth-header">较上月增长</span>
        </div>
        <div class="merged-content">
          <div class="metric-item">
            <div class="card-content">
              <div class="card-icon item-icon">
                <svg viewBox="0 0 1024 1024">
                  <path d="M832 384H576V128H192v832h640V384z m-128 64v384H256V192h256v256h192z"/>
                </svg>
              </div>
              <div class="card-info">
                <h3>景点总数</h3>
                <p class="number">{{ overviewData.totalItems }}</p>
              </div>
            </div>
            <div class="metric-growth" :class="{ 'positive': getGrowthPercent('totalItems') >= 0, 'negative': getGrowthPercent('totalItems') < 0 }">
              {{ getGrowthPercent('totalItems') >= 0 ? '+' : '' }}{{ getGrowthPercent('totalItems').toFixed(1) }}%
            </div>
          </div>
          <div class="metric-item">
            <div class="card-content">
              <div class="card-icon category-icon">
                <svg viewBox="0 0 1024 1024">
                  <path d="M544 416h416v416H544V416z m64 64v288h288V480H608zM64 64h416v416H64V64z m64 64v288h288V128H128zM64 544h416v416H64V544z m64 64v288h288V608H128z"/>
                </svg>
              </div>
              <div class="card-info">
                <h3>景点类别</h3>
                <p class="number">{{ overviewData.totalCategories }}</p>
              </div>
            </div>
            <div class="metric-growth" :class="{ 'positive': getGrowthPercent('totalCategories') >= 0, 'negative': getGrowthPercent('totalCategories') < 0 }">
              {{ getGrowthPercent('totalCategories') >= 0 ? '+' : '' }}{{ getGrowthPercent('totalCategories').toFixed(1) }}%
            </div>
          </div>
          <div class="metric-item">
            <div class="card-content">
              <div class="card-icon tag-icon">
                <svg viewBox="0 0 1024 1024">
                  <path d="M483.2 790.3L861.4 412c1.7-1.7 2.5-4 2.3-6.3l-25.5-301.4c-.7-7.8-6.1-14.9-13.9-18.1l-34.4-14.6c-9.2-3.9-19.8-1.1-26.3 7.3L565.2 302.1c-5.4 7-5.2 16.9 0.3 23.8l73.2 95.2c3.3 4.3 7.7 7.6 12.6 9.8l59.7 26.8c8.4 3.8 11.8 13.8 8 22.2-2.6 5.8-8.1 9.5-14.5 9.8L569.2 497c-10.9 0.5-21.4 5.6-28.9 14.1l-106.3 120.3c-5.3 6-8.2 13.6-8.2 21.5v150.9c0 4.3 3.5 7.8 7.8 7.8h14.1c4.3 0 7.8-3.5 7.8-7.8V673.3c0-2.9 1.1-5.6 3.1-7.6z"/>
                  <path d="M191.8 420.9c-19.9 0-36.1 16.2-36.1 36.1 0 19.9 16.2 36.1 36.1 36.1s36.1-16.2 36.1-36.1c0-19.9-16.2-36.1-36.1-36.1z"/>
                </svg>
              </div>
              <div class="card-info">
                <h3>标签总数</h3>
                <p class="number">{{ overviewData.totalTags }}</p>
              </div>
            </div>
            <div class="metric-growth" :class="{ 'positive': getGrowthPercent('totalTags') >= 0, 'negative': getGrowthPercent('totalTags') < 0 }">
              {{ getGrowthPercent('totalTags') >= 0 ? '+' : '' }}{{ getGrowthPercent('totalTags').toFixed(1) }}%
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 用户行为趋势图 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <h3>用户行为趋势</h3>
            <el-button-group>
              <el-button 
                :type="actionTrendPeriod === '7' ? 'primary' : 'default'"
                size="small"
                @click="changeActionTrendPeriod('7')"
              >
                7天
              </el-button>
              <el-button 
                :type="actionTrendPeriod === '30' ? 'primary' : 'default'"
                size="small"
                @click="changeActionTrendPeriod('30')"
              >
                30天
              </el-button>
            </el-button-group>
          </div>
        </template>
        <div 
          ref="actionTrendChart" 
          class="chart-container"
          v-loading="actionTrendLoading"
        ></div>
      </el-card>

      <!-- 景点分类分布 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <h3>景点分类分布</h3>
          </div>
        </template>
        <div 
          ref="categoryDistributionChart" 
          class="chart-container"
          v-loading="categoryDistributionLoading"
        ></div>
      </el-card>

      <!-- 用户活跃度分析 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <h3>用户活跃度分析</h3>
          </div>
        </template>
        <div 
          ref="userActivityChart" 
          class="chart-container"
          v-loading="userActivityLoading"
        ></div>
      </el-card>

      <!-- 行为类型对比 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <h3>用户行为类型对比</h3>
          </div>
        </template>
        <div 
          ref="actionTypeChart" 
          class="chart-container"
          v-loading="actionTypeLoading"
        ></div>
      </el-card>

      <!-- 热门景点TOP10 -->
      <el-card class="chart-card double-width">
        <template #header>
          <div class="card-header">
            <h3>热门景点TOP10</h3>
            <div class="chart-controls">
              <el-button-group>
                <el-button 
                  :type="hotItemsType === 'summary' ? 'primary' : 'default'"
                  size="small"
                  @click="changeHotItemsType('summary')"
                >
                  汇总
                </el-button>
                <el-button 
                  :type="hotItemsType === 'view' ? 'primary' : 'default'"
                  size="small"
                  @click="changeHotItemsType('view')"
                >
                  浏览量
                </el-button>
                <el-button 
                  :type="hotItemsType === 'purchase' ? 'primary' : 'default'"
                  size="small"
                  @click="changeHotItemsType('purchase')"
                >
                  预约量
                </el-button>
                <el-button 
                  :type="hotItemsType === 'favorite' ? 'primary' : 'default'"
                  size="small"
                  @click="changeHotItemsType('favorite')"
                >
                  收藏量
                </el-button>
              </el-button-group>
            </div>
          </div>
        </template>
        <div 
          ref="hotItemsChart" 
          class="chart-container"
          v-loading="hotItemsLoading"
        ></div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getUserList } from '@/api/user'
import { pageAllActions } from '@/api/userAction'
import { itemApi } from '@/api/item'
import { favoriteApi } from '@/api/favorite'
import { categoryApi } from '@/api/category'
import { algoRequest } from '@/api/algo_request'

// 数据概览
const overviewLoading = ref(false)
const overviewData = ref({
  totalUsers: 0,
  totalItems: 0,
  totalCategories: 0,
  totalActions: 0,
  totalTags: 0
})

// 图表实例
const actionTrendChart = ref<HTMLDivElement>()
const categoryDistributionChart = ref<HTMLDivElement>()
const userActivityChart = ref<HTMLDivElement>()
const actionTypeChart = ref<HTMLDivElement>()
const hotItemsChart = ref<HTMLDivElement>()

// 图表实例引用
let actionTrendChartInstance: echarts.ECharts | null = null
let categoryDistributionChartInstance: echarts.ECharts | null = null
let userActivityChartInstance: echarts.ECharts | null = null
let actionTypeChartInstance: echarts.ECharts | null = null
let hotItemsChartInstance: echarts.ECharts | null = null

// 加载状态
const actionTrendLoading = ref(false)
const categoryDistributionLoading = ref(false)
const userActivityLoading = ref(false)
const actionTypeLoading = ref(false)
const hotItemsLoading = ref(false)

// 控制参数
const actionTrendPeriod = ref('7')
const hotItemsType = ref('summary')
const useRealData = ref(false) // 默认使用模拟数据，避免初期数据过少影响展示效果

// 获取数据概览
const fetchOverviewData = async () => {
  try {
    overviewLoading.value = true
    
    if (useRealData.value) {
      // 并行获取所有概览数据
      const [usersRes, itemsRes, categoriesRes, actionsRes, kgStatsRes] = await Promise.all([
        getUserList({ current: 1, size: 1 }),
        itemApi.page({ current: 1, size: 1 }),
        categoryApi.page({ current: 1, size: 1 }),
        pageAllActions({ current: 1, size: 1 }),
        algoRequest.get('/knowledge-graph/stats').catch(() => ({ nodes: { Tag: 0 } }))
      ])
      
      overviewData.value = {
        totalUsers: usersRes.total || 0,
        totalItems: itemsRes.total || 0,
        totalCategories: categoriesRes.total || 0,
        totalActions: actionsRes.total || 0,
        totalTags: kgStatsRes.nodes?.Tag || 0,
        // 模拟上个月数据（实际项目中应从历史数据获取）
        lastMonthUsers: Math.floor((usersRes.total || 0) * (0.8 + Math.random() * 0.4)),
        lastMonthItems: Math.floor((itemsRes.total || 0) * (0.8 + Math.random() * 0.4)),
        lastMonthCategories: Math.floor((categoriesRes.total || 0) * (0.8 + Math.random() * 0.4)),
        lastMonthActions: Math.floor((actionsRes.total || 0) * (0.8 + Math.random() * 0.4)),
        lastMonthTags: Math.floor((kgStatsRes.nodes?.Tag || 0) * (0.8 + Math.random() * 0.4))
      }
    } else {
      // 使用模拟数据
      overviewData.value = {
        totalUsers: 1250,
        totalItems: 486,
        totalCategories: 12,
        totalActions: 8640,
        totalTags: 248,
        lastMonthUsers: 1180,
        lastMonthItems: 452,
        lastMonthCategories: 11,
        lastMonthActions: 8120,
        lastMonthTags: 235
      }
    }
  } catch (error) {
    console.error('获取概览数据失败:', error)
    ElMessage.error('获取概览数据失败')
  } finally {
    overviewLoading.value = false
  }
}

// 初始化用户行为趋势图
const initActionTrendChart = async () => {
  if (!actionTrendChart.value) return
  
  try {
    actionTrendLoading.value = true
    
    // 获取最近7天或30天的数据
    const days = parseInt(actionTrendPeriod.value)
    const dateList: string[] = []
    const viewData: number[] = []
    const purchaseData: number[] = []
    
    if (useRealData.value) {
      // 获取真实的用户行为数据
      const actionsRes = await pageAllActions({ current: 1, size: 10000 })
      const actions = actionsRes.records || []
      
      // 按日期统计行为数据
      const dateMap = new Map<string, { view: number; purchase: number }>()
      
      // 生成日期列表并初始化
      for (let i = days - 1; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        const dateStr = date.toLocaleDateString()
        dateList.push(dateStr)
        dateMap.set(dateStr, { view: 0, purchase: 0 })
      }
      
      // 统计每天的行为数据
      actions.forEach((action: any) => {
        if (action.createTime) {
          const actionDate = new Date(action.createTime).toLocaleDateString()
          if (dateMap.has(actionDate)) {
            const dayData = dateMap.get(actionDate)!
            if (action.actionType === 0) { // 浏览
              dayData.view++
            } else if (action.actionType === 1) { // 预约
              dayData.purchase++
            }
          }
        }
      })
      
      // 构建图表数据
      dateList.forEach(date => {
        const dayData = dateMap.get(date) || { view: 0, purchase: 0 }
        viewData.push(dayData.view)
        purchaseData.push(dayData.purchase)
      })
    } else {
      // 使用模拟数据
      for (let i = days - 1; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        dateList.push(date.toLocaleDateString())
        
        // 生成更美观的模拟数据，有一定的趋势和波动
        const baseView = 120 + Math.sin(i * 0.3) * 20
        const basePurchase = 25 + Math.sin(i * 0.2) * 8
        
        viewData.push(Math.floor(baseView + Math.random() * 40 - 20))
        purchaseData.push(Math.floor(basePurchase + Math.random() * 15 - 7))
      }
    }
    
    // 计算增长数据
    const viewGrowth: number[] = []
    const purchaseGrowth: number[] = []
    for (let i = 0; i < viewData.length; i++) {
      if (i === 0) {
        viewGrowth.push(0)
        purchaseGrowth.push(0)
      } else {
        viewGrowth.push(viewData[i] - viewData[i - 1])
        purchaseGrowth.push(purchaseData[i] - purchaseData[i - 1])
      }
    }
    
    if (!actionTrendChartInstance) {
      actionTrendChartInstance = echarts.init(actionTrendChart.value)
    }
    
    const option = {
      title: {
        text: `最近${days}天用户行为趋势`,
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'axis',
        formatter: function (params: any) {
          let result = params[0].name + '<br/>'
          params.forEach((item: any) => {
            if (item.seriesName.includes('增长')) {
              const growth = item.value
              const prevIndex = params[0].dataIndex - 1
              const prevValue = prevIndex >= 0 ? (item.seriesName === '浏览增长' ? viewData[prevIndex] : purchaseData[prevIndex]) : 0
              const growthPercent = prevValue !== 0 ? ((growth / prevValue) * 100).toFixed(1) : '0.0'
              result += `${item.marker} ${item.seriesName}: ${growth} (${growthPercent}%)<br/>`
            } else {
              result += `${item.marker} ${item.seriesName}: ${item.value}<br/>`
            }
          })
          return result
        }
      },
      legend: {
        data: ['浏览量', '预约量', '浏览增长', '预约增长'],
        top: 30
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '80px',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dateList
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '浏览量',
          type: 'line',
          smooth: true,
          data: viewData,
          areaStyle: {
            opacity: 0.3
          },
          itemStyle: {
            color: '#409EFF'
          }
        },
        {
          name: '预约量',
          type: 'line',
          smooth: true,
          data: purchaseData,
          areaStyle: {
            opacity: 0.3
          },
          itemStyle: {
            color: '#67C23A'
          }
        },
        {
          name: '浏览增长',
          type: 'bar',
          data: viewGrowth,
          barWidth: '20%',
          itemStyle: {
            color: '#409EFF' // 固定为浏览量主色
          }
        },
        {
          name: '预约增长',
          type: 'bar',
          data: purchaseGrowth,
          barWidth: '20%',
          itemStyle: {
            color: '#67C23A' // 固定为预约量主色
          }
        }
      ]
    }
    
    actionTrendChartInstance.setOption(option)
  } catch (error) {
    console.error('初始化行为趋势图失败:', error)
  } finally {
    actionTrendLoading.value = false
  }
}

// 初始化景点分类分布图
const initCategoryDistributionChart = async () => {
  if (!categoryDistributionChart.value) return
  
  try {
    categoryDistributionLoading.value = true
    
    let data: Array<{ name: string; value: number }> = []
    
    if (useRealData.value) {
      // 获取分类数据
      const categoriesRes = await categoryApi.list()
      const categories = categoriesRes || []
      
      // 获取每个分类的真实景点数量
      data = await Promise.all(
        categories.map(async (category) => {
          try {
            const itemsRes = await itemApi.page({ current: 1, size: 1, categoryId: category.id })
            return {
              name: category.name,
              value: itemsRes.total || 0
            }
          } catch (error) {
            console.error(`获取分类${category.name}景点数量失败:`, error)
            return {
              name: category.name,
              value: 0
            }
          }
        })
      )
    } else {
      // 使用模拟数据
      data = [
        { name: '自然风光', value: 85 },
        { name: '历史文化', value: 126 },
        { name: '主题乐园', value: 68 },
        { name: '城市观光', value: 92 },
        { name: '海滨度假', value: 45 },
        { name: '山水田园', value: 38 },
        { name: '古镇村落', value: 72 },
        { name: '宗教场所', value: 34 },
        { name: '科技展馆', value: 26 },
        { name: '其他', value: 18 }
      ]
    }
    
    if (!categoryDistributionChartInstance) {
      categoryDistributionChartInstance = echarts.init(categoryDistributionChart.value)
    }
    
    const option = {
      title: {
        text: '景点分类分布',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'horizontal',
        bottom: '5%',
        left: 'center'
      },
      color: generateColors(data.length), // 动态生成颜色，确保颜色数量与数据量匹配
      series: [
        {
          name: '景点数量',
          type: 'pie',
          radius: '50%',
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}: {d}%'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          labelLine: {
            show: true
          },
          itemStyle: {
            borderWidth: 2,
            borderColor: '#fff',
            shadowBlur: 5,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          },
          data: data
        }
      ]
    }
    
    categoryDistributionChartInstance.setOption(option)
  } catch (error) {
    console.error('初始化分类分布图失败:', error)
  } finally {
    categoryDistributionLoading.value = false
  }
}

// 初始化用户活跃度图
const initUserActivityChart = async () => {
  if (!userActivityChart.value) return
  
  try {
    userActivityLoading.value = true
    
    let activityData: Array<{ name: string; value: number }> = []
    
    if (useRealData.value) {
      // 获取所有用户的行为数据来计算活跃度
      const [usersRes, actionsRes] = await Promise.all([
        getUserList({ current: 1, size: 10000 }),
        pageAllActions({ current: 1, size: 10000 })
      ])
      
      const users = usersRes.records || []
      const actions = actionsRes.records || []
      
      // 统计每个用户的行为数量
      const userActionCount = new Map<number, number>()
      actions.forEach((action: any) => {
        if (action.userId) {
          const count = userActionCount.get(action.userId) || 0
          userActionCount.set(action.userId, count + 1)
        }
      })
      
      // 按活跃度分类用户
      let veryActive = 0, active = 0, normal = 0, inactive = 0
      
      users.forEach((user: any) => {
        const actionCount = userActionCount.get(user.id) || 0
        if (actionCount >= 20) {
          veryActive++
        } else if (actionCount >= 10) {
          active++
        } else if (actionCount >= 3) {
          normal++
        } else {
          inactive++
        }
      })
      
      activityData = [
        { name: '非常活跃', value: veryActive },
        { name: '较活跃', value: active },
        { name: '一般活跃', value: normal },
        { name: '不活跃', value: inactive }
      ]
    } else {
      // 使用模拟数据
      activityData = [
        { name: '非常活跃', value: 185 },
        { name: '较活跃', value: 342 },
        { name: '一般活跃', value: 468 },
        { name: '不活跃', value: 255 }
      ]
    }
    
    if (!userActivityChartInstance) {
      userActivityChartInstance = echarts.init(userActivityChart.value)
    }
    
    const option = {
      title: {
        text: '用户活跃度分布',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle'
      },
      color: generateColors(activityData.length), // 动态生成颜色
      series: [
        {
          name: '用户数量',
          type: 'pie',
          radius: ['40%', '70%'], // 环形图
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '20',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: activityData
        }
      ]
    }
    
    userActivityChartInstance.setOption(option)
  } catch (error) {
    console.error('初始化用户活跃度图失败:', error)
  } finally {
    userActivityLoading.value = false
  }
}

// 初始化行为类型对比图
const initActionTypeChart = async () => {
  if (!actionTypeChart.value) return
  
  try {
    actionTypeLoading.value = true
    
    let actionData: Array<{ name: string; value: number }> = []
    
    if (useRealData.value) {
      // 获取真实的行为数据并统计
      const actionsRes = await pageAllActions({ current: 1, size: 10000 })
      const actions = actionsRes.records || []
      
      // 统计不同类型的行为
      let viewCount = 0
      let purchaseCount = 0
      
      actions.forEach((action: any) => {
        if (action.actionType === 0) { // 浏览
          viewCount++
        } else if (action.actionType === 1) { // 预约
          purchaseCount++
        }
      })
      
      // 尝试获取收藏总数
      let favoriteCount = 0
      try {
        // 通过分页获取收藏总数
        const favoriteRes = await favoriteApi.page(1, 1)
        favoriteCount = favoriteRes.total || 0
      } catch (error) {
        console.warn('无法获取收藏总数，使用估算值:', error)
        favoriteCount = Math.floor(viewCount * 0.1) // 假设收藏率为10%
      }
      
      actionData = [
        { name: '浏览', value: viewCount },
        { name: '预约', value: purchaseCount },
        { name: '收藏', value: favoriteCount }
      ]
    } else {
      // 使用模拟数据
      actionData = [
        { name: '浏览', value: 6840 },
        { name: '预约', value: 1456 },
        { name: '收藏', value: 844 }
      ]
    }
    
    // 调整数据，确保最小值不小于最大值的1/3，以保证最小半径不小于最大半径的1/3
    const maxValue = Math.max(...actionData.map(d => d.value))
    const minValueThreshold = maxValue / 3
    actionData = actionData.map(d => ({
      ...d,
      value: Math.max(d.value, minValueThreshold)
    }))
    
    if (!actionTypeChartInstance) {
      actionTypeChartInstance = echarts.init(actionTypeChart.value)
    }
    
    const option = {
      title: {
        text: '用户行为类型对比',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle'
      },
      color: generateColors(actionData.length), // 动态生成颜色
      series: [
        {
          name: '行为数量',
          type: 'pie',
          radius: '60%',
          center: ['60%', '50%'],
          roseType: 'radius', // 南丁格尔玫瑰图效果，半径正比于值
          data: actionData,
          itemStyle: {
            emphasis: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            show: false
          },
          labelLine: {
            show: false
          }
        }
      ]
    }
    
    actionTypeChartInstance.setOption(option)
  } catch (error) {
    console.error('初始化行为类型图失败:', error)
  } finally {
    actionTypeLoading.value = false
  }
}

// 初始化热门景点图
const initHotItemsChart = async () => {
  if (!hotItemsChart.value) return
  
  try {
    hotItemsLoading.value = true
    
    let itemStats: Array<{ name: string; view: number; purchase: number; favorite: number }> = []
    
    if (useRealData.value) {
      // 获取真实的景点和行为数据
      const [itemsRes, actionsRes] = await Promise.all([
        itemApi.page({ current: 1, size: 100 }), // 获取景点列表
        pageAllActions({ current: 1, size: 10000 }) // 获取用户行为数据
      ])
      
      const items = itemsRes.records || []
      const actions = actionsRes.records || []
      
      // 统计每个景点的不同类型数据
      const statsMap = new Map<number, { view: number; purchase: number; favorite: number; name: string }>()
      
      // 初始化景点统计
      items.forEach((item: any) => {
        statsMap.set(item.id, {
          view: 0,
          purchase: 0,
          favorite: 0,
          name: item.title || item.name || `景点${item.id}`
        })
      })
      
      // 统计行为数据
      actions.forEach((action: any) => {
        if (action.itemId && statsMap.has(action.itemId)) {
          const stats = statsMap.get(action.itemId)!
          if (action.actionType === 0) { // 浏览
            stats.view++
          } else if (action.actionType === 1) { // 预约
            stats.purchase++
          }
        }
      })
      
      // 尝试获取真实的景点收藏数据
      for (const [itemId, stats] of statsMap.entries()) {
        try {
          const favoriteCount = await favoriteApi.getItemFavoriteCount(itemId)
          stats.favorite = favoriteCount
        } catch (error) {
          // 如果获取失败，使用浏览数据的估算值
          stats.favorite = Math.floor(stats.view * 0.15) // 假设收藏率为15%
        }
      }
      
      // 根据浏览量排序并取前10
      itemStats = Array.from(statsMap.values())
        .sort((a, b) => b.view - a.view)
        .slice(0, 10)
    } else {
      // 使用模拟数据
      const mockItemNames = [
        '九寨沟风景区', '张家界国家森林公园', '黄山风景区', 
        '桂林山水', '泰山风景区', '天安门广场', 
        '西湖风景区', '故宫博物院', '长城', '兵马俑'
      ]
      
      itemStats = mockItemNames.map((name, index) => ({
        name,
        view: 450 - index * 35,
        purchase: 89 - index * 7,
        favorite: 156 - index * 12
      }))
    }
    
    if (!hotItemsChartInstance) {
      hotItemsChartInstance = echarts.init(hotItemsChart.value)
    }
    
    let option: any
    
    if (hotItemsType.value === 'summary') {
      // 分组柱状图
      option = {
        title: {
          text: '热门景点TOP10 - 数据汇总',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'normal'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['浏览量', '预约量', '收藏量'],
          top: '30px'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '80px',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: itemStats.map(item => item.name),
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '浏览量',
            type: 'bar',
            data: itemStats.map(item => item.view),
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '预约量',
            type: 'bar',
            data: itemStats.map(item => item.purchase),
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '收藏量',
            type: 'bar',
            data: itemStats.map(item => item.favorite),
            itemStyle: { color: '#E6A23C' }
          }
        ]
      }
    } else {
      // 单个指标的竖向柱状图
      const typeName = hotItemsType.value === 'view' ? '浏览量' : hotItemsType.value === 'purchase' ? '预约量' : '收藏量'
      const dataKey = hotItemsType.value as keyof typeof itemStats[0]
      
      option = {
        title: {
          text: `热门景点TOP10 - ${typeName}`,
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'normal'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '60px',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: itemStats.map(item => item.name),
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: typeName,
            type: 'bar',
            data: itemStats.map(item => item[dataKey] as number),
            itemStyle: {
              color: hotItemsType.value === 'view' ? '#409EFF' : hotItemsType.value === 'purchase' ? '#67C23A' : '#E6A23C'
            }
          }
        ]
      }
    }
    
    hotItemsChartInstance.setOption(option, true)
  } catch (error) {
    console.error('初始化热门景点图失败:', error)
  } finally {
    hotItemsLoading.value = false
  }
}

// 动态颜色生成函数
const generateColors = (count: number): string[] => {
  const baseColors = [
    '#5470C6', '#91CC75', '#FAC858', '#EE6666', '#73C0DE', '#3BA272', 
    '#FC8452', '#9A60B4', '#EA7CCC', '#FF9F7F', '#FFDB5C', '#37A2FF',
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD',
    '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9', '#F8C471', '#82E0AA',
    '#F1948A', '#85C1E9', '#F7DC6F', '#D7BDE2', '#A9CCE3', '#FAD7A0',
    '#ABEBC6', '#F9E79F', '#D5A6BD', '#A3E4D7', '#F8C471', '#85C1E9',
    '#F1948A', '#D7BDE2', '#A9CCE3', '#FAD7A0', '#ABEBC6', '#F9E79F'
  ]
  
  // 如果需要的颜色数量在基础颜色范围内，直接返回
  if (count <= baseColors.length) {
    return baseColors.slice(0, count)
  }
  
  // 如果需要的颜色数量超过基础颜色，动态生成更多颜色
  const colors = [...baseColors]
  
  for (let i = baseColors.length; i < count; i++) {
    // 使用HSL颜色空间生成不同的颜色
    const hue = (i * 137.508) % 360 // 黄金角度，确保颜色分布均匀
    const saturation = 60 + (i % 20) // 60-80%的饱和度
    const lightness = 50 + (i % 15) // 50-65%的亮度
    
    // 转换为十六进制颜色
    const color = hslToHex(hue, saturation, lightness)
    colors.push(color)
  }
  
  return colors
}

// HSL转十六进制颜色函数
const hslToHex = (h: number, s: number, l: number): string => {
  s /= 100
  l /= 100
  
  const c = (1 - Math.abs(2 * l - 1)) * s
  const x = c * (1 - Math.abs((h / 60) % 2 - 1))
  const m = l - c / 2
  let r = 0, g = 0, b = 0
  
  if (0 <= h && h < 60) {
    r = c; g = x; b = 0
  } else if (60 <= h && h < 120) {
    r = x; g = c; b = 0
  } else if (120 <= h && h < 180) {
    r = 0; g = c; b = x
  } else if (180 <= h && h < 240) {
    r = 0; g = x; b = c
  } else if (240 <= h && h < 300) {
    r = x; g = 0; b = c
  } else if (300 <= h && h < 360) {
    r = c; g = 0; b = x
  }
  
  const rHex = Math.round((r + m) * 255).toString(16).padStart(2, '0')
  const gHex = Math.round((g + m) * 255).toString(16).padStart(2, '0')
  const bHex = Math.round((b + m) * 255).toString(16).padStart(2, '0')
  
  return `#${rHex}${gHex}${bHex}`
}

// 调整颜色亮度函数
const adjustColorBrightness = (hexColor: string, percent: number): string => {
  // 移除#号
  const hex = hexColor.replace('#', '')
  
  // 解析RGB值
  const r = parseInt(hex.substr(0, 2), 16)
  const g = parseInt(hex.substr(2, 2), 16)
  const b = parseInt(hex.substr(4, 2), 16)
  
  // 调整亮度
  const adjustR = Math.max(0, Math.min(255, r + (r * percent / 100)))
  const adjustG = Math.max(0, Math.min(255, g + (g * percent / 100)))
  const adjustB = Math.max(0, Math.min(255, b + (b * percent / 100)))
  
  // 转换回十六进制
  const newR = Math.round(adjustR).toString(16).padStart(2, '0')
  const newG = Math.round(adjustG).toString(16).padStart(2, '0')
  const newB = Math.round(adjustB).toString(16).padStart(2, '0')
  
  return `#${newR}${newG}${newB}`
}

// 改变行为趋势图时间段
const changeActionTrendPeriod = (period: string) => {
  actionTrendPeriod.value = period
  initActionTrendChart()
}

// 计算同比增长百分比
const getGrowthPercent = (key: string) => {
  const current = overviewData.value[key as keyof typeof overviewData.value] as number
  const lastMonthKey = `lastMonth${key.charAt(0).toUpperCase()}${key.slice(1)}`
  const lastMonth = overviewData.value[lastMonthKey as keyof typeof overviewData.value] as number
  if (lastMonth === 0) return 0
  return ((current - lastMonth) / lastMonth) * 100
}

// 改变热门景点类型
const changeHotItemsType = (type: string) => {
  hotItemsType.value = type
  nextTick(() => {
    initHotItemsChart()
  })
}

// 切换数据模式
const switchDataMode = async () => {
  console.log('切换数据模式:', useRealData.value ? '真实数据' : '模拟数据')
  
  // 重新获取概览数据
  await fetchOverviewData()
  
  // 重新初始化所有图表
  await nextTick(() => {
    initActionTrendChart()
    initCategoryDistributionChart()
    initUserActivityChart()
    initActionTypeChart()
    initHotItemsChart()
  })
  
  ElMessage.success(`已切换到${useRealData.value ? '真实' : '模拟'}数据模式`)
}

// 窗口大小改变时调整图表大小
const handleResize = () => {
  actionTrendChartInstance?.resize()
  categoryDistributionChartInstance?.resize()
  userActivityChartInstance?.resize()
  actionTypeChartInstance?.resize()
  hotItemsChartInstance?.resize()
}

// 组件挂载时初始化
onMounted(async () => {
  await fetchOverviewData()
  
  // 使用nextTick确保DOM渲染完成
  nextTick(() => {
    initActionTrendChart()
    initCategoryDistributionChart()
    initUserActivityChart()
    initActionTypeChart()
    initHotItemsChart()
  })
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理
onUnmounted(() => {
  actionTrendChartInstance?.dispose()
  categoryDistributionChartInstance?.dispose()
  userActivityChartInstance?.dispose()
  actionTypeChartInstance?.dispose()
  hotItemsChartInstance?.dispose()
  
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.admin-dashboard {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-text {
  text-align: center;
  flex: 1;
}

.header-text h1 {
  color: #303133;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.header-text p {
  color: #606266;
  font-size: 16px;
  margin: 0;
}

.header-controls {
  display: flex;
  align-items: center;
}

.data-mode-switch {
  opacity: 0.3;
  transition: all 0.3s ease;
}

.data-mode-switch:hover {
  opacity: 0.8;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.overview-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.merged-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px 0;
  margin-bottom: 16px;
}

.merged-card .card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.growth-header {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.merged-content {
  padding: 0 20px 20px;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  min-height: 80px; /* 确保高度一致 */
  border-bottom: 1px solid #f0f0f0;
}

.metric-item:last-child {
  border-bottom: none;
}

.metric-item .card-content {
  display: flex;
  align-items: center;
  flex: 1;
}

.metric-item .card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.metric-item .card-icon svg {
  width: 32px;
  height: 32px;
  fill: white;
}

.metric-item .card-info h3 {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 8px 0;
}

.metric-item .card-info .number {
  color: #303133;
  font-size: 28px;
  font-weight: 700;
  margin: 0;
}

.metric-growth {
  font-size: 16px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
  min-width: 60px;
  text-align: center;
}

.metric-growth.positive {
  color: #67C23A;
  background-color: #f0f9ff;
}

.metric-growth.negative {
  color: #F56C6C;
  background-color: #fef0f0;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 20px;
}

.chart-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-card.double-width {
  grid-column: span 2;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.chart-controls {
  display: flex;
  align-items: center;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.chart-card.full-width .chart-container {
  height: 500px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-dashboard {
    padding: 16px;
  }
  
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .chart-container {
    height: 300px;
  }
  
  .chart-card.full-width .chart-container {
    height: 400px;
  }
  
  .chart-card.double-width .chart-container {
    height: 400px;
  }
}

@media (max-width: 480px) {
  .overview-cards {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .card-content {
    padding: 16px;
  }
  
  .card-icon {
    width: 48px;
    height: 48px;
    margin-right: 16px;
  }
  
  .card-icon svg {
    width: 24px;
    height: 24px;
  }
  
  .card-info .number {
    font-size: 24px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>