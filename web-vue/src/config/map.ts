// 高德地图配置
export const mapConfig = {
  // 高德地图Web端API Key
  // 请在高德开放平台申请：https://console.amap.com/dev/key/app
  key: 'dbe6169147f921bf8b6abba1242d3e4a',
  
  // 地图版本
  version: '2.0',
  
  // 地图插件
  plugins: [
    'AMap.Geocoder',    // 地理编码插件
    'AMap.InfoWindow',  // 信息窗口插件
    'AMap.ToolBar',     // 工具栏插件
    'AMap.Scale'        // 比例尺插件
  ],
  
  // 默认缩放级别
  defaultZoom: 10,
  
  // 地图样式
  mapStyle: 'amap://styles/normal'
}