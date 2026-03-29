<!-- 知识图谱管理页面 -->
<template>
  <div class="admin-dashboard">
    <el-card class="welcome-card">
      <h1>知识图谱管理</h1>
      <p>欢迎使用Travel AI 推荐知识图谱管理系统</p>
    </el-card>

    <el-card class="knowledge-graph-card">
      <template #header>
        <div class="card-header">
          <span>知识图谱同步</span>
        </div>
      </template>
      
      <div class="kg-content">
        <p class="kg-description">
          将当前数据库中的数据同步到Neo4j图数据库，构建知识图谱以支持更好的推荐算法。
        </p>
        
        <div class="kg-actions">
          <el-button 
            type="primary" 
            :loading="syncLoading"
            @click="syncToKnowledgeGraph"
            size="large"
          >
            <el-icon class="mr-1"><Refresh /></el-icon>
            {{ syncLoading ? '同步中...' : '同步数据到图数据库' }}
          </el-button>
        </div>

        <div v-if="syncResult" class="sync-result">
          <el-alert
            :title="syncResult.success ? '同步成功' : '同步失败'"
            :type="syncResult.success ? 'success' : 'error'"
            :description="syncResult.message"
            show-icon
            :closable="false"
          />
        </div>
      </div>
    </el-card>

    <el-card class="statistics-card">
      <template #header>
        <div class="card-header">
          <span>图谱统计</span>
          <el-button 
            type="primary" 
            link 
            @click="fetchStatistics"
            :loading="statsLoading"
            :disabled="statsLoading"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <div v-if="statsLoading" class="statistics-loading">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="statistics" class="statistics-content">
        <el-row :gutter="20">
          <el-col :span="8" v-for="(value, key) in statistics.nodes" :key="key">
            <div class="statistic-box">
              <h3>{{ getNodeTypeName(key) }}</h3>
              <div class="statistic-value">{{ value }}</div>
              <div class="statistic-label">节点数</div>
            </div>
          </el-col>
          
          <el-col :span="8" v-for="(value, key) in statistics.relationships" :key="'rel-'+key">
            <div class="statistic-box relationship">
              <h3>{{ getRelationshipName(key) }}</h3>
              <div class="statistic-value">{{ value }}</div>
              <div class="statistic-label">关系数</div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div v-else class="no-statistics">
        <el-empty description="暂无统计数据" />
      </div>
    </el-card>

    <el-card class="visualization-card">
      <template #header>
        <div class="card-header">
          <span>知识图谱可视化</span>
          <div class="header-controls">
            <el-radio-group 
              v-model="visualizationMode" 
              size="small" 
              @change="switchVisualizationMode"
              style="margin-right: 15px;"
            >
              <el-radio-button value="custom">自定义图表</el-radio-button>
              <el-radio-button value="neo4j">Neo4j浏览器</el-radio-button>
            </el-radio-group>
            
            <template v-if="visualizationMode === 'custom'">
              <el-input-number 
                v-model="nodeLimit" 
                :min="50" 
                :max="500" 
                size="small" 
                @change="updateVisualization"
                style="width: 120px;"
              />
              <el-button 
                type="primary" 
                size="small" 
                @click="loadVisualizationData" 
                :loading="visualizationLoading"
                :disabled="visualizationLoading"
                style="margin-left: 10px;"
              >
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="toggleFullscreen"
                style="margin-left: 10px;"
              >
                <el-icon>
                  <svg v-if="!isFullscreen" viewBox="0 0 1024 1024">
                    <path d="M290 236.4l43.9-43.9a8.01 8.01 0 0 0-4.7-13.6L169 160c-5.1-.6-9.5 3.7-8.9 8.9L179 329.1c.8 6.6 8.9 9.4 13.6 4.7l43.7-43.7L370 423.7c3.1 3.1 8.2 3.1 11.3 0l42.4-42.3c3.1-3.1 3.1-8.2 0-11.3L290 236.4zm352.7 187.3c3.1 3.1 8.2 3.1 11.3 0l133.7-133.6 43.7 43.7a8.01 8.01 0 0 0 13.6-4.7L864.9 169c.6-5.1-3.7-9.5-8.9-8.9L695.9 179c-6.6.8-9.4 8.9-4.7 13.6l43.9 43.9L601.4 370a8.03 8.03 0 0 0 0 11.3l42.3 42.4zM845 694.9c-.8-6.6-8.9-9.4-13.6-4.7l-43.7 43.7L654 600.3a8.03 8.03 0 0 0-11.3 0l-42.4 42.3a8.03 8.03 0 0 0 0 11.3L734 787.6l-43.9 43.9a8.01 8.01 0 0 0 4.7 13.6L855 864.9c5.1.6 9.5-3.7 8.9-8.9L845 694.9zm-463.7-94.6a8.03 8.03 0 0 0-11.3 0L236.3 734l-43.7-43.7a8.01 8.01 0 0 0-13.6 4.7L160.1 855c-.6 5.1 3.7 9.5 8.9 8.9L329.1 845c6.6-.8 9.4-8.9 4.7-13.6L290 787.6 423.7 654c3.1-3.1 3.1-8.2 0-11.3l-42.4-42.3z"/>
                  </svg>
                  <svg v-else viewBox="0 0 1024 1024">
                    <path d="M391 240.9c-.8-6.6-8.9-9.4-13.6-4.7l-43.7 43.7L200 146.3a8.03 8.03 0 0 0-11.3 0l-42.4 42.3a8.03 8.03 0 0 0 0 11.3L280 333.6l-43.9 43.9a8.01 8.01 0 0 0 4.7 13.6L401 410c5.1.6 9.5-3.7 8.9-8.9L391 240.9zm10.1 373.2L240.8 633c-6.6.8-9.4 8.9-4.7 13.6l43.9 43.9L146.3 824a8.03 8.03 0 0 0 0 11.3l42.4 42.3c3.1 3.1 8.2 3.1 11.3 0L333.7 744l43.7 43.7c4.7 4.7 12.9 2 13.6-4.7l18.9-160.1c.6-5.1-3.7-9.4-8.8-8.8zm221.8-204.2L783.2 391c6.6-.8 9.4-8.9 4.7-13.6L744 333.6 877.7 200c3.1-3.1 3.1-8.2 0-11.3l-42.4-42.3a8.03 8.03 0 0 0-11.3 0L690.3 280l-43.7-43.7a8.01 8.01 0 0 0-13.6 4.7L614.1 401c-.6 5.2 3.7 9.5 8.8 8.9zM744 690.4l43.9-43.9a8.01 8.01 0 0 0-4.7-13.6L623 614c-5.1-.6-9.5 3.7-8.9 8.9L633 783.1c.8 6.6 8.9 9.4 13.6 4.7l43.7-43.7L824 877.7c3.1 3.1 8.2 3.1 11.3 0l42.4-42.3c3.1-3.1 3.1-8.2 0-11.3L744 690.4z"/>
                  </svg>
                </el-icon>
                {{ isFullscreen ? '退出全屏' : '全屏显示' }}
              </el-button>
            </template>
            
            <template v-else>
              <el-button 
                type="success" 
                size="small" 
                @click="openNeo4jBrowser"
                style="margin-left: 10px;"
              >
                <el-icon><Refresh /></el-icon>
                打开Neo4j浏览器
              </el-button>
            </template>
          </div>
        </div>
      </template>
      
      <div class="visualization-description">
        <p v-if="visualizationMode === 'custom'">
          下方显示的是知识图谱的可视化结果，不同颜色代表不同类型的节点，连线代表节点之间的关系。
          您可以拖拽节点、缩放图形，鼠标悬停在节点或关系上可查看详细信息。
        </p>
        <p v-else>
          点击下方按钮将在新的浏览器窗口中打开Neo4j原生浏览器界面，您可以在其中执行Cypher查询、浏览数据库结构、进行更复杂的图数据分析。
          默认连接到本地Neo4j数据库（localhost:7474），请确保Neo4j服务正在运行。
        </p>
      </div>
      
              <div v-if="visualizationMode === 'custom'" class="visualization-controls">
          <el-radio-group v-model="layoutType" size="small" @change="changeLayout">
            <el-radio-button value="force">力导向图</el-radio-button>
            <el-radio-button value="circular">环形布局</el-radio-button>
          </el-radio-group>
        
        <el-button 
          type="info" 
          size="small" 
          @click="resetLayout"
          style="margin-left: 15px;"
        >
          <el-icon><Refresh /></el-icon>
          重置布局
        </el-button>
        
        <el-tooltip content="双击节点可取消固定位置" placement="top">
          <el-button 
            type="warning" 
            size="small" 
            @click="clearFixedNodes"
            style="margin-left: 10px;"
          >
            解除固定
          </el-button>
        </el-tooltip>
      </div>
      
      <!-- 自定义可视化模式 -->
      <template v-if="visualizationMode === 'custom'">
        <div v-if="visualizationLoading" class="visualization-loading">
          <el-skeleton :rows="10" animated />
        </div>
        
        <div v-else-if="!visualizationData || visualizationData.nodes.length === 0" class="no-visualization">
          <el-empty description="暂无图谱数据，请先同步数据或刷新" />
        </div>
        
        <div v-else class="visualization-container" :class="{ 'fullscreen-mode': isFullscreen }">
          <!-- 全屏模式下的顶部控制栏 -->
          <div v-if="isFullscreen" class="fullscreen-header">
            <div class="fullscreen-title">
              <h3>知识图谱可视化 - 全屏模式</h3>
            </div>
            <div class="fullscreen-controls">
              <el-radio-group v-model="layoutType" size="small" @change="changeLayout">
                <el-radio-button value="force">力导向图</el-radio-button>
                <el-radio-button value="circular">环形布局</el-radio-button>
              </el-radio-group>
              
              <el-button 
                type="info" 
                size="small" 
                @click="resetLayout"
                style="margin-left: 15px;"
              >
                <el-icon><Refresh /></el-icon>
                重置布局
              </el-button>
              
              <el-tooltip content="双击节点可取消固定位置" placement="top">
                <el-button 
                  type="warning" 
                  size="small" 
                  @click="clearFixedNodes"
                  style="margin-left: 10px;"
                >
                  解除固定
                </el-button>
              </el-tooltip>
              
              <el-button 
                type="danger" 
                size="small" 
                @click="toggleFullscreen"
                style="margin-left: 15px;"
              >
                <el-icon>
                  <svg viewBox="0 0 1024 1024">
                    <path d="M391 240.9c-.8-6.6-8.9-9.4-13.6-4.7l-43.7 43.7L200 146.3a8.03 8.03 0 0 0-11.3 0l-42.4 42.3a8.03 8.03 0 0 0 0 11.3L280 333.6l-43.9 43.9a8.01 8.01 0 0 0 4.7 13.6L401 410c5.1.6 9.5-3.7 8.9-8.9L391 240.9zm10.1 373.2L240.8 633c-6.6.8-9.4 8.9-4.7 13.6l43.9 43.9L146.3 824a8.03 8.03 0 0 0 0 11.3l42.4 42.3c3.1 3.1 8.2 3.1 11.3 0L333.7 744l43.7 43.7c4.7 4.7 12.9 2 13.6-4.7l18.9-160.1c.6-5.1-3.7-9.4-8.8-8.8zm221.8-204.2L783.2 391c6.6-.8 9.4-8.9 4.7-13.6L744 333.6 877.7 200c3.1-3.1 3.1-8.2 0-11.3l-42.4-42.3a8.03 8.03 0 0 0-11.3 0L690.3 280l-43.7-43.7a8.01 8.01 0 0 0-13.6 4.7L614.1 401c-.6 5.2 3.7 9.5 8.8 8.9zM744 690.4l43.9-43.9a8.01 8.01 0 0 0-4.7-13.6L623 614c-5.1-.6-9.5 3.7-8.9 8.9L633 783.1c.8 6.6 8.9 9.4 13.6 4.7l43.7-43.7L824 877.7c3.1 3.1 8.2 3.1 11.3 0l42.4-42.3c3.1-3.1 3.1-8.2 0-11.3L744 690.4z"/>
                  </svg>
                </el-icon>
                退出全屏
              </el-button>
            </div>
          </div>
          
          <!-- 自定义图例 -->
          <div v-if="visualizationData && visualizationData.nodes.length > 0" class="custom-legend" :class="{ 'fullscreen-legend': isFullscreen }">
            <div class="legend-section">
              <h4 class="legend-title">节点类型</h4>
              <div class="legend-items">
                <div 
                  v-for="nodeType in ['User', 'Item', 'Category', 'Tag']" 
                  :key="nodeType"
                  class="legend-item"
                  :class="{ disabled: !nodeVisibility[getNodeTypeName(nodeType)] }"
                  @click="toggleNodeVisibility(nodeType)"
                >
                  <span 
                    class="legend-icon node-icon" 
                    :style="{ backgroundColor: getNodeColor(nodeType) }"
                  ></span>
                  <span class="legend-label">{{ getNodeTypeName(nodeType) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div ref="chartRef" class="chart-container" :class="{ 'fullscreen-chart': isFullscreen }"></div>
        </div>
      </template>
      
      <!-- Neo4j浏览器模式 -->
      <template v-else>
        <div class="neo4j-browser-container">
          <div class="neo4j-browser-panel">
            <div class="browser-info">
              <el-icon class="info-icon" size="48" color="#409eff">
                <svg viewBox="0 0 1024 1024">
                  <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/>
                  <path d="M623.6 316.7C593.6 290.4 554 276 512 276s-81.6 14.4-111.6 40.7C369.2 344 352 380.7 352 420v7.6c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8V420c0-44.1 43.1-80 96-80s96 35.9 96 80c0 31.1-22 59.6-56.1 72.7-21.2 8.1-39.2 22.3-52.1 40.9-13.1 19-19.8 41.3-19.8 64.9V620c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8v-22.7a48.3 48.3 0 0 1 30.9-44.8c59-22.7 97.1-74.7 97.1-132.5 0-39.3-17.2-76-48.4-103.3z"/>
                  <path d="M512 732a40 40 0 1 0 80 0 40 40 0 1 0-80 0z"/>
                </svg>
              </el-icon>
              <h3>Neo4j数据库浏览器</h3>
              <p class="browser-description">
                Neo4j浏览器是一个强大的图数据库管理工具，提供可视化查询、数据浏览、Cypher语句执行等功能。
              </p>
            </div>
            
            <div class="browser-actions">
              <el-button 
                type="primary" 
                size="large"
                @click="openNeo4jBrowser"
                :loading="neo4jLoading"
              >
                <el-icon class="mr-1"><svg viewBox="0 0 1024 1024"><path d="M759.2 419.8L697.4 358 469 586.4l-109.2-109.2-61.8 61.8L469 710l290.2-290.2z"/></svg></el-icon>
                在新窗口中打开Neo4j浏览器
              </el-button>
              
              <el-button 
                type="info" 
                size="large"
                @click="testNeo4jConnection"
                :loading="neo4jConnectionTesting"
              >
                <el-icon class="mr-1"><Refresh /></el-icon>
                测试连接
              </el-button>
              
              <el-button 
                type="default" 
                size="large"
                @click="showConnectionHelp = true"
              >
                <el-icon class="mr-1"><svg viewBox="0 0 1024 1024"><path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z"/><path d="M623.6 316.7C593.6 290.4 554 276 512 276s-81.6 14.4-111.6 40.7C369.2 344 352 380.7 352 420v7.6c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8V420c0-44.1 43.1-80 96-80s96 35.9 96 80c0 31.1-22 59.6-56.1 72.7-21.2 8.1-39.2 22.3-52.1 40.9-13.1 19-19.8 41.3-19.8 64.9V620c0 4.4 3.6 8 8 8h48c4.4 0 8-3.6 8-8v-22.7a48.3 48.3 0 0 1 30.9-44.8c59-22.7 97.1-74.7 97.1-132.5 0-39.3-17.2-76-48.4-103.3z"/><path d="M512 732a40 40 0 1 0 80 0 40 40 0 1 0-80 0z"/></svg></el-icon>
                连接帮助
              </el-button>
            </div>
            
            <div v-if="neo4jConnectionError" class="connection-status error">
              <el-alert
                title="连接测试失败"
                :description="neo4jConnectionErrorMsg"
                type="error"
                show-icon
                :closable="false"
              />
            </div>
            
            <div v-else-if="neo4jConnectionSuccess" class="connection-status success">
              <el-alert
                title="连接测试成功"
                description="Neo4j服务运行正常，可以正常访问浏览器界面"
                type="success"
                show-icon
                :closable="false"
              />
            </div>
          </div>
        </div>
      </template>
      
      <!-- 连接帮助对话框 -->
      <el-dialog
        v-model="showConnectionHelp"
        title="Neo4j连接帮助"
        width="600px"
      >
        <div class="connection-help">
          <h4>确保Neo4j服务正常运行：</h4>
          <ol>
            <li>检查Neo4j服务是否已启动（默认端口：7474）</li>
            <li>访问 <a href="http://localhost:7474" target="_blank">http://localhost:7474</a> 确认可以正常访问</li>
            <li>确保Neo4j配置允许跨域访问</li>
            <li>检查防火墙设置是否阻止了7474端口</li>
          </ol>
          
          <h4>Neo4j配置建议：</h4>
          <p>在Neo4j配置文件中添加以下配置以允许跨域访问：</p>
          <pre class="config-code">dbms.security.allow_csv_import_from_file_urls=true
dbms.security.http_strict_transport_security=false
dbms.connectors.default_listen_address=0.0.0.0</pre>
        </div>
        
        <template #footer>
          <el-button @click="showConnectionHelp = false">关闭</el-button>
          <el-button type="primary" @click="testNeo4jConnection">重新测试连接</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { algoRequest } from '@/api/algo_request'
import * as echarts from 'echarts'

// 同步状态
const syncLoading = ref(false)
const syncResult = ref<{ success: boolean; message: string } | null>(null)

// 统计信息状态
const statsLoading = ref(false)
const statistics = ref<any>(null)

// 可视化状态
const visualizationLoading = ref(false)
const visualizationData = ref<any>(null)
const chartRef = ref<HTMLElement | null>(null)
const chartInstance = ref<echarts.ECharts | null>(null)
const layoutType = ref<'force' | 'circular'>('force')
const nodeLimit = ref(300)

// 可视化模式状态
const visualizationMode = ref<'custom' | 'neo4j'>('custom')

// Neo4j浏览器状态
const neo4jBrowserUrl = ref('http://localhost:7474')
const neo4jLoading = ref(false)
const neo4jConnectionTesting = ref(false)
const neo4jConnectionError = ref(false)
const neo4jConnectionSuccess = ref(false)
const neo4jConnectionErrorMsg = ref('')
const showConnectionHelp = ref(false)

// 图例控制状态
const nodeVisibility = ref<{ [key: string]: boolean }>({
  '用户': true,
  '景点': true,
  '分类': true,
  '标签': true
})

// 全屏状态
const isFullscreen = ref(false)







// 同步数据到知识图谱
const syncToKnowledgeGraph = async () => {
  try {
    syncLoading.value = true
    syncResult.value = null
    
    const result = await algoRequest.post('/knowledge-graph/sync')
    
    syncResult.value = {
      success: true,
      message: `同步完成！${result.message || '数据已成功同步到图数据库'}`
    }
    
    ElMessage.success('数据同步成功')
    
    // 同步成功后更新统计和可视化
    fetchStatistics()
    loadVisualizationData()
  } catch (error: any) {
    console.error('同步失败:', error)
    syncResult.value = {
      success: false,
      message: error.message || '同步过程中发生错误，请稍后重试'
    }
    ElMessage.error('数据同步失败')
  } finally {
    syncLoading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    statsLoading.value = true
    const result = await algoRequest.get('/knowledge-graph/stats')
    statistics.value = result
  } catch (error: any) {
    ElMessage.error('获取统计信息失败: ' + (error.message || '请稍后重试'))
    console.error('获取统计信息失败:', error)
  } finally {
    statsLoading.value = false
  }
}

// 加载可视化数据
const loadVisualizationData = async () => {
  try {
    visualizationLoading.value = true
    
    const params = { limit: nodeLimit.value }
    console.log('请求可视化数据，参数:', params)
    
    const result = await algoRequest.get('/knowledge-graph/visualization', { params })
    console.log('获取到可视化数据:', result)
    
    if (result && result.nodes && result.edges) {
      visualizationData.value = result
      
      // 数据加载完成，准备可视化
      
      // 确保DOM更新后再初始化图表
      await nextTick()
      
      // 使用setTimeout确保容器完全渲染
      setTimeout(() => {
        initVisualization()
      }, 200)
      
    } else {
      ElMessage.warning('可视化数据格式不正确')
    }
  } catch (error: any) {
    console.error('获取可视化数据失败:', error)
    ElMessage.error('获取可视化数据失败: ' + (error.message || '请稍后重试'))
  } finally {
    visualizationLoading.value = false
  }
}

// 初始化可视化
const initVisualization = () => {
  console.log('开始初始化图表')
  
  if (!chartRef.value) {
    console.error('图表容器不存在')
    return
  }
  
  if (!visualizationData.value) {
    console.error('可视化数据不存在')
    return
  }
  
  // 检查容器尺寸
  const rect = chartRef.value.getBoundingClientRect()
  console.log('图表容器尺寸:', rect.width, 'x', rect.height)
  
  if (rect.width === 0 || rect.height === 0) {
    console.warn('图表容器尺寸为0，延迟重试')
    setTimeout(() => {
      initVisualization()
    }, 300)
    return
  }
  
  // 销毁旧实例
  if (chartInstance.value) {
    console.log('销毁旧的图表实例')
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  
  try {
    console.log('创建新的图表实例')
    chartInstance.value = echarts.init(chartRef.value)
    
    console.log('开始渲染图表')
    renderChart()
    
    // 确保图表尺寸正确
    setTimeout(() => {
      if (chartInstance.value) {
        console.log('调整图表尺寸')
        chartInstance.value.resize()
      }
    }, 100)
    
    // 监听窗口变化（只添加一次）
    if (!(window as any).__chartResizeListener) {
      (window as any).__chartResizeListener = () => {
        if (chartInstance.value) {
          chartInstance.value.resize()
        }
      }
      window.addEventListener('resize', (window as any).__chartResizeListener)
    }
    
    console.log('图表初始化完成')
  } catch (error) {
    console.error('初始化图表失败:', error)
    // 如果初始化失败，尝试重新初始化
    setTimeout(() => {
      if (chartRef.value && visualizationData.value) {
        console.log('重试图表初始化')
        initVisualization()
      }
    }, 500)
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartInstance.value || !visualizationData.value) return
  
  const { nodes, edges } = visualizationData.value
  
  // 处理节点数据
  const graphNodes = nodes.map((node: any) => ({
    id: node.id,
    name: node.name,
    category: getNodeTypeName(node.type), // 使用中文名称确保与categories匹配
    symbolSize: getNodeSize(node.type),
    itemStyle: {
      color: getNodeColor(node.type),
      borderWidth: 0,                            // 去掉白色边框
      shadowColor: 'rgba(0, 0, 0, 0.15)',
      shadowBlur: 5
    },
    label: {
      show: true,
      fontSize: 11,        // 稍微减小字体
      fontWeight: 'bold',
      color: '#fff',
      formatter: (params: any) => {
        // 根据节点类型和大小设置显示字符数
        const name = params.data.name || ''
        const nodeType = params.data.category
        let maxLength = 4  // 默认最大字符数
        
        // 根据节点类型调整显示长度 - 现在使用中文名称
        switch (nodeType) {
          case '用户': maxLength = 4; break      // 用户节点显示4个字符
          case '景点': maxLength = 3; break      // 景点节点显示3个字符  
          case '分类': maxLength = 4; break      // 分类节点显示4个字符
          default: maxLength = 3; break
        }
        
        // 从首字符开始显示，超出范围直接截断
        return name.substring(0, maxLength)
      },
      position: 'inside',
      overflow: 'hidden',    // 隐藏超出的文字
      textBorderWidth: 0,    // 去掉文字边框
      textShadowColor: 'rgba(0, 0, 0, 0.5)',  // 添加文字阴影增强可读性
      textShadowBlur: 2
    },
    tooltip: {
      formatter: (params: any) => {
        const data = params.data
        return `<div style="font-weight:bold; font-size:14px;">${data.name}</div>
                <div style="color:#666; margin-top:5px;">类型: ${data.category}</div>`
      }
    }
  }))
  
  // 处理边数据
  const graphLinks = edges.map((edge: any, index: number) => {
    // 选择主要关系类型的颜色（使用第一个关系类型）
    const primaryType = edge.types && edge.types.length > 0 ? edge.types[0] : 'VIEWED'
    
    return {
      id: edge.id,
      source: edge.source,
      target: edge.target,
      value: edge.label,
      category: 'mixed',
      lineStyle: {
        width: Math.max(1.5, getEdgeWidth(edge.total_count)),  // 确保最小粗细为1.5，更细更优雅
        opacity: 0.8,                                          // 降低透明度，让文字标签更突出
        color: getMixedEdgeColor(edge.types),                      // 使用主要关系类型的颜色
        curveness: 0,                                          // 使用直线
        shadowColor: 'rgba(0, 0, 0, 0.15)',                   // 轻微阴影
        shadowBlur: 2,
        shadowOffsetY: 1
      },
      label: {
        show: true,
        fontSize: 9,                                    // 减小字体
        color: getEdgeColor(edge.types[0]),             // 使用主要关系类型的颜色作为文字色
        fontWeight: 'bold',
        formatter: edge.label,
        position: 'middle',                             // 标签显示在边的中间位置
        backgroundColor: 'transparent',                 // 透明背景，让文字贴近边
        borderWidth: 0,                                // 去掉边框
        padding: 0,                                    // 去掉内边距，让文字更贴近边
        distance: 0                                    // 文字距离边线的距离设为0，紧贴边线
      },
      symbol: ['none', 'arrow'],
      symbolSize: [0, 12],  // 适中的箭头大小，与断开效果协调
      tooltip: {
        formatter: () => {
          const typeDetails = Object.entries(edge.type_counts).map(([type, count]) => 
            `<div style="display: flex; justify-content: space-between; margin: 2px 0;">
              <span>${getEdgeTypeName(type as string)}</span>
              <span style="font-weight: bold; color: ${getEdgeColor(type as string)};">${count}条</span>
            </div>`
          ).join('')
          
          return `<div style="min-width: 200px;">
                    <div style="font-weight: bold; font-size: 14px; margin-bottom: 8px; color: #333;">
                      ${edge.label}
                    </div>
                    <div style="border-top: 1px solid #eee; padding-top: 6px;">
                      ${typeDetails}
                    </div>
                    <div style="border-top: 1px solid #eee; padding-top: 6px; text-align: center; color: #666; font-size: 12px;">
                      总计：${edge.total_count} 条关系
                    </div>
                  </div>`
        }
      }
    }
  })
  
  // 创建节点分类 - 使用中文名称作为name，确保与图例匹配
  const nodeCategories = [
    { name: getNodeTypeName('User'), itemStyle: { color: getNodeColor('User') } },
    { name: getNodeTypeName('Item'), itemStyle: { color: getNodeColor('Item') } },
    { name: getNodeTypeName('Category'), itemStyle: { color: getNodeColor('Category') } },
    { name: getNodeTypeName('Tag'), itemStyle: { color: getNodeColor('Tag') } }
  ]
  
  // 获取所有边的类型用于图例
  const edgeTypes = [...new Set(edges.map((edge: any) => edge.type as string))]
  
  const option = {
    title: {
      text: '知识图谱可视化',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      textStyle: { color: '#fff' },
      borderRadius: 5
    },
    series: [{
      type: 'graph',
      layout: layoutType.value,
      data: graphNodes,
      links: graphLinks,
      categories: nodeCategories,
      roam: true,
      zoom: 0.4,               // 适当提高初始缩放，适应0.7倍节点大小
      focusNodeAdjacency: true,
      draggable: true,
      nodeScaleRatio: 0.6,     // 减少拖拽时的节点缩放比例
      lineStyle: {
        opacity: 0.6           // 降低边的默认透明度
      },
      force: {
        repulsion: 2000,       // 适当减少节点间斥力，适应0.7倍节点
        gravity: 0.02,         // 适当增加重力
        edgeLength: 350,       // 适当减少边长，适应较小的节点
        layoutAnimation: true,
        friction: 0.9,         // 保持摩擦力
        initLayout: 'circular' // 初始化为圆形布局
      },
      emphasis: {
        focus: 'adjacency',
        scale: 1.05,           // 最小放大比例，节点已经很大了
        lineStyle: {
          width: 3,            // 进一步减少悬停时边的粗细
          shadowBlur: 15,
          shadowColor: 'rgba(0, 0, 0, 0.4)'
        },
        label: {
          fontSize: 14,        // 悬停时字体适当增大，适应0.7倍节点
          show: true,
          formatter: (params: any) => {
            // 悬停时显示完整名称
            return params.data.name || '?'
          }
        }
      }
    }]
  }
  
  // 注意：已移除ECharts原生图例，使用自定义图例控制
  
  // 添加节点拖拽事件处理
  chartInstance.value.off('brush')
  chartInstance.value.off('brushselected')
  
  // 节点拖拽开始
  chartInstance.value.off('mousedown')
  chartInstance.value.on('mousedown', (params: any) => {
    if (params.dataType === 'node') {
      console.log('开始拖拽节点:', params.data.name)
    }
  })
  
  // 节点拖拽结束，固定节点位置
  chartInstance.value.off('mouseup')
  chartInstance.value.on('mouseup', (params: any) => {
    if (params.dataType === 'node' && chartInstance.value) {
      console.log('结束拖拽节点:', params.data.name)
      // 固定节点位置，防止被力导向算法继续移动
      const option = chartInstance.value.getOption() as any
      if (option.series && option.series[0] && option.series[0].data) {
        const nodeData = option.series[0].data.find((item: any) => item.id === params.data.id)
        if (nodeData && chartInstance.value) {
          nodeData.fixed = true
          nodeData.x = params.event.offsetX
          nodeData.y = params.event.offsetY
          chartInstance.value.setOption(option)
        }
      }
    }
  })
  
  // 双击节点取消固定
  chartInstance.value.off('dblclick')
  chartInstance.value.on('dblclick', (params: any) => {
    if (params.dataType === 'node' && chartInstance.value) {
      console.log('双击节点取消固定:', params.data.name)
      const option = chartInstance.value.getOption() as any
      if (option.series && option.series[0] && option.series[0].data) {
        const nodeData = option.series[0].data.find((item: any) => item.id === params.data.id)
        if (nodeData && nodeData.fixed) {
          delete nodeData.fixed
          delete nodeData.x
          delete nodeData.y
          chartInstance.value.setOption(option)
          ElMessage.success(`已取消固定节点: ${params.data.name}`)
        }
      }
    }
  })
  
  chartInstance.value.setOption(option)
}

// 更新可视化
const updateVisualization = () => {
  loadVisualizationData()
}

// 切换布局
const changeLayout = () => {
  if (!chartInstance.value) return
  
  const option = chartInstance.value.getOption()
  const series = option.series as any[]
  if (series && series[0]) {
    series[0].layout = layoutType.value
    chartInstance.value.setOption(option)
  }
}

// 重置布局
const resetLayout = () => {
  if (!chartInstance.value || !visualizationData.value) return
  
  console.log('重置图谱布局')
  
  // 清除所有节点的固定位置
  const option = chartInstance.value.getOption() as any
  if (option.series && option.series[0] && option.series[0].data) {
    option.series[0].data.forEach((node: any) => {
      delete node.fixed
      delete node.x
      delete node.y
    })
    
    // 重新应用布局
    chartInstance.value.setOption(option, true)
    
    // 强制重新计算布局
    setTimeout(() => {
      if (chartInstance.value) {
        chartInstance.value.resize()
      }
    }, 100)
  }
  
  ElMessage.success('布局已重置')
}

// 清除固定节点
const clearFixedNodes = () => {
  if (!chartInstance.value) return
  
  const option = chartInstance.value.getOption() as any
  if (option.series && option.series[0] && option.series[0].data) {
    let fixedCount = 0
    option.series[0].data.forEach((node: any) => {
      if (node.fixed) {
        fixedCount++
        delete node.fixed
        delete node.x
        delete node.y
      }
    })
    
    if (fixedCount > 0) {
      chartInstance.value.setOption(option)
      ElMessage.success(`已解除 ${fixedCount} 个节点的固定位置`)
    } else {
      ElMessage.info('当前没有固定的节点')
    }
  }
}

// 获取节点大小 - 调整为0.7倍大小
const getNodeSize = (type: string): number => {
  switch (type) {
    case 'User': return 63        // 用户节点 - 0.7倍 (90 * 0.7)
    case 'Item': return 59        // 景点节点 - 0.7倍 (84 * 0.7)
    case 'Category': return 70    // 分类节点 - 0.7倍 (100 * 0.7)
    case 'Tag': return 45         // 标签节点 - 较小 (64 * 0.7)
    default: return 56            // 默认节点 - 0.7倍 (80 * 0.7)
  }
}

// 获取节点颜色
const getNodeColor = (type: string): string => {
  switch (type) {
    case 'User': return '#91cc75'     // 绿色
    case 'Item': return '#5470c6'     // 蓝色
    case 'Category': return '#ee6666' // 红色
    case 'Tag': return '#fac858'      // 黄色
    default: return '#73c0de'
  }
}

// 获取混合关系的边颜色
const getMixedEdgeColor = (types: string[]): any => {
  if (types.length === 1) {
    // 单一关系类型，使用原有颜色
    return getEdgeColor(types[0])
  } else if (types.length === 2) {
    // 两种关系类型，使用线性渐变
    const color1 = getEdgeColor(types[0])
    const color2 = getEdgeColor(types[1])
    return {
      type: 'linear',
      x: 0, y: 0, x2: 1, y2: 0,
      colorStops: [
        { offset: 0, color: color1 },
        { offset: 1, color: color2 }
      ]
    }
  } else {
    // 多种关系类型，使用多色渐变
    const colors = types.map(type => getEdgeColor(type))
    const colorStops = colors.map((color, index) => ({
      offset: index / (colors.length - 1),
      color: color
    }))
    return {
      type: 'linear',
      x: 0, y: 0, x2: 1, y2: 0,
      colorStops: colorStops
    }
  }
}

// 获取边的颜色
const getEdgeColor = (type: string): string => {
  switch (type) {
    case 'CREATED': return '#f56c6c'    // 红色 - 创建关系
    case 'BELONGS_TO': return '#409eff' // 蓝色 - 归属关系
    case 'VIEWED': return '#67c23a'     // 绿色 - 浏览关系
    case 'PURCHASED': return '#e6a23c'  // 橙色 - 预约关系
    case 'FAVORITED': return '#f78989'  // 粉色 - 收藏关系
    case 'LIKED': return '#a0cfff'      // 浅蓝色 - 点赞关系
    case 'HAS_TAG': return '#fac858'    // 黄色 - 标签关系
    default: return '#909399'           // 灰色 - 默认
  }
}

// 根据边数量计算粗细 - 大幅减小，更优雅
const getEdgeWidth = (edgeCount: number): number => {
  const baseWidth = 1.5  // 大幅减少基础粗细，更优雅
  return baseWidth + (edgeCount - 1) * 0.5  // 每增加一条边，粗细增加0.5
}

// 获取边类型名称
const getEdgeTypeName = (type: string): string => {
  switch (type) {
    case 'CREATED': return '创建'
    case 'BELONGS_TO': return '属于'
    case 'VIEWED': return '浏览'
    case 'PURCHASED': return '预约'
    case 'FAVORITED': return '收藏'
    case 'LIKED': return '点赞'
    case 'HAS_TAG': return '标签'
    default: return type
  }
}

// 切换节点可见性
const toggleNodeVisibility = (nodeType: string) => {
  const typeName = getNodeTypeName(nodeType)
  nodeVisibility.value[typeName] = !nodeVisibility.value[typeName]
  updateChartVisibility()
}

// 更新图表可见性
const updateChartVisibility = () => {
  if (!chartInstance.value || !visualizationData.value) return
  
  const { nodes, edges } = visualizationData.value
  
  // 过滤节点
  const filteredNodes = nodes.filter((node: any) => {
    const typeName = getNodeTypeName(node.type)
    return nodeVisibility.value[typeName] !== false
  })
  
  // 过滤边
  const filteredEdges = edges.filter((edge: any) => {
    // 检查源节点和目标节点类型是否被选中
    const sourceTypeName = getNodeTypeName(edge.source.split('-')[0])
    const targetTypeName = getNodeTypeName(edge.target.split('-')[0])
    
    // 只要源节点和目标节点类型都被选中，就显示边
    return nodeVisibility.value[sourceTypeName] !== false && 
           nodeVisibility.value[targetTypeName] !== false
  })
  
  // 重新渲染图表
  renderChartWithData(filteredNodes, filteredEdges)
}

// 使用指定数据渲染图表
const renderChartWithData = (nodes: any[], edges: any[]) => {
  if (!chartInstance.value) return
  
  // 处理节点数据
  const graphNodes = nodes.map((node: any) => ({
    id: node.id,
    name: node.name,
    category: getNodeTypeName(node.type), // 使用中文名称确保与categories匹配
    symbolSize: getNodeSize(node.type),
    itemStyle: {
      color: getNodeColor(node.type),
      borderWidth: 0,                            // 去掉白色边框
      shadowColor: 'rgba(0, 0, 0, 0.15)',
      shadowBlur: 5
    },
    label: {
      show: true,
      fontSize: 11,        // 稍微减小字体
      fontWeight: 'bold',
      color: '#fff',
      formatter: (params: any) => {
        // 根据节点类型和大小设置显示字符数
        const name = params.data.name || ''
        const nodeType = params.data.category
        let maxLength = 4  // 默认最大字符数
        
        // 根据节点类型调整显示长度 - 现在使用中文名称
        switch (nodeType) {
          case '用户': maxLength = 4; break      // 用户节点显示4个字符
          case '景点': maxLength = 3; break      // 景点节点显示3个字符  
          case '分类': maxLength = 4; break      // 分类节点显示4个字符
          default: maxLength = 3; break
        }
        
        // 从首字符开始显示，超出范围直接截断
        return name.substring(0, maxLength)
      },
      position: 'inside',
      overflow: 'hidden',    // 隐藏超出的文字
      textBorderWidth: 0,    // 去掉文字边框
      textShadowColor: 'rgba(0, 0, 0, 0.5)',  // 添加文字阴影增强可读性
      textShadowBlur: 2
    }
  }))
  
  // 处理边数据
  const graphLinks = edges.map((edge: any, index: number) => {
    // 选择主要关系类型的颜色
    const primaryType = edge.types && edge.types.length > 0 ? edge.types[0] : 'VIEWED'
    
    return {
      id: edge.id,
      source: edge.source,
      target: edge.target,
      value: edge.label,
      category: 'mixed',
      lineStyle: {
        width: Math.max(1.5, getEdgeWidth(edge.total_count)),  // 确保最小粗细为1.5，更细更优雅
        opacity: 0.8,                                          // 降低透明度，让文字标签更突出
        color: getMixedEdgeColor(edge.types),                      // 使用主要关系类型的颜色
        curveness: 0,                                          // 使用直线
        shadowColor: 'rgba(0, 0, 0, 0.15)',                   // 轻微阴影
        shadowBlur: 2,
        shadowOffsetY: 1
      },
      label: {
        show: true,
        fontSize: 9,                                    // 减小字体
        color: getEdgeColor(edge.types[0]),             // 使用主要关系类型的颜色作为文字色
        fontWeight: 'bold',
        formatter: edge.label,
        position: 'middle',                             // 标签显示在边的中间位置
        backgroundColor: 'transparent',                 // 透明背景，让文字贴近边
        borderWidth: 0,                                // 去掉边框
        padding: 0,                                    // 去掉内边距，让文字更贴近边
        distance: 0                                    // 文字距离边线的距离设为0，紧贴边线
      },
      symbol: ['none', 'arrow'],
      symbolSize: [0, 12],  // 适中的箭头大小，与断开效果协调
      tooltip: {
        formatter: () => {
          const typeDetails = Object.entries(edge.type_counts).map(([type, count]) => 
            `<div style="display: flex; justify-content: space-between; margin: 2px 0;">
              <span>${getEdgeTypeName(type as string)}</span>
              <span style="font-weight: bold; color: ${getEdgeColor(type as string)};">${count}条</span>
            </div>`
          ).join('')
          
          return `<div style="min-width: 200px;">
                    <div style="font-weight: bold; font-size: 14px; margin-bottom: 8px; color: #333;">
                      ${edge.label}
                    </div>
                    <div style="border-top: 1px solid #eee; padding-top: 6px;">
                      ${typeDetails}
                    </div>
                    <div style="border-top: 1px solid #eee; padding-top: 6px; text-align: center; color: #666; font-size: 12px;">
                      总计：${edge.total_count} 条关系
                    </div>
                  </div>`
        }
      }
    }
  })
  
  const option = chartInstance.value.getOption() as any
  if (option.series && option.series[0]) {
    option.series[0].data = graphNodes
    option.series[0].links = graphLinks
    chartInstance.value.setOption(option)
  }
}



// 获取节点类型名称
const getNodeTypeName = (type: string | number): string => {
  const typeStr = String(type);
  switch (typeStr) {
    case 'User': return '用户'
    case 'Item': return '景点'
    case 'Category': return '分类'
    case 'Tag': return '标签'
    default: return typeStr
  }
}

// 获取关系名称
const getRelationshipName = (type: string | number): string => {
  const typeStr = String(type);
  switch (typeStr) {
    case 'VIEWED': return '浏览'
    case 'PURCHASED': return '预约'
    case 'FAVORITED': return '收藏'
    case 'LIKED': return '点赞'
    case 'BELONGS_TO': return '属于'
    case 'COMMENTED': return '评论'
    case 'CREATED': return '创建'
    case 'HAS_TAG': return '标签'
    default: return typeStr
  }
}

// 切换可视化模式
const switchVisualizationMode = (mode: string | number | boolean | undefined) => {
  const modeStr = String(mode) as 'custom' | 'neo4j'
  console.log('切换可视化模式:', modeStr)
  visualizationMode.value = modeStr
  
  if (modeStr === 'neo4j') {
    testNeo4jConnection()
  } else if (modeStr === 'custom') {
    // 切换回自定义模式时，需要重新初始化图表
    nextTick(() => {
      if (visualizationData.value && visualizationData.value.nodes && visualizationData.value.nodes.length > 0) {
        // 如果已有数据，直接重新初始化图表
        setTimeout(() => {
          initVisualization()
        }, 100)
      } else {
        // 如果没有数据，重新加载数据
        loadVisualizationData()
      }
    })
  }
}

// 测试Neo4j连接
const testNeo4jConnection = async () => {
  console.log('测试Neo4j连接')
  neo4jConnectionTesting.value = true
  neo4jConnectionError.value = false
  neo4jConnectionSuccess.value = false
  
  try {
    // 简单的连接测试
    const response = await fetch(neo4jBrowserUrl.value, {
      method: 'GET',
      mode: 'no-cors' // 由于跨域限制，使用no-cors模式
    })
    
    // 由于no-cors模式，我们无法获取具体的响应状态
    // 这里主要是为了触发网络请求，测试网络连通性
    console.log('Neo4j连接测试完成')
    neo4jConnectionSuccess.value = true
    ElMessage.success('Neo4j连接测试成功')
    
  } catch (error) {
    console.error('Neo4j连接测试失败:', error)
    neo4jConnectionError.value = true
    neo4jConnectionErrorMsg.value = '无法连接到Neo4j服务，请检查服务是否启动以及端口7474是否可访问'
    ElMessage.error('Neo4j连接测试失败')
  } finally {
    neo4jConnectionTesting.value = false
  }
}

// 在新窗口中打开Neo4j浏览器
const openNeo4jBrowser = () => {
  console.log('在新窗口中打开Neo4j浏览器')
  neo4jLoading.value = true
  
  try {
    // 在新窗口中打开Neo4j浏览器
    const newWindow = window.open(
      neo4jBrowserUrl.value, 
      'neo4j-browser',
      'width=1200,height=800,scrollbars=yes,resizable=yes,status=yes,location=yes,menubar=yes,toolbar=yes'
    )
    
    if (newWindow) {
      newWindow.focus()
      ElMessage.success('Neo4j浏览器已在新窗口中打开')
    } else {
      throw new Error('浏览器阻止了弹出窗口，请检查浏览器设置')
    }
  } catch (error: any) {
    console.error('打开Neo4j浏览器失败:', error)
    ElMessage.error('打开Neo4j浏览器失败: ' + error.message)
  } finally {
    neo4jLoading.value = false
  }
}

// 切换全屏模式
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  
  if (isFullscreen.value) {
    // 进入全屏模式
    ElMessage.success('已进入全屏模式，按ESC键或点击退出按钮可退出全屏')
    
    // 监听ESC键退出全屏
    document.addEventListener('keydown', handleEscapeKey)
    
    // 禁用页面滚动
    document.body.style.overflow = 'hidden'
    
    // 延迟调整图表尺寸，确保DOM更新完成
    nextTick(() => {
      setTimeout(() => {
        if (chartInstance.value) {
          chartInstance.value.resize()
        }
      }, 300)
    })
  } else {
    // 退出全屏模式
    ElMessage.info('已退出全屏模式')
    
    // 移除ESC键监听
    document.removeEventListener('keydown', handleEscapeKey)
    
    // 恢复页面滚动
    document.body.style.overflow = ''
    
    // 延迟调整图表尺寸
    nextTick(() => {
      setTimeout(() => {
        if (chartInstance.value) {
          chartInstance.value.resize()
        }
      }, 300)
    })
  }
}

// 处理ESC键退出全屏
const handleEscapeKey = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && isFullscreen.value) {
    toggleFullscreen()
  }
}

// 组件挂载时获取数据
onMounted(async () => {
  await fetchStatistics()
  await loadVisualizationData()
})

// 监听布局类型变化
watch(layoutType, () => {
  changeLayout()
})

// 监听可视化模式变化
watch(visualizationMode, (newMode, oldMode) => {
  console.log('可视化模式变化:', oldMode, '->', newMode)
  if (newMode === 'custom' && oldMode === 'neo4j') {
    // 从Neo4j模式切换回自定义模式时，强制重新初始化图表
    forceReinitChart()
  }
})

// 强制重新初始化图表
const forceReinitChart = () => {
  console.log('强制重新初始化图表')
  
  // 清理现有图表实例
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  
  // 确保DOM更新后再初始化
  nextTick(() => {
    setTimeout(() => {
      console.log('检查图表容器和数据状态')
      console.log('chartRef.value:', !!chartRef.value)
      console.log('visualizationData.value:', !!visualizationData.value)
      console.log('visualizationData nodes:', visualizationData.value?.nodes?.length || 0)
      
      if (chartRef.value) {
        if (visualizationData.value && visualizationData.value.nodes && visualizationData.value.nodes.length > 0) {
          console.log('重新初始化图表 - 有数据')
          initVisualization()
        } else {
          console.log('重新加载可视化数据')
          loadVisualizationData()
        }
      } else {
        console.error('图表容器不存在，延迟重试')
        setTimeout(() => {
          forceReinitChart()
        }, 300)
      }
    }, 100)
  })
}

// 组件卸载时清理
onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  
  // 清理窗口监听器
  if ((window as any).__chartResizeListener) {
    window.removeEventListener('resize', (window as any).__chartResizeListener)
    delete (window as any).__chartResizeListener
  }
  
  // 清理全屏相关状态
  if (isFullscreen.value) {
    document.removeEventListener('keydown', handleEscapeKey)
    document.body.style.overflow = ''
  }
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card,
.knowledge-graph-card,
.statistics-card,
.visualization-card {
  margin-bottom: 20px;
}

.welcome-card h1 {
  color: #409EFF;
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
}

.header-controls {
  display: flex;
  align-items: center;
}

.kg-content {
  text-align: center;
}

.kg-description,
.visualization-description {
  color: #606266;
  margin-bottom: 20px;
  line-height: 1.6;
}

.kg-actions {
  margin-bottom: 20px;
}

.mr-1 {
  margin-right: 4px;
}

.sync-result {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.sync-result :deep(.el-alert) {
  text-align: center;
}

.sync-result :deep(.el-alert__content) {
  text-align: center;
  width: 100%;
}

.sync-result :deep(.el-alert__title) {
  text-align: center;
  width: 100%;
}

.sync-result :deep(.el-alert__description) {
  text-align: center;
  width: 100%;
}

.statistics-content {
  padding: 10px 0;
}

.statistic-box {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.statistic-box.relationship {
  background-color: #f0f9eb;
}

.statistic-box h3 {
  color: #606266;
  font-size: 14px;
  margin-top: 0;
  margin-bottom: 10px;
}

.statistic-value {
  color: #409EFF;
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.statistic-box.relationship .statistic-value {
  color: #67c23a;
}

.statistic-label {
  color: #909399;
  font-size: 12px;
}

.visualization-controls {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.visualization-container {
  width: 100%;
  margin-bottom: 20px;
}

.chart-container {
  width: 100%;
  height: 500px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

/* 全屏模式样式 */
.visualization-container.fullscreen-mode {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: #fff;
  z-index: 9999;
  display: flex;
  flex-direction: column;
}

.fullscreen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.fullscreen-title h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.fullscreen-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.fullscreen-legend {
  margin: 15px 20px 10px 20px;
  flex-shrink: 0;
}

.chart-container.fullscreen-chart {
  flex: 1;
  height: auto;
  margin: 0 20px 20px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.neo4j-browser-container {
  padding: 20px;
}

.neo4j-browser-panel {
  text-align: center;
  max-width: 800px;
  margin: 0 auto;
}

.browser-info {
  margin-bottom: 30px;
}

.info-icon {
  margin-bottom: 20px;
}

.browser-info h3 {
  color: #303133;
  font-size: 24px;
  margin-bottom: 10px;
}

.browser-description {
  color: #606266;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 0;
}

.browser-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.browser-actions .el-button {
  min-width: 180px;
}

.connection-status {
  margin-top: 20px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.connection-status :deep(.el-alert) {
  text-align: center;
}

.connection-status :deep(.el-alert__content) {
  text-align: center;
  width: 100%;
}

.connection-status :deep(.el-alert__title) {
  text-align: center;
  width: 100%;
}

.connection-status :deep(.el-alert__description) {
  text-align: center;
  width: 100%;
}

.custom-legend {
  display: flex;
  justify-content: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.legend-section {
  flex: 1;
}

.legend-title {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 10px;
  text-align: center;
}

.legend-items {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid transparent;
}

.legend-item:hover {
  background-color: rgba(64, 158, 255, 0.1);
  border-color: #409eff;
}

.legend-item.disabled {
  opacity: 0.4;
  text-decoration: line-through;
}

.legend-icon {
  display: inline-block;
  width: 12px;
  height: 12px;
  margin-right: 6px;
  border-radius: 2px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.node-icon {
  border-radius: 50%;
}

.edge-icon {
  border-radius: 2px;
  height: 3px;
}

.legend-label {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.statistics-loading,
.visualization-loading,
.no-statistics,
.no-visualization {
  padding: 40px 20px;
  text-align: center;
}

/* Neo4j浏览器相关样式 */
.neo4j-browser-container {
  width: 100%;
  height: 600px;
  position: relative;
}

.neo4j-iframe-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.neo4j-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.neo4j-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.loading-text {
  margin-top: 20px;
  color: #606266;
  font-size: 14px;
}

.connection-error {
  text-align: center;
  padding: 40px 20px;
}

.error-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.connection-help h4 {
  color: #409eff;
  margin-top: 20px;
  margin-bottom: 10px;
}

.connection-help ol {
  padding-left: 20px;
  line-height: 1.8;
}

.connection-help ol li {
  margin-bottom: 8px;
}

.connection-help a {
  color: #409eff;
  text-decoration: none;
}

.connection-help a:hover {
  text-decoration: underline;
}

.connection-help p {
  line-height: 1.6;
  color: #606266;
}

.config-code {
  background-color: #f5f5f5;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  margin: 10px 0;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #606266;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 全屏模式响应式样式 */
@media (max-width: 768px) {
  .fullscreen-header {
    flex-direction: column;
    gap: 15px;
    padding: 10px 15px;
  }
  
  .fullscreen-title h3 {
    font-size: 16px;
  }
  
  .fullscreen-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .fullscreen-legend {
    margin: 10px 15px 5px 15px;
  }
  
  .chart-container.fullscreen-chart {
    margin: 0 15px 15px 15px;
  }
}

@media (max-width: 480px) {
  .fullscreen-header {
    padding: 8px 10px;
  }
  
  .fullscreen-controls .el-button {
    font-size: 12px;
    padding: 5px 8px;
  }
  
  .fullscreen-controls .el-radio-group {
    font-size: 12px;
  }
  
  .fullscreen-legend {
    margin: 8px 10px 5px 10px;
  }
  
  .chart-container.fullscreen-chart {
    margin: 0 10px 10px 10px;
  }
}


</style> 