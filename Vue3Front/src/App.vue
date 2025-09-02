<template>
  <div id="app">
    <div class="container">
      <!-- 标题 -->
      <el-card class="header-card">
        <template #header>
          <div class="card-header">
            <h1>🎬 视频帧提取器</h1>
            <p>支持多种视频格式，智能提取视频帧并生成Base64编码</p>
          </div>
        </template>
      </el-card>

      <!-- 服务状态检查 -->
      <el-card class="section-card">
        <template #header>
          <h2>🔍 服务状态</h2>
        </template>
        <div class="button-group">
          <el-button type="primary" @click="checkHealth" :loading="healthLoading">
            <el-icon><Connection /></el-icon>
            检查服务状态
          </el-button>
          <el-button @click="getSupportedFormats" :loading="formatsLoading">
            <el-icon><List /></el-icon>
            获取支持格式
          </el-button>
        </div>
        <div v-if="healthStatus" class="result-summary">
          <h4>服务状态</h4>
          <div class="result-item">
            <span class="result-label">状态:</span>
            <span :class="healthStatus.success ? 'status-success' : 'status-error'">
              {{ healthStatus.success ? '正常' : '异常' }}
            </span>
          </div>
          <div class="result-item">
            <span class="result-label">消息:</span>
            <span class="result-value">{{ healthStatus.message }}</span>
          </div>
        </div>
        
        <!-- 服务器负载均衡状态 -->
        <div class="result-summary">
          <h4>🌐 服务器负载均衡状态</h4>
          <div v-for="(server, index) in serverStatusList" :key="index" class="result-item">
            <span class="result-label">服务器{{ index + 1 }}:</span>
            <span class="result-value">
              <span :class="server.available ? 'status-success' : 'status-error'">
                {{ server.available ? '✅ 可用' : '❌ 不可用' }}
              </span>
              <span v-if="server.available && server.responseTime > 0" class="server-info">
                ({{ server.responseTime }}ms)
              </span>
              <span v-if="server.errorCount > 0" class="server-info error">
                错误: {{ server.errorCount }}
              </span>
            </span>
          </div>
          <div class="result-item">
            <span class="result-label">负载策略:</span>
            <span class="result-value">轮询算法 (Round Robin)</span>
          </div>
        </div>
        <div v-if="supportedFormats" class="result-summary">
          <h4>支持的格式</h4>
          <div class="result-item">
            <span class="result-label">视频格式:</span>
            <span class="result-value">{{ supportedFormats.videoFormats?.join(', ') }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">图片格式:</span>
            <span class="result-value">{{ supportedFormats.imageFormats?.join(', ') }}</span>
          </div>
        </div>
      </el-card>

      <!-- 视频信息获取 -->
      <el-card class="section-card">
        <template #header>
          <h2>📊 视频信息</h2>
        </template>
        <div class="form-row">
          <div class="form-item">
            <el-upload
              ref="videoInfoUpload"
              :auto-upload="false"
              :show-file-list="false"
              accept="video/*"
              @change="handleVideoInfoFileChange"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择视频文件
              </el-button>
            </el-upload>
            <span v-if="videoInfoFile" class="file-name">{{ videoInfoFile.name }}</span>
          </div>
        </div>
        <div class="button-group">
          <el-button type="success" @click="getVideoInfo" :loading="videoInfoLoading" :disabled="!videoInfoFile">
            <el-icon><VideoCamera /></el-icon>
            获取视频信息
          </el-button>
        </div>
        <div v-if="videoInfo" class="result-summary">
          <h4>视频信息</h4>
          <div class="result-item">
            <span class="result-label">文件名:</span>
            <span class="result-value">{{ videoInfo.fileName }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">时长:</span>
            <span class="result-value">{{ videoInfo.durationSeconds }}秒</span>
          </div>
          <div class="result-item">
            <span class="result-label">帧率:</span>
            <span class="result-value">{{ videoInfo.frameRate }} fps</span>
          </div>
          <div class="result-item">
            <span class="result-label">分辨率:</span>
            <span class="result-value">{{ videoInfo.width }}x{{ videoInfo.height }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">文件大小:</span>
            <span class="result-value">{{ formatFileSize(videoInfo.fileSizeBytes) }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">总帧数:</span>
            <span class="result-value">{{ videoInfo.totalFrames }}</span>
          </div>
        </div>
      </el-card>

      <!-- 视频帧提取 -->
      <el-card class="section-card">
        <template #header>
          <h2>🎬 视频帧提取</h2>
        </template>
        <div class="form-row">
          <div class="form-item">
            <el-upload
              ref="videoUpload"
              :auto-upload="false"
              :show-file-list="false"
              accept="video/*"
              @change="handleVideoFileChange"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择视频文件
              </el-button>
            </el-upload>
            <span v-if="videoFile" class="file-name">{{ videoFile.name }}</span>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-item">
            <label>提取间隔（秒）:</label>
            <el-input-number v-model="extractParams.intervalSeconds" :min="1" :max="60" placeholder="不填则提取所有帧" />
          </div>
          <div class="form-item">
            <label>开始时间（秒）:</label>
            <el-input-number v-model="extractParams.startTimeSeconds" :min="0" placeholder="0" />
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-item">
            <label>结束时间（秒）:</label>
            <el-input-number v-model="extractParams.endTimeSeconds" :min="0" placeholder="不填则到视频结束" />
          </div>
          <div class="form-item">
            <label>输出格式:</label>
            <el-select v-model="extractParams.outputFormat">
              <el-option label="JPG" value="jpg" />
              <el-option label="PNG" value="png" />
              <el-option label="BMP" value="bmp" />
            </el-select>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-item">
            <label>图片质量:</label>
            <el-slider v-model="extractParams.imageQuality" :min="1" :max="100" show-input />
          </div>
          <div class="form-item">
            <label>返回Base64:</label>
            <el-switch v-model="extractParams.returnBase64" />
          </div>
        </div>

        <div class="button-group">
          <el-button type="success" @click="extractFrames" :loading="extractLoading" :disabled="!videoFile">
            <el-icon><Picture /></el-icon>
            提取视频帧（完整信息）
          </el-button>
          <el-button type="warning" @click="extractFramesBase64Only" :loading="extractBase64Loading" :disabled="!videoFile">
            <el-icon><Document /></el-icon>
            提取视频帧（仅Base64）
          </el-button>
        </div>
      </el-card>

      <!-- JSON格式生成 -->
      <el-card class="section-card">
        <template #header>
          <h2>📝 生成JSON格式（每秒一帧）</h2>
        </template>
        <div class="form-row">
          <div class="form-item">
            <el-upload
              ref="jsonVideoUpload"
              :auto-upload="false"
              :show-file-list="false"
              accept="video/*"
              @change="handleJsonVideoFileChange"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择视频文件
              </el-button>
            </el-upload>
            <span v-if="jsonVideoFile" class="file-name">{{ jsonVideoFile.name }}</span>
          </div>
          <div class="form-item">
            <label>提示词内容:</label>
            <el-input v-model="promptText" placeholder="请描述这些图片的内容" />
          </div>
        </div>
        
        <div class="button-group">
          <el-button type="primary" @click="generateJsonFormat" :loading="jsonLoading" :disabled="!jsonVideoFile">
            <el-icon><DocumentAdd /></el-icon>
            生成JSON格式
          </el-button>
        </div>
        
        <div v-if="jsonResult" class="json-container">
          <div class="json-header">
            <h4>生成的JSON格式</h4>
            <el-button type="primary" size="small" @click="copyJsonContent">
              <el-icon><CopyDocument /></el-icon>
              复制JSON
            </el-button>
          </div>
          <div class="json-content">{{ formattedJsonResult }}</div>
        </div>
      </el-card>

      <!-- 提取结果展示 -->
      <el-card v-if="extractResult" class="section-card">
        <template #header>
          <h2>📋 提取结果</h2>
        </template>
        <div class="result-summary">
          <h4>处理结果</h4>
          <div class="result-item">
            <span class="result-label">状态:</span>
            <span :class="extractResult.success ? 'status-success' : 'status-error'">
              {{ extractResult.success ? '成功' : '失败' }}
            </span>
          </div>
          <div class="result-item">
            <span class="result-label">提取帧数:</span>
            <span class="result-value">{{ extractResult.totalFramesExtracted }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">处理时间:</span>
            <span class="result-value">{{ extractResult.processingTimeMs }}ms</span>
          </div>
        </div>
        
        <div v-if="extractResult.frames && extractResult.frames.length > 0" class="frames-grid">
          <div v-for="(frame, index) in extractResult.frames" :key="index" class="frame-item">
            <img :src="`data:image/jpeg;base64,${frame.base64Data}`" :alt="`Frame ${frame.frameNumber}`" class="frame-image" />
            <div class="frame-info">
              <div>帧号: {{ frame.frameNumber }}</div>
              <div>时间: {{ frame.timestampSeconds.toFixed(2) }}s</div>
              <div>尺寸: {{ frame.width }}x{{ frame.height }}</div>
              <el-button size="small" @click="copyBase64(frame.base64Data, index)">
                复制Base64
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-if="base64Frames && base64Frames.length > 0" class="frames-grid">
          <div v-for="(base64Data, index) in base64Frames" :key="index" class="frame-item">
            <img :src="`data:image/jpeg;base64,${base64Data}`" :alt="`Frame ${index + 1}`" class="frame-image" />
            <div class="frame-info">
              <div>帧序号: {{ index + 1 }}</div>
              <div>Base64长度: {{ base64Data.length }} 字符</div>
              <div>数据类型: 图片Base64编码</div>
              <el-button size="small" @click="copyBase64(base64Data, index)">
                复制Base64
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 操作日志 -->
      <el-card class="section-card">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <h2>📝 操作日志</h2>
            <el-button size="small" @click="clearLog">
              <el-icon><Delete /></el-icon>
              清空日志
            </el-button>
          </div>
        </template>
        <div class="log-container">
          <div v-for="(log, index) in logs" :key="index" :class="['log-item', log.type]">
            [{{ log.timestamp }}] {{ log.message }}
          </div>
          <div v-if="logs.length === 0" class="log-item info">
            暂无日志记录
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// API配置 - 服务器部署
const API_SERVERS = [
  'http://159.75.236.29:8080/api',  // 服务器1
  'http://8.156.73.160:8080/api'    // 服务器2
]

// 负载均衡配置
const loadBalancer = {
  currentIndex: 0,
  serverStatus: API_SERVERS.map(() => ({ available: true, responseTime: 0, errorCount: 0 })),
  
  // 获取下一个可用服务器
  getNextServer() {
    // 过滤可用服务器
    const availableServers = API_SERVERS.filter((_, index) => this.serverStatus[index].available)
    
    if (availableServers.length === 0) {
      // 如果没有可用服务器，重置所有服务器状态
      this.serverStatus.forEach(status => {
        status.available = true
        status.errorCount = 0
      })
      return API_SERVERS[0]
    }
    
    // 轮询算法
    this.currentIndex = (this.currentIndex + 1) % availableServers.length
    const selectedServer = availableServers[this.currentIndex]
    return selectedServer
  },
  
  // 标记服务器为不可用
  markServerUnavailable(serverUrl) {
    const index = API_SERVERS.indexOf(serverUrl)
    if (index !== -1) {
      this.serverStatus[index].available = false
      this.serverStatus[index].errorCount++
      console.warn(`服务器 ${serverUrl} 标记为不可用`)
    }
  },
  
  // 标记服务器为可用
  markServerAvailable(serverUrl) {
    const index = API_SERVERS.indexOf(serverUrl)
    if (index !== -1) {
      this.serverStatus[index].available = true
      this.serverStatus[index].errorCount = 0
      console.log(`服务器 ${serverUrl} 恢复可用`)
    }
  },
  
  // 获取服务器状态
  getServerStatus() {
    return API_SERVERS.map((server, index) => ({
      url: server,
      available: this.serverStatus[index].available,
      errorCount: this.serverStatus[index].errorCount,
      responseTime: this.serverStatus[index].responseTime
    }))
  }
}

export default {
  name: 'App',
  setup() {
    // 响应式数据
    const healthLoading = ref(false)
    const formatsLoading = ref(false)
    const videoInfoLoading = ref(false)
    const extractLoading = ref(false)
    const extractBase64Loading = ref(false)
    const jsonLoading = ref(false)
    
    const healthStatus = ref(null)
    const supportedFormats = ref(null)
    const videoInfo = ref(null)
    const extractResult = ref(null)
    const base64Frames = ref([])
    const jsonResult = ref(null)
    
    const videoInfoFile = ref(null)
    const videoFile = ref(null)
    const jsonVideoFile = ref(null)
    const promptText = ref('请描述这些图片的内容')
    
    const logs = ref([])
    const serverStatusList = ref([])
    
    // 提取参数
    const extractParams = reactive({
      intervalSeconds: null,
      startTimeSeconds: 0,
      endTimeSeconds: null,
      outputFormat: 'jpg',
      imageQuality: 85,
      returnBase64: true
    })
    
    // 更新服务器状态显示
    const updateServerStatus = () => {
      serverStatusList.value = loadBalancer.getServerStatus()
    }
    
    // 初始化服务器状态
    updateServerStatus()
    
    // 计算属性
    const formattedJsonResult = computed(() => {
      return jsonResult.value ? JSON.stringify(jsonResult.value, null, 2) : ''
    })
    
    // 工具函数
    const addLog = (message, type = 'info') => {
      const timestamp = new Date().toLocaleTimeString()
      logs.value.unshift({ message, type, timestamp })
      if (logs.value.length > 100) {
        logs.value = logs.value.slice(0, 100)
      }
    }
    
    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 Bytes'
      const k = 1024
      const sizes = ['Bytes', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }
    
    const apiRequest = async (url, options = {}, retryCount = 0) => {
      const maxRetries = API_SERVERS.length // 最多重试服务器数量次
      let lastError = null
      
      // 尝试所有可用服务器
      for (let attempt = 0; attempt < maxRetries; attempt++) {
        const serverUrl = loadBalancer.getNextServer()
        const startTime = Date.now()
        
        try {
          addLog(`尝试连接服务器: ${serverUrl}`, 'info')
          
          const response = await axios({
             url: `${serverUrl}${url}`,
             timeout: 300000, // 5分钟超时，适应大文件处理
             ...options
           })
          
          // 记录响应时间
          const responseTime = Date.now() - startTime
          const serverIndex = API_SERVERS.indexOf(serverUrl)
          if (serverIndex !== -1) {
            loadBalancer.serverStatus[serverIndex].responseTime = responseTime
          }
          
          // 标记服务器为可用
          loadBalancer.markServerAvailable(serverUrl)
          addLog(`服务器 ${serverUrl} 响应成功 (${responseTime}ms)`, 'success')
          
          return response.data
          
        } catch (error) {
          lastError = error
          const responseTime = Date.now() - startTime
          
          console.error(`服务器 ${serverUrl} 请求失败:`, error)
          addLog(`服务器 ${serverUrl} 请求失败 (${responseTime}ms): ${error.message}`, 'error')
          
          // 如果是网络错误或超时，标记服务器为不可用
          if (error.code === 'ECONNREFUSED' || 
              error.code === 'ETIMEDOUT' || 
              error.code === 'ENOTFOUND' ||
              error.message.includes('timeout') ||
              error.message.includes('Network Error')) {
            loadBalancer.markServerUnavailable(serverUrl)
          }
          
          // 如果是最后一次尝试，抛出错误
          if (attempt === maxRetries - 1) {
            break
          }
          
          // 等待一小段时间再重试
          await new Promise(resolve => setTimeout(resolve, 1000))
        }
      }
      
      // 所有服务器都失败了
      const message = lastError?.response?.data?.message || lastError?.message || '所有服务器都无法连接'
      addLog(`所有服务器请求失败: ${message}`, 'error')
      throw new Error(message)
    }
    
    // API方法
    const checkHealth = async () => {
      healthLoading.value = true
      try {
        addLog('检查服务状态...', 'info')
        const data = await apiRequest('/video/health')
        // 检查响应数据结构，确保正确解析状态
        if (data && typeof data === 'object') {
          // 如果返回的是包装在data字段中的数据
          const healthData = data.data || data
          healthStatus.value = {
            success: healthData.success !== undefined ? healthData.success : true,
            message: healthData.message || data.message || '服务正常运行'
          }
        } else {
          // 如果返回的是字符串或其他格式，默认为成功
          healthStatus.value = {
            success: true,
            message: typeof data === 'string' ? data : '服务正常运行'
          }
        }
        addLog(`服务状态检查完成: ${healthStatus.value.success ? '正常' : '异常'}`, healthStatus.value.success ? 'success' : 'error')
        ElMessage.success('服务状态检查完成')
        // 更新服务器状态显示
        updateServerStatus()
      } catch (error) {
        healthStatus.value = {
          success: false,
          message: error.message || '服务连接失败'
        }
        addLog(`服务状态检查失败: ${error.message}`, 'error')
        ElMessage.error(`检查失败: ${error.message}`)
      } finally {
        healthLoading.value = false
      }
    }
    
    const getSupportedFormats = async () => {
      formatsLoading.value = true
      try {
        addLog('获取支持格式...', 'info')
        const data = await apiRequest('/video/supported-formats')
        supportedFormats.value = data.data
        addLog('支持格式获取完成', 'success')
        ElMessage.success('支持格式获取完成')
      } catch (error) {
        addLog(`获取支持格式失败: ${error.message}`, 'error')
        ElMessage.error(`获取失败: ${error.message}`)
      } finally {
        formatsLoading.value = false
      }
    }
    
    const getVideoInfo = async () => {
      if (!videoInfoFile.value) {
        ElMessage.warning('请先选择视频文件')
        return
      }
      
      videoInfoLoading.value = true
      try {
        addLog(`获取视频信息: ${videoInfoFile.value.name}`, 'info')
        const formData = new FormData()
        formData.append('file', videoInfoFile.value)
        
        const data = await apiRequest('/video/info', {
          method: 'POST',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        
        videoInfo.value = data.data
        addLog('视频信息获取完成', 'success')
        ElMessage.success('视频信息获取完成')
      } catch (error) {
        addLog(`获取视频信息失败: ${error.message}`, 'error')
        ElMessage.error(`获取失败: ${error.message}`)
      } finally {
        videoInfoLoading.value = false
      }
    }
    
    const extractFrames = async () => {
      if (!videoFile.value) {
        ElMessage.warning('请先选择视频文件')
        return
      }
      
      extractLoading.value = true
      try {
        addLog(`开始提取视频帧: ${videoFile.value.name}`, 'info')
        const formData = new FormData()
        formData.append('file', videoFile.value)
        
        // 添加参数
        Object.entries(extractParams).forEach(([key, value]) => {
          if (value !== null && value !== undefined && value !== '') {
            formData.append(key, value)
          }
        })
        
        const data = await apiRequest('/video/extract-frames', {
          method: 'POST',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        
        extractResult.value = data
        base64Frames.value = []
        addLog(`视频帧提取完成: 共提取 ${data.totalFramesExtracted} 帧`, 'success')
        ElMessage.success('视频帧提取完成')
      } catch (error) {
        addLog(`提取视频帧失败: ${error.message}`, 'error')
        ElMessage.error(`提取失败: ${error.message}`)
      } finally {
        extractLoading.value = false
      }
    }
    
    const extractFramesBase64Only = async () => {
      if (!videoFile.value) {
        ElMessage.warning('请先选择视频文件')
        return
      }
      
      // 检查文件大小并给出提示
      const fileSizeMB = videoFile.value.size / (1024 * 1024)
      if (fileSizeMB > 100) {
        const confirmed = await ElMessageBox.confirm(
          `检测到大文件 (${fileSizeMB.toFixed(1)}MB)，Base64提取可能需要较长时间。\n建议：\n• 考虑设置提取间隔减少帧数\n• 确保网络连接稳定\n• 大文件处理时间可能超过5分钟\n\n是否继续处理？`,
          '大文件处理提示',
          {
            confirmButtonText: '继续处理',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).catch(() => false)
        
        if (!confirmed) {
          return
        }
      }
      
      extractBase64Loading.value = true
      try {
        addLog(`开始提取视频帧Base64: ${videoFile.value.name} (${fileSizeMB.toFixed(1)}MB)`, 'info')
        
        if (fileSizeMB > 50) {
          addLog('大文件Base64提取中，请耐心等待...', 'info')
          ElMessage({
            message: '大文件Base64提取中，请耐心等待，不要关闭页面',
            type: 'info',
            duration: 5000
          })
        }
        
        const formData = new FormData()
        formData.append('file', videoFile.value)
        
        // 添加参数
        Object.entries(extractParams).forEach(([key, value]) => {
          if (value !== null && value !== undefined && value !== '' && key !== 'returnBase64') {
            formData.append(key, value)
          }
        })
        
        const data = await apiRequest('/video/extract-frames-base64-only', {
          method: 'POST',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        
        extractResult.value = {
          success: data.success,
          totalFramesExtracted: data.totalFramesExtracted,
          processingTimeMs: data.processingTimeMs
        }
        base64Frames.value = data.base64Frames
        addLog(`Base64帧提取完成: 共提取 ${data.totalFramesExtracted} 帧`, 'success')
        ElMessage.success('Base64帧提取完成')
      } catch (error) {
        addLog(`提取Base64帧失败: ${error.message}`, 'error')
        if (error.message.includes('timeout')) {
          ElMessage.error('处理超时，请尝试：1. 设置提取间隔 2. 减小文件大小 3. 检查网络连接')
        } else {
          ElMessage.error(`提取失败: ${error.message}`)
        }
      } finally {
        extractBase64Loading.value = false
      }
    }
    
    const generateJsonFormat = async () => {
      if (!jsonVideoFile.value) {
        ElMessage.warning('请先选择视频文件')
        return
      }
      
      // 检查文件大小并给出提示
      const fileSizeMB = jsonVideoFile.value.size / (1024 * 1024)
      if (fileSizeMB > 100) {
        const confirmed = await ElMessageBox.confirm(
          `检测到大文件 (${fileSizeMB.toFixed(1)}MB)，处理可能需要较长时间。\n建议：\n• 确保网络连接稳定\n• 请耐心等待，不要关闭页面\n• 大文件处理时间可能超过5分钟\n\n是否继续处理？`,
          '大文件处理提示',
          {
            confirmButtonText: '继续处理',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).catch(() => false)
        
        if (!confirmed) {
          return
        }
      }
      
      jsonLoading.value = true
      try {
        addLog(`开始生成JSON格式: ${jsonVideoFile.value.name} (${fileSizeMB.toFixed(1)}MB)`, 'info')
        
        if (fileSizeMB > 50) {
          addLog('大文件处理中，请耐心等待...', 'info')
          ElMessage({
            message: '大文件处理中，请耐心等待，不要关闭页面',
            type: 'info',
            duration: 5000
          })
        }
        
        const formData = new FormData()
        formData.append('file', jsonVideoFile.value)
        formData.append('promptText', promptText.value)
        
        const data = await apiRequest('/video/extract-frames-json-format', {
          method: 'POST',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        
        jsonResult.value = data.jsonFormat
        addLog(`JSON格式生成完成: 共包含 ${data.totalFramesExtracted} 帧`, 'success')
        ElMessage.success('JSON格式生成完成')
      } catch (error) {
        addLog(`生成JSON格式失败: ${error.message}`, 'error')
        if (error.message.includes('timeout')) {
          ElMessage.error('处理超时，请尝试：1. 减小文件大小 2. 检查网络连接 3. 稍后重试')
        } else {
          ElMessage.error(`生成失败: ${error.message}`)
        }
      } finally {
        jsonLoading.value = false
      }
    }
    
    // 文件处理方法
    const handleVideoInfoFileChange = (file) => {
      videoInfoFile.value = file.raw
      addLog(`选择视频文件: ${file.name} (${formatFileSize(file.size)})`, 'info')
    }
    
    const handleVideoFileChange = (file) => {
      videoFile.value = file.raw
      addLog(`选择视频文件: ${file.name} (${formatFileSize(file.size)})`, 'info')
    }
    
    const handleJsonVideoFileChange = (file) => {
      jsonVideoFile.value = file.raw
      addLog(`选择JSON视频文件: ${file.name} (${formatFileSize(file.size)})`, 'info')
    }
    
    // 工具方法
    const copyBase64 = async (base64Data, index) => {
      try {
        // 方法1: 使用现代 Clipboard API
        if (navigator.clipboard && window.isSecureContext) {
          await navigator.clipboard.writeText(base64Data)
          addLog(`已复制第 ${index + 1} 帧的Base64编码`, 'success')
          ElMessage.success(`已复制第 ${index + 1} 帧的Base64编码`)
          return
        }
        
        // 方法2: 使用传统的 execCommand 方法作为备用
        const textArea = document.createElement('textarea')
        textArea.value = base64Data
        textArea.style.position = 'fixed'
        textArea.style.left = '-999999px'
        textArea.style.top = '-999999px'
        document.body.appendChild(textArea)
        textArea.focus()
        textArea.select()
        
        const successful = document.execCommand('copy')
        document.body.removeChild(textArea)
        
        if (successful) {
          addLog(`已复制第 ${index + 1} 帧的Base64编码`, 'success')
          ElMessage.success(`已复制第 ${index + 1} 帧的Base64编码`)
        } else {
          throw new Error('execCommand复制失败')
        }
        
      } catch (error) {
        console.error('复制失败:', error)
        addLog(`复制失败: ${error.message}`, 'error')
        
        // 方法3: 提供手动复制提示
        try {
          ElMessageBox.alert(base64Data, `请手动复制第 ${index + 1} 帧的Base64编码`, {
            confirmButtonText: '确定',
            type: 'info',
            customStyle: {
              width: '80%',
              maxWidth: '800px'
            }
          })
          addLog('已显示Base64内容供手动复制', 'info')
        } catch (modalError) {
          ElMessage.error('复制功能不可用，请检查浏览器设置')
          addLog('复制功能完全失败', 'error')
        }
      }
    }
    
    const copyJsonContent = async () => {
      if (!jsonResult.value) {
        ElMessage.warning('没有JSON内容可复制')
        return
      }
      
      const textToCopy = formattedJsonResult.value
      
      try {
        // 方法1: 使用现代 Clipboard API
        if (navigator.clipboard && window.isSecureContext) {
          await navigator.clipboard.writeText(textToCopy)
          addLog('JSON内容已复制到剪贴板', 'success')
          ElMessage.success('JSON内容已复制到剪贴板')
          return
        }
        
        // 方法2: 使用传统的 execCommand 方法作为备用
        const textArea = document.createElement('textarea')
        textArea.value = textToCopy
        textArea.style.position = 'fixed'
        textArea.style.left = '-999999px'
        textArea.style.top = '-999999px'
        document.body.appendChild(textArea)
        textArea.focus()
        textArea.select()
        
        const successful = document.execCommand('copy')
        document.body.removeChild(textArea)
        
        if (successful) {
          addLog('JSON内容已复制到剪贴板', 'success')
          ElMessage.success('JSON内容已复制到剪贴板')
        } else {
          throw new Error('execCommand复制失败')
        }
        
      } catch (error) {
        console.error('复制失败:', error)
        addLog(`复制失败: ${error.message}`, 'error')
        
        // 方法3: 提供手动复制提示
        try {
          // 创建一个模态框显示内容供用户手动复制
          ElMessageBox.alert(textToCopy, '请手动复制以下内容', {
            confirmButtonText: '确定',
            type: 'info',
            customStyle: {
              width: '80%',
              maxWidth: '800px'
            },
            beforeClose: (action, instance, done) => {
              done()
            }
          })
          addLog('已显示内容供手动复制', 'info')
        } catch (modalError) {
          ElMessage.error('复制功能不可用，请检查浏览器设置')
          addLog('复制功能完全失败', 'error')
        }
      }
    }
    
    const clearLog = () => {
      logs.value = []
      ElMessage.success('日志已清空')
    }
    
    return {
      // 响应式数据
      healthLoading,
      formatsLoading,
      videoInfoLoading,
      extractLoading,
      extractBase64Loading,
      jsonLoading,
      healthStatus,
      supportedFormats,
      videoInfo,
      extractResult,
      base64Frames,
      jsonResult,
      videoInfoFile,
      videoFile,
      jsonVideoFile,
      promptText,
      logs,
      extractParams,
      serverStatusList,
      
      // 计算属性
      formattedJsonResult,
      
      // 方法
      checkHealth,
      getSupportedFormats,
      getVideoInfo,
      extractFrames,
      extractFramesBase64Only,
      generateJsonFormat,
      handleVideoInfoFileChange,
      handleVideoFileChange,
      handleJsonVideoFileChange,
      copyBase64,
      copyJsonContent,
      clearLog,
      formatFileSize,
      updateServerStatus
    }
  }
}
</script>

<style scoped>
.header-card {
  margin-bottom: 20px;
}

.header-card .card-header {
  text-align: center;
}

.header-card h1 {
  margin: 0 0 10px 0;
  color: #409eff;
  font-size: 28px;
}

.header-card p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.section-card {
  margin-bottom: 20px;
}

.section-card h2 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.file-name {
  margin-left: 10px;
  color: #666;
  font-size: 14px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.form-item {
  flex: 1;
  min-width: 200px;
}

.form-item label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.button-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 20px;
}

/* 服务器状态样式 */
.server-info {
  font-size: 12px;
  margin-left: 8px;
  color: #666;
}

.server-info.error {
  color: #ef4444;
  font-weight: 500;
}

.result-summary h4 {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .button-group {
    flex-direction: column;
  }
  
  .server-info {
    display: block;
    margin-left: 0;
    margin-top: 4px;
  }
}
</style>