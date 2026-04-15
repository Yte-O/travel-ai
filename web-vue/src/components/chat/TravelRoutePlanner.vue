<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { ItineraryPlan, ItineraryDay, ItineraryItem } from '@/types/chat'
import { mapConfig } from '@/config/map'

const props = defineProps<{
	plan: ItineraryPlan
	loading?: boolean
}>()

const activeDay = ref(1)
const mapContainer = ref<HTMLDivElement | null>(null)
const mapInstance = ref<any>(null)
const mapMarkers = ref<any[]>([])
const mapPolylines = ref<any[]>([])

const days = computed<ItineraryDay[]>(() => props.plan?.days || [])

const currentDay = computed<ItineraryDay | null>(() => {
	if (!days.value.length) return null
	return days.value.find(d => d.day === activeDay.value) || days.value[0]
})

const totalItems = computed(() => days.value.reduce((sum, d) => sum + d.items.length, 0))

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

const clearMapOverlays = () => {
	if (!mapInstance.value) return
	mapMarkers.value.forEach(marker => mapInstance.value.remove(marker))
	mapPolylines.value.forEach(line => mapInstance.value.remove(line))
	mapMarkers.value = []
	mapPolylines.value = []
}

const normalizeLocation = (item: ItineraryItem): [number, number] | null => {
	const loc = item.location as number[]
	if (!Array.isArray(loc) || loc.length < 2) return null
	const lng = Number(loc[0])
	const lat = Number(loc[1])
	if (Number.isNaN(lng) || Number.isNaN(lat)) return null
	return [lng, lat]
}

const drawCurrentDay = () => {
	if (!mapInstance.value || !currentDay.value) return

	clearMapOverlays()

	const pathPoints: any[] = []

	currentDay.value.items.forEach((item, index) => {
		const point = normalizeLocation(item)
		if (!point) return

		pathPoints.push(point)

		const marker = new window.AMap.Marker({
			position: point,
			title: item.name,
			label: {
				content: `${index + 1}`,
				direction: 'top'
			}
		})

		const infoWindow = new window.AMap.InfoWindow({
			content: `
				<div style="min-width: 180px;">
					<div style="font-weight: 600; margin-bottom: 4px;">${item.name}</div>
					<div style="font-size: 12px; color: #666;">${item.startTime} - ${item.endTime}</div>
					<div style="font-size: 12px; color: #999; margin-top: 4px;">${item.address || ''}</div>
				</div>
			`,
			offset: new window.AMap.Pixel(0, -25)
		})

		marker.on('click', () => infoWindow.open(mapInstance.value, marker.getPosition()))
		mapInstance.value.add(marker)
		mapMarkers.value.push(marker)

		if (item.transportFromPrev?.polyline?.length) {
			const line = new window.AMap.Polyline({
				path: item.transportFromPrev.polyline,
				borderWeight: 2,
				strokeWeight: 5,
				strokeColor: '#1d4ed8',
				lineJoin: 'round',
				lineCap: 'round'
			})
			mapInstance.value.add(line)
			mapPolylines.value.push(line)
		}
	})

	if (!mapPolylines.value.length && pathPoints.length > 1) {
		const fallbackLine = new window.AMap.Polyline({
			path: pathPoints,
			borderWeight: 2,
			strokeWeight: 5,
			strokeColor: '#0ea5e9',
			lineJoin: 'round',
			lineCap: 'round'
		})
		mapInstance.value.add(fallbackLine)
		mapPolylines.value.push(fallbackLine)
	}

	if (pathPoints.length) {
		mapInstance.value.setFitView([...mapMarkers.value, ...mapPolylines.value], false, [60, 60, 60, 60])
	}
}

const initMap = async () => {
	if (!mapContainer.value || mapInstance.value) return
	try {
		await loadAMapScript()
		mapInstance.value = new window.AMap.Map(mapContainer.value, {
			zoom: 12,
			center: [116.397428, 39.90923],
			mapStyle: mapConfig.mapStyle,
			viewMode: '3D'
		})
		mapInstance.value.addControl(new window.AMap.Scale())
		mapInstance.value.addControl(new window.AMap.ToolBar())
		drawCurrentDay()
	} catch (e) {
		ElMessage.error('路书地图加载失败')
		console.error(e)
	}
}

watch(
	() => props.plan,
	async (plan) => {
		if (plan?.days?.length) {
			activeDay.value = plan.days[0].day
			await nextTick()
			await initMap()
			drawCurrentDay()
		}
	},
	{ deep: true, immediate: true }
)

watch(activeDay, async () => {
	await nextTick()
	if (!mapInstance.value) {
		await initMap()
	}
	drawCurrentDay()
})

onUnmounted(() => {
	if (mapInstance.value) {
		mapInstance.value.destroy()
		mapInstance.value = null
	}
})

declare global {
	interface Window {
		AMap: any
	}
}
</script>

<template>
	<div class="route-planner-wrapper">
		<div class="planner-head">
			<div>
				<h3>{{ plan.title }}</h3>
				<p>
					<span v-if="plan.city">目的地：{{ plan.city }}</span>
					<span>共 {{ days.length }} 天 · {{ totalItems }} 个点位</span>
				</p>
			</div>
			<el-tag size="small" effect="plain" type="success" v-if="plan.travelMode">{{ plan.travelMode }}</el-tag>
		</div>

		<div class="day-tabs">
			<button
				v-for="day in days"
				:key="day.day"
				class="day-tab"
				:class="{ active: day.day === activeDay }"
				@click="activeDay = day.day"
			>
				Day {{ day.day }} · {{ day.theme }}
			</button>
		</div>

		<div class="planner-body" v-loading="loading">
			<div class="map-panel">
				<div ref="mapContainer" class="map-canvas"></div>
			</div>

			<div class="timeline-panel" v-if="currentDay">
				<div class="day-summary">
					<div class="summary-title">当日概览</div>
					<div class="summary-text">{{ currentDay.summary || '按时段完成城市探索' }}</div>
				</div>

				<div class="timeline-list">
					<div v-for="(item, idx) in currentDay.items" :key="`${item.name}-${idx}`" class="timeline-item">
						<div class="timeline-index">{{ idx + 1 }}</div>
						<div class="timeline-content">
							<div class="time">{{ item.startTime }} - {{ item.endTime }}</div>
							<div class="name">{{ item.name }}</div>
							<div class="address">{{ item.address || item.adname || '地址待补充' }}</div>
							<div class="transport" v-if="item.transportFromPrev">
								路段：{{ item.transportFromPrev.mode }} · {{ item.transportFromPrev.distanceKm }} km · {{ item.transportFromPrev.durationMinutes }} 分钟
							</div>
							<div class="tips" v-if="item.tips">{{ item.tips }}</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<style scoped>
.route-planner-wrapper {
	margin-top: 16px;
	border: 1px solid #e5e7eb;
	border-radius: 12px;
	background: #ffffff;
	overflow: hidden;
}

.planner-head {
	padding: 14px 16px;
	display: flex;
	justify-content: space-between;
	align-items: center;
	border-bottom: 1px solid #eef2f7;
	background: linear-gradient(135deg, #eff6ff, #f8fafc);
}

.planner-head h3 {
	margin: 0;
	font-size: 16px;
	color: #0f172a;
}

.planner-head p {
	margin: 4px 0 0;
	display: flex;
	gap: 12px;
	font-size: 12px;
	color: #64748b;
}

.day-tabs {
	display: flex;
	gap: 8px;
	padding: 12px 16px;
	flex-wrap: wrap;
	border-bottom: 1px solid #eef2f7;
}

.day-tab {
	border: 1px solid #cbd5e1;
	background: #fff;
	color: #334155;
	border-radius: 999px;
	padding: 6px 12px;
	cursor: pointer;
	font-size: 12px;
}

.day-tab.active {
	border-color: #1d4ed8;
	color: #1d4ed8;
	background: #dbeafe;
}

.planner-body {
	display: grid;
	grid-template-columns: 1.2fr 1fr;
	min-height: 420px;
}

.map-panel {
	border-right: 1px solid #eef2f7;
}

.map-canvas {
	width: 100%;
	height: 100%;
	min-height: 420px;
}

.timeline-panel {
	padding: 12px;
	overflow: auto;
	max-height: 520px;
}

.day-summary {
	margin-bottom: 12px;
	padding: 10px;
	border-radius: 8px;
	background: #f8fafc;
	border: 1px solid #e2e8f0;
}

.summary-title {
	font-size: 12px;
	color: #64748b;
}

.summary-text {
	margin-top: 4px;
	font-size: 13px;
	color: #0f172a;
}

.timeline-list {
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.timeline-item {
	display: grid;
	grid-template-columns: 30px 1fr;
	gap: 10px;
	padding: 10px;
	border: 1px solid #e2e8f0;
	border-radius: 8px;
}

.timeline-index {
	width: 30px;
	height: 30px;
	border-radius: 50%;
	background: #dbeafe;
	color: #1d4ed8;
	display: flex;
	align-items: center;
	justify-content: center;
	font-weight: 600;
	font-size: 12px;
}

.time {
	font-size: 12px;
	color: #64748b;
}

.name {
	font-size: 14px;
	font-weight: 600;
	margin-top: 2px;
	color: #0f172a;
}

.address,
.transport,
.tips {
	margin-top: 4px;
	font-size: 12px;
	color: #475569;
}

@media (max-width: 960px) {
	.planner-body {
		grid-template-columns: 1fr;
	}

	.map-panel {
		border-right: none;
		border-bottom: 1px solid #eef2f7;
	}

	.map-canvas {
		min-height: 320px;
	}
}
</style>
