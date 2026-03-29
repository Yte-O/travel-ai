<template>
  <div class="user-action-history">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户行为历史</span>
          <el-button 
            v-if="selected.length > 0" 
            type="danger" 
            @click="handleBatchDelete"
          >
            批量删除 ({{ selected.length }})
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="用户名" clearable @keyup.enter="fetchHistoryList"></el-input>
        </el-form-item>
        <el-form-item label="景点名称">
          <el-input v-model="queryParams.itemTitle" placeholder="景点名称" clearable @keyup.enter="fetchHistoryList"></el-input>
        </el-form-item>
        <el-form-item label="行为类型">
          <el-select v-model="queryParams.actionType" placeholder="行为类型" clearable @keyup.enter="fetchHistoryList" style="width: 150px;">
            <el-option label="浏览" :value="0"></el-option>
            <el-option label="预约" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchHistoryList">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table
        v-loading="loading"
        :data="historyList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column
          prop="userInfo.username"
          label="用户名"
          min-width="100"
        >
          <template #default="scope">
            {{ scope.row.userInfo ? scope.row.userInfo.username : '-' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="item.title"
          label="景点名称"
          min-width="120"
        >
          <template #default="scope">
            <router-link :to="'/user/item/' + scope.row.itemId" class="item-link">
              {{ scope.row.item ? scope.row.item.title : '未知景点' }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column
          prop="actionType"
          label="行为类型"
          min-width="80"
        >
          <template #default="scope">
            {{ scope.row.actionType === 0 ? '浏览' : '预约' }}
          </template>
        </el-table-column>
        <el-table-column
          prop="extraData"
          label="附加信息"
          min-width="120"
        >
          <template #default="scope">
            <span v-if="scope.row.extraData" v-html="formatExtraData(scope.row.extraData)"></span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="行为时间"
          min-width="160"
        >
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page.sync="queryParams.current"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="queryParams.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
        </el-pagination>
      </div>
    </el-card>
    
    <!-- 批量删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <span>确认要删除选中的 {{ selected.length }} 条记录吗？此操作不可撤销。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmBatchDelete" :loading="deleteLoading">
            确认删除
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { pageAllActions, batchDeleteActions, type UserActionQueryParams, type UserActionResponse } from '@/api/userAction'
import { formatDate } from '@/utils/date'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const deleteLoading = ref(false)
const historyList = ref<any[]>([])
const total = ref(0)
const selected = ref<any[]>([])
const deleteDialogVisible = ref(false)
const queryParams = ref<UserActionQueryParams>({
  current: 1,
  size: 10,
  username: undefined,
  itemTitle: undefined,
  actionType: undefined
})

// 字段中文映射
const fieldNameMap: Record<string, string> = {
  reservationId: '预约编号',
  adults: '成人数量',
  children: '儿童数量',
  totalPrice: '总价格',
  paymentMethod: '支付方式',
  travelDate: '出行日期',
  contactInfo: '联系信息',
  viewTime: '浏览时间',
  source: '来源'
}

const formatExtraData = (extraDataStr: string): string => {
  try {
    const extraData = JSON.parse(extraDataStr)
    let formattedData = '<div style="display: flex; flex-direction: column; gap: 5px;">'
    
    // 按照指定顺序显示字段
    const displayOrder = ['reservationId', 'adults', 'children', 'totalPrice', 'paymentMethod', 'travelDate', 'contactInfo', 'viewTime', 'source']
    
    for (const key of displayOrder) {
      if (extraData[key] !== undefined) {
        let value = extraData[key]
        const displayName = fieldNameMap[key] || key
        
        // 格式化特定字段
        if (key === 'totalPrice') {
          value = `￥${value}`
        } else if (key === 'adults' || key === 'children') {
          value = `${value}人`
        } else if (key === 'viewTime' && typeof value === 'string' && value.includes('T')) {
          try {
            value = formatDate(value)
          } catch (e) {
            // 保持原样
          }
        }
        
        formattedData += `<div style="display: flex; align-items: baseline; margin-bottom: 2px;">
          <span style="font-weight: 600; color: #606266; margin-right: 5px; min-width: 80px;">${displayName}:</span>
          <span style="color: #333;">${value}</span>
        </div>`
      }
    }
    
    // 显示其他未定义的字段
    for (const key in extraData) {
      if (!displayOrder.includes(key)) {
        let value = extraData[key]
        const displayName = fieldNameMap[key] || key
        
        formattedData += `<div style="display: flex; align-items: baseline; margin-bottom: 2px;">
          <span style="font-weight: 600; color: #606266; margin-right: 5px; min-width: 80px;">${displayName}:</span>
          <span style="color: #333;">${value}</span>
        </div>`
      }
    }
    
    formattedData += '</div>'
    return formattedData
  } catch (e) {
    return extraDataStr
  }
}

const fetchHistoryList = async () => {
  loading.value = true
  try {
    const response = await pageAllActions(queryParams.value)
    historyList.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    console.error('获取用户行为历史失败', error)
    ElMessage.error('获取用户行为历史失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.value = {
    current: 1,
    size: 10,
    username: undefined,
    itemTitle: undefined,
    actionType: undefined
  }
  fetchHistoryList()
}

const handleSizeChange = (size: number) => {
  queryParams.value.size = size
  fetchHistoryList()
}

const handleCurrentChange = (current: number) => {
  queryParams.value.current = current
  fetchHistoryList()
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  selected.value = selection
}

// 处理批量删除按钮点击
const handleBatchDelete = () => {
  if (selected.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  deleteDialogVisible.value = true
}

// 确认批量删除
const confirmBatchDelete = async () => {
  if (selected.value.length === 0) {
    deleteDialogVisible.value = false
    return
  }
  
  deleteLoading.value = true
  try {
    // 提取所选记录的ID
    const ids = selected.value.map(item => item.id)
    
    // 调用批量删除API
    const result = await batchDeleteActions(ids)
    
    if (result) {
      ElMessage.success(`成功删除 ${selected.value.length} 条记录`)
      // 刷新数据
      fetchHistoryList()
      // 清空选择
      selected.value = []
    } else {
      ElMessage.error('删除失败')
    }
    
    deleteDialogVisible.value = false
  } catch (error) {
    console.error('批量删除失败', error)
    ElMessage.error('批量删除失败')
  } finally {
    deleteLoading.value = false
  }
}

onMounted(() => {
  fetchHistoryList()
})
</script>

<style scoped>
.user-action-history {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  text-align: center;
}
.item-link {
  color: #409EFF;
  text-decoration: none;
}
.item-link:hover {
  text-decoration: underline;
}
.extra-data-container {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.extra-data-item {
  display: flex;
  align-items: baseline;
}
.extra-data-key {
  font-weight: 600;
  color: #606266;
  margin-right: 5px;
}
.extra-data-value {
  color: #333;
}
</style> 