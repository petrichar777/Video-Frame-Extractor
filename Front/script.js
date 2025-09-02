// API 基础配置
const API_BASE_URL = 'http://159.75.236.29:8080/api';

// DOM 元素
const elements = {
    // 按钮
    checkHealth: document.getElementById('checkHealth'),
    getSupportedFormats: document.getElementById('getSupportedFormats'),
    getVideoInfo: document.getElementById('getVideoInfo'),
    extractFrames: document.getElementById('extractFrames'),
    extractFramesBase64Only: document.getElementById('extractFramesBase64Only'),
    generateJsonFormat: document.getElementById('generateJsonFormat'),
    copyJsonButton: document.getElementById('copyJsonButton'),
    quickTest1: document.getElementById('quickTest1'),
    quickTest2: document.getElementById('quickTest2'),
    quickTest3: document.getElementById('quickTest3'),
    clearLog: document.getElementById('clearLog'),
    
    // 输入元素
    videoInfoFile: document.getElementById('videoInfoFile'),
    videoFile: document.getElementById('videoFile'),
    jsonVideoFile: document.getElementById('jsonVideoFile'),
    promptText: document.getElementById('promptText'),
    intervalSeconds: document.getElementById('intervalSeconds'),
    startTimeSeconds: document.getElementById('startTimeSeconds'),
    endTimeSeconds: document.getElementById('endTimeSeconds'),
    outputFormat: document.getElementById('outputFormat'),
    imageQuality: document.getElementById('imageQuality'),
    returnBase64: document.getElementById('returnBase64'),
    
    // 显示区域
    healthStatus: document.getElementById('healthStatus'),
    supportedFormats: document.getElementById('supportedFormats'),
    videoInfoResult: document.getElementById('videoInfoResult'),
    extractResult: document.getElementById('extractResult'),
    extractProgress: document.getElementById('extractProgress'),
    framesContainer: document.getElementById('framesContainer'),
    jsonResult: document.getElementById('jsonResult'),
    jsonProgress: document.getElementById('jsonProgress'),
    jsonContent: document.getElementById('jsonContent'),
    logContainer: document.getElementById('logContainer')
};

// 日志功能
class Logger {
    static log(message, type = 'info') {
        const timestamp = new Date().toLocaleTimeString();
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry ${type}`;
        logEntry.textContent = `[${timestamp}] ${message}`;
        
        elements.logContainer.appendChild(logEntry);
        elements.logContainer.scrollTop = elements.logContainer.scrollHeight;
    }
    
    static info(message) {
        this.log(message, 'info');
    }
    
    static success(message) {
        this.log(message, 'success');
    }
    
    static error(message) {
        this.log(message, 'error');
    }
    
    static warning(message) {
        this.log(message, 'warning');
    }
    
    static clear() {
        elements.logContainer.innerHTML = '';
    }
}

// API 请求工具类
class ApiClient {
    static async request(url, options = {}) {
        try {
            Logger.info(`发送请求: ${options.method || 'GET'} ${url}`);
            
            const response = await fetch(url, {
                ...options,
                headers: {
                    ...options.headers
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const data = await response.json();
            Logger.success(`请求成功: ${url}`);
            return data;
            
        } catch (error) {
            Logger.error(`请求失败: ${error.message}`);
            throw error;
        }
    }
    
    static async get(endpoint) {
        return this.request(`${API_BASE_URL}${endpoint}`);
    }
    
    static async post(endpoint, body) {
        return this.request(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            body: body
        });
    }
}

// UI 工具类
class UIHelper {
    static showLoading(element, text = '处理中...') {
        element.innerHTML = `<div class="loading"></div>${text}`;
        element.className = 'status';
    }
    
    static showSuccess(element, message) {
        element.textContent = message;
        element.className = 'status success';
    }
    
    static showError(element, message) {
        element.textContent = message;
        element.className = 'status error';
    }
    
    static showInfo(element, data) {
        if (typeof data === 'object') {
            element.innerHTML = `<pre class="json-display">${JSON.stringify(data, null, 2)}</pre>`;
        } else {
            element.textContent = data;
        }
        element.className = 'info';
    }
    
    static showResult(element, data) {
        if (typeof data === 'object') {
            element.innerHTML = `<pre class="json-display">${JSON.stringify(data, null, 2)}</pre>`;
        } else {
            element.textContent = data;
        }
        element.className = 'result';
    }
    
    static showProgress(element, show = true, message = '') {
        // 兼容旧的调用方式
        if (typeof element === 'boolean') {
            show = element;
            message = show;
            element = elements.extractProgress;
        }
        
        if (show) {
            element.textContent = message;
            element.classList.add('show');
        } else {
            element.classList.remove('show');
        }
    }
    
    static formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
    
    static formatDuration(milliseconds) {
        const seconds = Math.floor(milliseconds / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        
        if (hours > 0) {
            return `${hours}:${(minutes % 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}`;
        } else {
            return `${minutes}:${(seconds % 60).toString().padStart(2, '0')}`;
        }
    }
}

// 业务逻辑类
class VideoProcessor {
    // 检查服务健康状态
    static async checkHealth() {
        try {
            UIHelper.showLoading(elements.healthStatus, '检查服务状态...');
            const data = await ApiClient.get('/video/health');
            UIHelper.showSuccess(elements.healthStatus, `服务正常运行 - ${data.service}`);
        } catch (error) {
            UIHelper.showError(elements.healthStatus, `服务连接失败: ${error.message}`);
        }
    }
    
    // 获取支持的格式
    static async getSupportedFormats() {
        try {
            UIHelper.showLoading(elements.supportedFormats, '获取支持格式...');
            const data = await ApiClient.get('/video/supported-formats');
            
            if (data.success) {
                const formatInfo = {
                    '视频格式': data.data.videoFormats,
                    '图片格式': data.data.imageFormats
                };
                UIHelper.showInfo(elements.supportedFormats, formatInfo);
            } else {
                UIHelper.showError(elements.supportedFormats, data.message);
            }
        } catch (error) {
            UIHelper.showError(elements.supportedFormats, `获取格式失败: ${error.message}`);
        }
    }
    
    // 获取视频信息
    static async getVideoInfo() {
        const file = elements.videoInfoFile.files[0];
        if (!file) {
            UIHelper.showError(elements.videoInfoResult, '请先选择视频文件');
            return;
        }
        
        try {
            UIHelper.showLoading(elements.videoInfoResult, '分析视频信息...');
            
            const formData = new FormData();
            formData.append('file', file);
            
            const data = await ApiClient.post('/video/info', formData);
            
            if (data.success) {
                const videoInfo = data.data;
                const displayInfo = {
                    '文件名': videoInfo.fileName,
                    '时长': UIHelper.formatDuration(videoInfo.duration),
                    '帧率': `${videoInfo.frameRate} fps`,
                    '分辨率': `${videoInfo.width} x ${videoInfo.height}`,
                    '文件大小': UIHelper.formatFileSize(videoInfo.fileSize),
                    '格式': videoInfo.format.toUpperCase(),
                    '总帧数': videoInfo.totalFrames
                };
                UIHelper.showResult(elements.videoInfoResult, displayInfo);
            } else {
                UIHelper.showError(elements.videoInfoResult, data.message);
            }
        } catch (error) {
            UIHelper.showError(elements.videoInfoResult, `获取视频信息失败: ${error.message}`);
        }
    }
    
    // 提取视频帧（仅Base64编码）
    static async extractFramesBase64Only(customOptions = {}) {
        const file = elements.videoFile.files[0];
        if (!file) {
            UIHelper.showError(elements.extractResult, '请先选择视频文件');
            return;
        }
        
        try {
            UIHelper.showProgress(elements.extractProgress, true, '正在提取视频帧，请稍候...');
            UIHelper.showLoading(elements.extractResult, '处理中...');
            elements.framesContainer.innerHTML = '';
            
            const formData = new FormData();
            formData.append('file', file);
            
            // 获取参数（优先使用自定义参数）
            const options = {
                intervalSeconds: customOptions.intervalSeconds || elements.intervalSeconds.value,
                startTimeSeconds: customOptions.startTimeSeconds || elements.startTimeSeconds.value,
                endTimeSeconds: customOptions.endTimeSeconds || elements.endTimeSeconds.value,
                outputFormat: customOptions.outputFormat || elements.outputFormat.value,
                imageQuality: customOptions.imageQuality || elements.imageQuality.value
            };
            
            // 添加非空参数到FormData
            Object.entries(options).forEach(([key, value]) => {
                if (value !== '' && value !== null && value !== undefined) {
                    formData.append(key, value);
                }
            });
            
            Logger.info(`开始提取帧Base64 - 文件: ${file.name}, 参数: ${JSON.stringify(options)}`);
            
            const data = await ApiClient.post('/video/extract-frames-base64-only', formData);
            
            UIHelper.showProgress(elements.extractProgress, false);
            
            if (data.success) {
                // 显示提取结果摘要
                const summary = {
                    '处理状态': '成功',
                    '提取帧数': data.totalFramesExtracted,
                    '处理时间': `${data.processingTimeMs} ms`,
                    'Base64编码数量': data.base64Frames.length
                };
                UIHelper.showResult(elements.extractResult, summary);
                
                // 显示Base64编码的帧
                this.displayBase64Frames(data.base64Frames);
                
                Logger.success(`Base64帧提取完成 - 共提取 ${data.totalFramesExtracted} 帧`);
            } else {
                UIHelper.showError(elements.extractResult, data.message);
            }
        } catch (error) {
            UIHelper.showProgress(elements.extractProgress, false);
            UIHelper.showError(elements.extractResult, `提取Base64帧失败: ${error.message}`);
        }
    }
    
    // 提取视频帧
    static async extractFrames(customOptions = {}) {
        const file = elements.videoFile.files[0];
        if (!file) {
            UIHelper.showError(elements.extractResult, '请先选择视频文件');
            return;
        }
        
        try {
            UIHelper.showProgress(elements.extractProgress, true, '正在提取视频帧，请稍候...');
            UIHelper.showLoading(elements.extractResult, '处理中...');
            elements.framesContainer.innerHTML = '';
            
            const formData = new FormData();
            formData.append('file', file);
            
            // 获取参数（优先使用自定义参数）
            const options = {
                intervalSeconds: customOptions.intervalSeconds || elements.intervalSeconds.value,
                startTimeSeconds: customOptions.startTimeSeconds || elements.startTimeSeconds.value,
                endTimeSeconds: customOptions.endTimeSeconds || elements.endTimeSeconds.value,
                outputFormat: customOptions.outputFormat || elements.outputFormat.value,
                imageQuality: customOptions.imageQuality || elements.imageQuality.value,
                returnBase64: customOptions.returnBase64 !== undefined ? customOptions.returnBase64 : elements.returnBase64.checked
            };
            
            // 添加非空参数到FormData
            Object.entries(options).forEach(([key, value]) => {
                if (value !== '' && value !== null && value !== undefined) {
                    formData.append(key, value);
                }
            });
            
            Logger.info(`开始提取帧 - 文件: ${file.name}, 参数: ${JSON.stringify(options)}`);
            
            const data = await ApiClient.post('/video/extract-frames', formData);
            
            UIHelper.showProgress(elements.extractProgress, false);
            
            if (data.success) {
                // 显示提取结果摘要
                const summary = {
                    '处理状态': '成功',
                    '提取帧数': data.totalFramesExtracted,
                    '处理时间': `${data.processingTimeMs} ms`,
                    '视频信息': {
                        '文件名': data.videoInfo.fileName,
                        '时长': UIHelper.formatDuration(data.videoInfo.duration),
                        '帧率': `${data.videoInfo.frameRate} fps`,
                        '分辨率': `${data.videoInfo.width} x ${data.videoInfo.height}`
                    }
                };
                UIHelper.showResult(elements.extractResult, summary);
                
                // 显示提取的帧
                this.displayFrames(data.frames);
                
                Logger.success(`帧提取完成 - 共提取 ${data.totalFramesExtracted} 帧`);
            } else {
                UIHelper.showError(elements.extractResult, data.message);
            }
        } catch (error) {
            UIHelper.showProgress(elements.extractProgress, false);
            UIHelper.showError(elements.extractResult, `提取帧失败: ${error.message}`);
        }
    }
    
    // 显示Base64编码的帧
    static displayBase64Frames(base64Frames) {
        elements.framesContainer.innerHTML = '';
        
        if (!base64Frames || base64Frames.length === 0) {
            elements.framesContainer.innerHTML = '<p>没有提取到Base64帧数据</p>';
            return;
        }
        
        base64Frames.forEach((base64Data, index) => {
            const frameItem = document.createElement('div');
            frameItem.className = 'frame-item';
            
            const img = document.createElement('img');
            img.src = `data:image/jpeg;base64,${base64Data}`;
            img.alt = `Frame ${index + 1}`;
            img.loading = 'lazy';
            
            const info = document.createElement('div');
            info.className = 'frame-info';
            info.innerHTML = `
                <div>帧序号: ${index + 1}</div>
                <div>Base64长度: ${base64Data.length} 字符</div>
                <div>数据类型: 图片Base64编码</div>
            `;
            
            // 添加复制Base64按钮
            const copyBtn = document.createElement('button');
            copyBtn.textContent = '复制Base64';
            copyBtn.className = 'btn btn-small';
            copyBtn.style.marginTop = '5px';
            copyBtn.onclick = () => {
                navigator.clipboard.writeText(base64Data).then(() => {
                    Logger.success(`已复制第 ${index + 1} 帧的Base64编码`);
                }).catch(err => {
                    Logger.error(`复制失败: ${err.message}`);
                });
            };
            
            frameItem.appendChild(img);
            frameItem.appendChild(info);
            frameItem.appendChild(copyBtn);
            elements.framesContainer.appendChild(frameItem);
        });
        
        Logger.info(`显示了 ${base64Frames.length} 个Base64编码帧`);
    }
    
    // 显示提取的帧
    static displayFrames(frames) {
        elements.framesContainer.innerHTML = '';
        
        if (!frames || frames.length === 0) {
            elements.framesContainer.innerHTML = '<p>没有提取到帧数据</p>';
            return;
        }
        
        frames.forEach((frame, index) => {
            const frameItem = document.createElement('div');
            frameItem.className = 'frame-item';
            
            const img = document.createElement('img');
            img.src = `data:image/jpeg;base64,${frame.base64Data}`;
            img.alt = `Frame ${frame.frameNumber}`;
            img.loading = 'lazy';
            
            const info = document.createElement('div');
            info.className = 'frame-info';
            info.innerHTML = `
                <div>帧号: ${frame.frameNumber}</div>
                <div>时间: ${frame.timestampSeconds.toFixed(2)}s</div>
                <div>尺寸: ${frame.width}x${frame.height}</div>
            `;
            
            frameItem.appendChild(img);
            frameItem.appendChild(info);
            elements.framesContainer.appendChild(frameItem);
        });
        
        Logger.info(`显示了 ${frames.length} 个帧图像`);
    }
    
    // 生成JSON格式（每秒一帧）
    static async generateJsonFormat() {
        const file = elements.jsonVideoFile.files[0];
        if (!file) {
            UIHelper.showError(elements.jsonResult, '请先选择视频文件');
            return;
        }
        
        const promptText = elements.promptText.value.trim();
        if (!promptText) {
            UIHelper.showError(elements.jsonResult, '请输入提示词内容');
            return;
        }
        
        try {
            UIHelper.showProgress(elements.jsonProgress, true, '正在生成JSON格式，请稍候...');
            UIHelper.showLoading(elements.jsonResult, '处理中...');
            elements.jsonContent.textContent = '';
            elements.copyJsonButton.style.display = 'none';
            
            const formData = new FormData();
            formData.append('file', file);
            formData.append('promptText', promptText);
            
            Logger.info(`开始生成JSON格式 - 文件: ${file.name}, 提示词: ${promptText}`);
            
            const data = await ApiClient.post('/video/extract-frames-json-format', formData);
            
            UIHelper.showProgress(elements.jsonProgress, false);
            
            if (data.success) {
                // 显示处理结果摘要
                const summary = {
                    '处理状态': '成功',
                    '提取帧数': data.totalFramesExtracted,
                    '处理时间': `${data.processingTimeMs} ms`,
                    'JSON格式': '已生成'
                };
                UIHelper.showResult(elements.jsonResult, summary);
                
                // 显示生成的JSON
                const jsonString = JSON.stringify(data.jsonFormat, null, 2);
                elements.jsonContent.textContent = jsonString;
                elements.copyJsonButton.style.display = 'inline-block';
                
                // 存储JSON字符串供复制使用
                elements.copyJsonButton.dataset.jsonContent = jsonString;
                
                Logger.success(`JSON格式生成完成 - 共包含 ${data.totalFramesExtracted} 帧`);
            } else {
                UIHelper.showError(elements.jsonResult, data.message);
            }
        } catch (error) {
            UIHelper.showProgress(elements.jsonProgress, false);
            UIHelper.showError(elements.jsonResult, `生成JSON格式失败: ${error.message}`);
        }
    }
    
    // 复制JSON内容
    static copyJsonContent() {
        const jsonContent = elements.copyJsonButton.dataset.jsonContent;
        if (jsonContent) {
            navigator.clipboard.writeText(jsonContent).then(() => {
                Logger.success('JSON内容已复制到剪贴板');
                // 临时改变按钮文本
                const originalText = elements.copyJsonButton.textContent;
                elements.copyJsonButton.textContent = '已复制!';
                setTimeout(() => {
                    elements.copyJsonButton.textContent = originalText;
                }, 2000);
            }).catch(err => {
                Logger.error(`复制失败: ${err.message}`);
            });
        }
    }
    
    // 快速测试方法
    static async quickTest1() {
        Logger.info('开始快速测试1: 每5秒提取一帧');
        await this.extractFrames({
            intervalSeconds: 5,
            startTimeSeconds: 0,
            outputFormat: 'jpg',
            imageQuality: 85,
            returnBase64: true
        });
    }
    
    static async quickTest2() {
        Logger.info('开始快速测试2: 前30秒每2秒一帧');
        await this.extractFrames({
            intervalSeconds: 2,
            startTimeSeconds: 0,
            endTimeSeconds: 30,
            outputFormat: 'jpg',
            imageQuality: 85,
            returnBase64: true
        });
    }
    
    static async quickTest3() {
        Logger.info('开始快速测试3: 提取所有帧');
        await this.extractFrames({
            outputFormat: 'jpg',
            imageQuality: 85,
            returnBase64: true
        });
    }
}

// 事件监听器
function setupEventListeners() {
    // 基础功能按钮
    elements.checkHealth.addEventListener('click', VideoProcessor.checkHealth);
    elements.getSupportedFormats.addEventListener('click', VideoProcessor.getSupportedFormats);
    elements.getVideoInfo.addEventListener('click', VideoProcessor.getVideoInfo);
    elements.extractFrames.addEventListener('click', () => VideoProcessor.extractFrames());
    elements.extractFramesBase64Only.addEventListener('click', () => VideoProcessor.extractFramesBase64Only());
    elements.generateJsonFormat.addEventListener('click', VideoProcessor.generateJsonFormat);
    elements.copyJsonButton.addEventListener('click', VideoProcessor.copyJsonContent);
    
    // 快速测试按钮
    elements.quickTest1.addEventListener('click', VideoProcessor.quickTest1);
    elements.quickTest2.addEventListener('click', VideoProcessor.quickTest2);
    elements.quickTest3.addEventListener('click', VideoProcessor.quickTest3);
    
    // 清空日志
    elements.clearLog.addEventListener('click', Logger.clear);
    
    // 文件选择事件
    elements.videoInfoFile.addEventListener('change', function() {
        if (this.files[0]) {
            Logger.info(`选择了视频信息文件: ${this.files[0].name} (${UIHelper.formatFileSize(this.files[0].size)})`);
        }
    });
    
    elements.videoFile.addEventListener('change', function() {
        if (this.files[0]) {
            Logger.info(`选择了视频文件: ${this.files[0].name} (${UIHelper.formatFileSize(this.files[0].size)})`);
            // 自动同步到视频信息文件选择器
            elements.videoInfoFile.files = this.files;
        }
    });
    
    elements.jsonVideoFile.addEventListener('change', function() {
        if (this.files[0]) {
            Logger.info(`选择了JSON视频文件: ${this.files[0].name} (${UIHelper.formatFileSize(this.files[0].size)})`);
        }
    });
    
    elements.promptText.addEventListener('change', function() {
        Logger.info(`提示词更新: ${this.value}`);
    });
    
    // 参数变化监听
    const paramInputs = [
        elements.intervalSeconds,
        elements.startTimeSeconds,
        elements.endTimeSeconds,
        elements.outputFormat,
        elements.imageQuality,
        elements.returnBase64
    ];
    
    paramInputs.forEach(input => {
        input.addEventListener('change', function() {
            Logger.info(`参数更新: ${this.id} = ${this.type === 'checkbox' ? this.checked : this.value}`);
        });
    });
}

// 初始化应用
function initApp() {
    Logger.info('视频帧提取器已启动');
    Logger.info(`API服务地址: ${API_BASE_URL}`);
    
    // 设置事件监听器
    setupEventListeners();
    
    // 自动检查服务状态
    setTimeout(() => {
        VideoProcessor.checkHealth();
    }, 500);
    
    Logger.info('应用初始化完成');
}

// 页面加载完成后初始化
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
} else {
    initApp();
}

// 全局错误处理
window.addEventListener('error', function(event) {
    Logger.error(`全局错误: ${event.error.message}`);
});

// 导出到全局作用域（用于调试）
window.VideoProcessor = VideoProcessor;
window.Logger = Logger;
window.ApiClient = ApiClient;
window.UIHelper = UIHelper;