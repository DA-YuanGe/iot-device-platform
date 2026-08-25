<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import axios from 'axios'

const devices = ref([])
const statuses = ref({})
const telemetry = ref({})
const alarms = ref({})

const selectedDevice = ref(null)
const detailStatus = ref(null)
const detailTelemetry = ref(null)
const detailEvents = ref([])
const detailAlarm = ref(null)

const loading = ref(true)
const detailLoading = ref(false)
const error = ref('')

let refreshTimer = null

const onlineCount = computed(() => {
  return devices.value.filter(device => {
    return getDeviceStatus(device) === 'ONLINE'
  }).length
})

const offlineCount = computed(() => {
  return devices.value.length - onlineCount.value
})

const alarmCount = computed(() => {
  return Object.keys(alarms.value).length
})

async function loadData() {
  try {
    error.value = ''

    const [
      deviceResponse,
      statusResponse,
      telemetryResponse,
      alarmResponse
    ] = await Promise.all([
      axios.get('/api/devices'),
      axios.get('/api/devices/status'),
      axios.get('/api/devices/telemetry'),
      axios.get('/api/devices/alarms')
    ])

    devices.value = deviceResponse.data
    statuses.value = statusResponse.data
    telemetry.value = telemetryResponse.data
    alarms.value = alarmResponse.data

    if (selectedDevice.value) {
      await loadDeviceDetail(selectedDevice.value.device_id)
    }
  } catch (e) {
    console.error(e)
    error.value = '无法连接 IoT 后端服务，请确认 Gateway 正在运行。'
  } finally {
    loading.value = false
  }
}

function getDeviceStatus(device) {
  const runtimeStatus = statuses.value[device.device_id]

  if (runtimeStatus?.state) {
    return runtimeStatus.state
  }

  return device.status || 'UNKNOWN'
}

function getTelemetry(deviceId) {
  return telemetry.value[deviceId]
}

function formatTime(value) {
  if (!value) {
    return '--'
  }

  return String(value).replace('T', ' ')
}

function parseEvent(event) {
  if (typeof event === 'string') {
    const parts = event.split('|')

    return {
      type: parts[0] || 'EVENT',
      time: parts.slice(1).join('|') || null
    }
  }

  return {
    type: event?.type || event?.eventType || 'EVENT',
    time:
      event?.eventTime ||
      event?.time ||
      event?.timestamp ||
      null
  }
}

async function openDeviceDetail(deviceId) {
  selectedDevice.value =
    devices.value.find(device => device.device_id === deviceId)

  await loadDeviceDetail(deviceId)
}

async function loadDeviceDetail(deviceId) {
  if (!deviceId) {
    return
  }

  detailLoading.value = true

  try {
    const [
      statusResponse,
      telemetryResponse,
      eventsResponse
    ] = await Promise.all([
      axios.get(`/api/devices/${deviceId}/status`),
      axios.get(`/api/devices/${deviceId}/telemetry`),
      axios.get(`/api/devices/${deviceId}/events`)
    ])

    detailStatus.value = statusResponse.data
    detailTelemetry.value = telemetryResponse.data
    detailEvents.value = eventsResponse.data

    try {
      const alarmResponse =
        await axios.get(`/api/devices/${deviceId}/alarm`)

      detailAlarm.value = alarmResponse.data
    } catch (e) {
      if (e.response?.status === 404) {
        detailAlarm.value = null
      } else {
        throw e
      }
    }
  } catch (e) {
    console.error(e)

    if (e.response?.status === 404) {
      detailStatus.value = null
      detailTelemetry.value = null
      detailEvents.value = []
      detailAlarm.value = null
    }
  } finally {
    detailLoading.value = false
  }
}

function closeDetail() {
  selectedDevice.value = null
  detailStatus.value = null
  detailTelemetry.value = null
  detailEvents.value = []
  detailAlarm.value = null
}

onMounted(() => {
  loadData()

  refreshTimer = setInterval(loadData, 5000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<template>
  <div class="app">

    <header class="header">
      <div>
        <h1>IoT Device Monitoring Platform</h1>
        <p>企业级物联网设备接入与监控平台</p>
      </div>

      <div class="refresh-status">
        <span class="online-dot status-dot"></span>
        自动刷新：5s
      </div>
    </header>

    <main class="container">

      <div v-if="error" class="error">
        {{ error }}
      </div>

      <template v-if="!selectedDevice">

        <section class="stats">

          <div class="stat-card">
            <span class="label">设备总数</span>
            <strong>{{ devices.length }}</strong>
          </div>

          <div class="stat-card online">
            <span class="label">在线设备</span>
            <strong>{{ onlineCount }}</strong>
          </div>

          <div class="stat-card offline">
            <span class="label">离线设备</span>
            <strong>{{ offlineCount }}</strong>
          </div>

          <div class="stat-card alarm">
            <span class="label">当前告警</span>
            <strong>{{ alarmCount }}</strong>
          </div>

        </section>

        <section class="panel">

          <div class="panel-header">

            <div>
              <h2>设备列表</h2>
              <span>Device List</span>
            </div>

            <button @click="loadData">
              立即刷新
            </button>

          </div>

          <div v-if="loading" class="loading">
            正在加载设备数据...
          </div>

          <div v-else-if="devices.length === 0" class="empty">
            暂无设备
          </div>

          <div v-else class="table-wrapper">

            <table>

              <thead>
                <tr>
                  <th>设备编号</th>
                  <th>设备名称</th>
                  <th>设备类型</th>
                  <th>运行状态</th>
                  <th>最后上线</th>
                  <th>最后离线</th>
                  <th>遥测数据</th>
                </tr>
              </thead>

              <tbody>

                <tr
                  v-for="device in devices"
                  :key="device.device_id"
                >

                  <td class="device-id">

                    <button
                      class="device-link"
                      @click="openDeviceDetail(device.device_id)"
                    >
                      {{ device.device_id }}
                    </button>

                  </td>

                  <td>
                    {{ device.device_name }}
                  </td>

                  <td>
                    {{ device.device_type }}
                  </td>

                  <td>

                    <span
                      class="status"
                      :class="{
                        online: getDeviceStatus(device) === 'ONLINE',
                        offline: getDeviceStatus(device) === 'OFFLINE',
                        unknown: getDeviceStatus(device) === 'UNKNOWN'
                      }"
                    >

                      <span class="status-dot"></span>

                      {{ getDeviceStatus(device) }}

                    </span>

                  </td>

                  <td>
                    {{ formatTime(device.last_online_time) }}
                  </td>

                  <td>
                    {{ formatTime(device.last_offline_time) }}
                  </td>

                  <td>

                    <div
                      v-if="getTelemetry(device.device_id)"
                      class="telemetry"
                    >

                      <span>
                        温度
                        {{ getTelemetry(device.device_id).temperature }}℃
                      </span>

                      <span>
                        湿度
                        {{ getTelemetry(device.device_id).humidity }}%
                      </span>

                      <span>
                        电压
                        {{ getTelemetry(device.device_id).voltage }}V
                      </span>

                    </div>

                    <span v-else class="muted">
                      暂无数据
                    </span>

                  </td>

                </tr>

              </tbody>

            </table>

          </div>

        </section>

      </template>

      <template v-else>

        <div class="detail-header">

          <button
            class="back-button"
            @click="closeDetail"
          >
            ← 返回设备列表
          </button>

          <div>
            <h2>{{ selectedDevice.device_id }}</h2>
            <p>{{ selectedDevice.device_name }}</p>
          </div>

        </div>

        <div v-if="detailLoading" class="loading">
          正在加载设备详情...
        </div>

        <template v-else>

          <section class="detail-grid">

            <div class="detail-card">

              <span class="detail-label">
                当前状态
              </span>

              <strong
                class="detail-status"
                :class="{
                  online: detailStatus?.state === 'ONLINE',
                  offline: detailStatus?.state === 'OFFLINE'
                }"
              >

                <span class="status-dot"></span>

                {{ detailStatus?.state || selectedDevice.status || '--' }}

              </strong>

            </div>

            <div class="detail-card">

              <span class="detail-label">
                最后上线
              </span>

              <strong>
                {{
                  formatTime(
                    selectedDevice.last_online_time
                  )
                }}
              </strong>

            </div>

            <div class="detail-card">

              <span class="detail-label">
                最后离线
              </span>

              <strong>
                {{
                  formatTime(
                    selectedDevice.last_offline_time
                  )
                }}
              </strong>

            </div>

            <div class="detail-card">

              <span class="detail-label">
                最后心跳
              </span>

              <strong>
                {{
                  formatTime(
                    detailStatus?.lastHeartbeatTime
                  )
                }}
              </strong>

            </div>

          </section>

          <section class="panel">

            <div class="panel-header">

              <div>
                <h2>最新遥测数据</h2>
                <span>Latest Telemetry</span>
              </div>

            </div>

            <div class="telemetry-grid">

              <div class="metric">

                <span>温度</span>

                <strong>
                  {{ detailTelemetry?.temperature ?? '--' }}
                  <small>℃</small>
                </strong>

              </div>

              <div class="metric">

                <span>湿度</span>

                <strong>
                  {{ detailTelemetry?.humidity ?? '--' }}
                  <small>%</small>
                </strong>

              </div>

              <div class="metric">

                <span>电压</span>

                <strong>
                  {{ detailTelemetry?.voltage ?? '--' }}
                  <small>V</small>
                </strong>

              </div>

              <div class="metric">

                <span>上报时间</span>

                <strong class="time">
                  {{
                    formatTime(
                      detailTelemetry?.reportTime
                    )
                  }}
                </strong>

              </div>

            </div>

          </section>

          <section class="panel">

            <div class="panel-header">

              <div>
                <h2>当前告警</h2>
                <span>Active Alarm</span>
              </div>

            </div>

            <div
              v-if="detailAlarm"
              class="alarm-detail"
            >

              <div>

                <strong>
                  {{ detailAlarm.type }}
                </strong>

                <span>
                  {{ detailAlarm.level }}
                </span>

              </div>

              <p>
                {{ detailAlarm.content || '设备异常' }}
              </p>

              <small>
                {{ formatTime(detailAlarm.time) }}
              </small>

            </div>

            <div v-else class="empty">
              当前无未恢复告警
            </div>

          </section>

          <section class="panel">

            <div class="panel-header">

              <div>
                <h2>设备事件</h2>
                <span>Device Events</span>
              </div>

            </div>

            <div v-if="detailEvents.length">

              <div
                v-for="(event, index) in [...detailEvents].reverse()"
                :key="index"
                class="event-item"
              >

                <div class="event-type">
                  {{ parseEvent(event).type }}
                </div>

                <div class="event-time">
                  {{
                    formatTime(
                      parseEvent(event).time
                    )
                  }}
                </div>

              </div>

            </div>

            <div v-else class="empty">
              暂无设备事件记录
            </div>

          </section>

        </template>

      </template>

    </main>

  </div>
</template>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family:
    Inter,
    -apple-system,
    BlinkMacSystemFont,
    "Segoe UI",
    sans-serif;
  background: #f4f6f8;
  color: #1f2937;
}

button {
  cursor: pointer;
}

.app {
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 48px;
  background: #111827;
  color: white;
}

.header h1 {
  margin: 0 0 8px;
  font-size: 26px;
}

.header p {
  margin: 0;
  color: #9ca3af;
}

.refresh-status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #d1d5db;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 40px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  padding: 24px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.stat-card .label {
  display: block;
  color: #6b7280;
  margin-bottom: 10px;
}

.stat-card strong {
  font-size: 32px;
}

.stat-card.online strong {
  color: #16a34a;
}

.stat-card.offline strong {
  color: #dc2626;
}

.stat-card.alarm strong {
  color: #d97706;
}

.panel {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
  margin-bottom: 24px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 24px;
  border-bottom: 1px solid #e5e7eb;
}

.panel-header h2 {
  margin: 0 0 4px;
  font-size: 20px;
}

.panel-header span {
  color: #9ca3af;
  font-size: 13px;
}

.panel-header button {
  padding: 8px 16px;
  border: 0;
  border-radius: 6px;
  background: #2563eb;
  color: white;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
  white-space: nowrap;
}

th {
  background: #f9fafb;
  color: #6b7280;
  font-weight: 500;
  font-size: 14px;
}

.device-id {
  font-weight: 600;
}

.device-link {
  padding: 0;
  border: 0;
  background: none;
  color: #2563eb;
  font-size: inherit;
  font-weight: 600;
}

.device-link:hover {
  text-decoration: underline;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  font-weight: 600;
}

.status.online {
  color: #16a34a;
}

.status.offline {
  color: #dc2626;
}

.status.unknown {
  color: #6b7280;
}

.status-dot {
  width: 8px;
  height: 8px;
  display: inline-block;
  border-radius: 50%;
  background: #9ca3af;
}

.status.online .status-dot,
.detail-status.online .status-dot {
  background: #16a34a;
}

.status.offline .status-dot,
.detail-status.offline .status-dot {
  background: #dc2626;
}

.online-dot {
  background: #16a34a;
}

.telemetry {
  display: flex;
  gap: 14px;
  font-size: 13px;
}

.muted {
  color: #9ca3af;
}

.loading,
.empty {
  padding: 40px;
  text-align: center;
  color: #6b7280;
}

.error {
  margin-bottom: 20px;
  padding: 16px;
  background: #fef2f2;
  color: #b91c1c;
  border-radius: 8px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 28px;
}

.detail-header h2 {
  margin: 0 0 6px;
  font-size: 28px;
}

.detail-header p {
  margin: 0;
  color: #6b7280;
}

.back-button {
  padding: 10px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: white;
  color: #374151;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.detail-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 24px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

.detail-label {
  color: #6b7280;
  font-size: 14px;
}

.detail-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
}

.detail-status.online {
  color: #16a34a;
}

.detail-status.offline {
  color: #dc2626;
}

.telemetry-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 24px;
}

.metric {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 10px;
}

.metric span {
  color: #6b7280;
  font-size: 14px;
}

.metric strong {
  font-size: 28px;
}

.metric small {
  font-size: 14px;
  font-weight: normal;
}

.metric .time {
  font-size: 16px;
}

.alarm-detail {
  padding: 24px;
}

.alarm-detail > div {
  display: flex;
  align-items: center;
  gap: 12px;
}

.alarm-detail strong {
  font-size: 18px;
  color: #dc2626;
}

.alarm-detail span {
  padding: 4px 8px;
  border-radius: 4px;
  background: #fef2f2;
  color: #dc2626;
  font-size: 12px;
}

.alarm-detail p {
  margin: 12px 0;
  color: #4b5563;
}

.alarm-detail small {
  color: #9ca3af;
}

.event-item {
  display: flex;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.event-type {
  font-weight: 600;
}

.event-time {
  color: #6b7280;
}

@media (max-width: 1000px) {
  .stats,
  .detail-grid,
  .telemetry-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .stats,
  .detail-grid,
  .telemetry-grid {
    grid-template-columns: 1fr;
  }

  .header {
    padding: 24px;
  }

  .header h1 {
    font-size: 20px;
  }

  .container {
    padding: 24px;
  }

  .detail-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
