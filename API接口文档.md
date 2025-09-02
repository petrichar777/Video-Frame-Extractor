# 视频帧提取器 API 接口文档

## 基础信息

- **服务地址**: http://localhost:8080
- **API基础路径**: /api
- **Content-Type**: multipart/form-data (文件上传接口)
- **响应格式**: JSON

## 接口列表

### 1. 健康检查

**接口地址**: `GET /api/video/health`

**功能描述**: 检查服务是否正常运行

**请求参数**: 无

**响应示例**:
```json
{
  "status": "UP",
  "service": "Video Frame Extractor",
  "timestamp": 1703123456789
}
```

**测试命令**:
```bash
curl -X GET http://localhost:8080/api/video/health
```

---

### 2. 获取支持的格式

**接口地址**: `GET /api/video/supported-formats`

**功能描述**: 获取支持的视频和图片格式列表

**请求参数**: 无

**响应示例**:
```json
{
  "success": true,
  "message": "获取支持格式成功",
  "data": {
    "videoFormats": ["mp4", "avi", "mov", "mkv", "wmv", "flv", "webm"],
    "imageFormats": ["jpg", "jpeg", "png", "bmp"]
  },
  "timestamp": 1703123456789
}
```

**测试命令**:
```bash
curl -X GET http://localhost:8080/api/video/supported-formats
```

---

### 3. 获取视频信息

**接口地址**: `POST /api/video/info`

**功能描述**: 上传视频文件并获取其基本信息

**请求参数**:
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| file | File | 是 | 视频文件 |

**响应示例**:
```json
{
  "success": true,
  "message": "获取视频信息成功",
  "data": {
    "fileName": "sample.mp4",
    "duration": 30000,
    "frameRate": 30.0,
    "width": 1920,
    "height": 1080,
    "fileSize": 5242880,
    "format": "mp4",
    "totalFrames": 900
  },
  "timestamp": 1703123456789
}
```

**测试命令**:
```bash
curl -X POST http://localhost:8080/api/video/info \
  -F "file=@/path/to/your/video.mp4"
```

---

### 4. 提取视频帧（表单参数）

**接口地址**: `POST /api/video/extract-frames`

**功能描述**: 提取视频帧并返回base64编码

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| file | File | 是 | - | 视频文件 |
| intervalSeconds | Integer | 否 | null | 提取间隔秒数，不指定则提取所有帧 |
| startTimeSeconds | Integer | 否 | 0 | 开始时间（秒） |
| endTimeSeconds | Integer | 否 | null | 结束时间（秒），不指定则到视频结束 |
| outputFormat | String | 否 | jpg | 输出图片格式 |
| imageQuality | Integer | 否 | 85 | 图片质量（1-100） |
| returnBase64 | Boolean | 否 | true | 是否返回base64编码 |

**响应示例**:
```json
{
  "success": true,
  "message": "帧提取成功",
  "videoInfo": {
    "fileName": "sample.mp4",
    "duration": 30000,
    "frameRate": 30.0,
    "width": 1920,
    "height": 1080,
    "fileSize": 5242880,
    "format": "mp4",
    "totalFrames": 900
  },
  "frames": [
    {
      "frameNumber": 1,
      "timestampSeconds": 0.0,
      "base64Data": "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBD...",
      "width": 1920,
      "height": 1080
    },
    {
      "frameNumber": 2,
      "timestampSeconds": 5.0,
      "base64Data": "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBD...",
      "width": 1920,
      "height": 1080
    }
  ],
  "totalFramesExtracted": 2,
  "processingTimeMs": 1500
}
```

**测试命令**:

1. **提取所有帧**:
```bash
curl -X POST http://localhost:8080/api/video/extract-frames \
  -F "file=@/path/to/your/video.mp4"
```

2. **每5秒提取一帧**:
```bash
curl -X POST http://localhost:8080/api/video/extract-frames \
  -F "file=@/path/to/your/video.mp4" \
  -F "intervalSeconds=5"
```

3. **提取指定时间范围的帧**:
```bash
curl -X POST http://localhost:8080/api/video/extract-frames \
  -F "file=@/path/to/your/video.mp4" \
  -F "startTimeSeconds=10" \
  -F "endTimeSeconds=20" \
  -F "intervalSeconds=2"
```

4. **自定义图片格式和质量**:
```bash
curl -X POST http://localhost:8080/api/video/extract-frames \
  -F "file=@/path/to/your/video.mp4" \
  -F "intervalSeconds=3" \
  -F "outputFormat=png" \
  -F "imageQuality=95"
```

---

### 5. 提取视频帧（仅返回Base64编码）

**接口地址**: `POST /api/video/extract-frames-base64-only`

**功能描述**: 提取视频帧并只返回base64编码字符串列表，简化响应格式

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 描述 |
|--------|------|------|--------|------|
| file | File | 是 | - | 视频文件 |
| intervalSeconds | Integer | 否 | null | 提取间隔秒数，不指定则提取所有帧 |
| startTimeSeconds | Integer | 否 | 0 | 开始时间（秒） |
| endTimeSeconds | Integer | 否 | null | 结束时间（秒），不指定则到视频结束 |
| outputFormat | String | 否 | jpg | 输出图片格式 |
| imageQuality | Integer | 否 | 85 | 图片质量（1-100） |

**响应示例**:
```json
{
  "success": true,
  "message": "帧提取成功",
  "base64Frames": [
    "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBD...",
    "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBD...",
    "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBD..."
  ],
  "totalFramesExtracted": 3,
  "processingTimeMs": 1200,
  "timestamp": 1703123456789
}
```

**测试命令**:

1. **每5秒提取一帧（仅Base64）**:
```bash
curl -X POST http://localhost:8080/api/video/extract-frames-base64-only \
  -F "file=@/path/to/your/video.mp4" \
  -F "intervalSeconds=5"
```

2. **提取前30秒每2秒一帧（仅Base64）**:
```bash
curl -X POST http://localhost:8080/api/video/extract-frames-base64-only \
  -F "file=@/path/to/your/video.mp4" \
  -F "startTimeSeconds=0" \
  -F "endTimeSeconds=30" \
  -F "intervalSeconds=2"
```

---

### 6. 提取视频帧（JSON参数）

**接口地址**: `POST /api/video/extract-frames-json`

**功能描述**: 使用JSON格式参数提取视频帧

**请求参数**:
- `file`: 视频文件（multipart/form-data）
- `request`: JSON格式的请求参数（multipart/form-data）

**JSON参数结构**:
```json
{
  "intervalSeconds": 5,
  "startTimeSeconds": 0,
  "endTimeSeconds": 30,
  "outputFormat": "jpg",
  "imageQuality": 85,
  "returnBase64": true
}
```

**响应格式**: 与接口4相同

**测试命令**:
```bash
curl -X POST http://localhost:8080/api/video/extract-frames-json \
  -F "file=@/path/to/your/video.mp4" \
  -F 'request={"intervalSeconds":5,"startTimeSeconds":0,"endTimeSeconds":30,"outputFormat":"jpg","imageQuality":85,"returnBase64":true};type=application/json'
```

---

## 错误响应格式

当请求出现错误时，API会返回以下格式的错误响应：

```json
{
  "success": false,
  "message": "错误描述信息",
  "timestamp": 1703123456789
}
```

**常见错误码**:
- `400 Bad Request`: 参数错误或文件格式不支持
- `413 Payload Too Large`: 文件大小超过限制（500MB）
- `500 Internal Server Error`: 服务器内部错误

---

## 使用注意事项

1. **文件大小限制**: 最大支持500MB的视频文件
2. **支持的视频格式**: MP4, AVI, MOV, MKV, WMV, FLV, WebM
3. **支持的图片格式**: JPG, JPEG, PNG, BMP
4. **时间参数**: 所有时间参数均以秒为单位
5. **Base64数据**: 返回的base64数据可直接用于HTML img标签显示
6. **性能考虑**: 提取大量帧可能耗时较长，建议合理设置间隔时间

---

## 前端集成示例

### JavaScript Fetch API

```javascript
// 获取视频信息
async function getVideoInfo(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await fetch('http://localhost:8080/api/video/info', {
    method: 'POST',
    body: formData
  });
  
  return await response.json();
}

// 提取视频帧
async function extractFrames(file, options = {}) {
  const formData = new FormData();
  formData.append('file', file);
  
  // 添加可选参数
  if (options.intervalSeconds) formData.append('intervalSeconds', options.intervalSeconds);
  if (options.startTimeSeconds) formData.append('startTimeSeconds', options.startTimeSeconds);
  if (options.endTimeSeconds) formData.append('endTimeSeconds', options.endTimeSeconds);
  if (options.outputFormat) formData.append('outputFormat', options.outputFormat);
  if (options.imageQuality) formData.append('imageQuality', options.imageQuality);
  
  const response = await fetch('http://localhost:8080/api/video/extract-frames', {
    method: 'POST',
    body: formData
  });
  
  return await response.json();
}

// 显示提取的帧
function displayFrames(frames) {
  frames.forEach(frame => {
    const img = document.createElement('img');
    img.src = `data:image/jpeg;base64,${frame.base64Data}`;
    img.alt = `Frame ${frame.frameNumber} at ${frame.timestampSeconds}s`;
    document.body.appendChild(img);
  });
}
```

### jQuery示例

```javascript
// 使用jQuery上传文件并提取帧
$('#uploadForm').on('submit', function(e) {
  e.preventDefault();
  
  const formData = new FormData();
  formData.append('file', $('#videoFile')[0].files[0]);
  formData.append('intervalSeconds', $('#interval').val());
  
  $.ajax({
    url: 'http://localhost:8080/api/video/extract-frames',
    type: 'POST',
    data: formData,
    processData: false,
    contentType: false,
    success: function(response) {
      if (response.success) {
        displayFrames(response.frames);
      } else {
        alert('错误: ' + response.message);
      }
    },
    error: function(xhr, status, error) {
      alert('请求失败: ' + error);
    }
  });
});
```

---

## 测试工具推荐

1. **Postman**: 图形化API测试工具
2. **curl**: 命令行工具
3. **Insomnia**: 现代化API测试工具
4. **浏览器开发者工具**: 用于调试前端集成

---

## 常见问题

**Q: 为什么上传文件后没有响应？**
A: 检查文件大小是否超过500MB限制，以及文件格式是否支持。

**Q: Base64数据如何在前端显示？**
A: 使用 `data:image/jpeg;base64,${base64Data}` 格式作为img标签的src属性。

**Q: 如何提高处理速度？**
A: 设置合适的intervalSeconds参数，避免提取过多帧；或者限制时间范围。

**Q: 支持哪些视频编码格式？**
A: 支持FFmpeg能处理的所有主流编码格式，包括H.264、H.265、VP9等。