# 视频帧提取器 Vue3 前端

基于 Vue 3 + Vite + Element Plus 开发的视频帧提取器前端应用。

## 🚀 快速开始

### 环境要求

- Node.js 16+ 
- npm 或 yarn

### 安装依赖

```bash
# 使用 npm
npm install

# 或使用 yarn
yarn install
```

### 开发模式

```bash
# 启动开发服务器（端口5221）
npm run dev

# 或
yarn dev
```

访问 http://localhost:5221

### 生产构建

```bash
# 构建生产版本
npm run build

# 或
yarn build
```

构建文件将输出到 `dist` 目录。

### 预览生产版本

```bash
# 预览构建结果
npm run preview

# 或在指定端口预览
npm run serve
```

## 📁 项目结构

```
Vue3Front/
├── public/                 # 静态资源
├── src/
│   ├── App.vue            # 主应用组件
│   ├── main.js            # 应用入口
│   └── style.css          # 全局样式
├── index.html             # HTML模板
├── package.json           # 项目配置
├── vite.config.js         # Vite配置
└── README.md              # 项目说明
```

## 🛠️ 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - Vue 3 UI组件库
- **Axios** - HTTP客户端

## ⚙️ 配置说明

### API地址配置

在 `src/App.vue` 中修改API基础地址：

```javascript
const API_BASE_URL = 'http://159.75.236.29:8080/api'
```

### 端口配置

在 `vite.config.js` 中修改开发服务器端口：

```javascript
server: {
  port: 5221,
  host: '0.0.0.0'
}
```

## 🌐 部署方式

### 方式1: 静态文件部署

1. 构建项目：
   ```bash
   npm run build
   ```

2. 将 `dist` 目录中的文件上传到Web服务器

### 方式2: 宝塔面板部署

1. 构建项目
2. 在宝塔面板中创建网站，端口5221
3. 将 `dist` 目录内容上传到网站根目录
4. 配置Nginx支持SPA路由

### 方式3: Docker部署

创建 `Dockerfile`：

```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

构建和运行：

```bash
docker build -t video-frontend .
docker run -d -p 5221:80 video-frontend
```

## 🔧 开发说明

### 主要功能模块

1. **服务状态检查** - 检查后端API服务状态
2. **支持格式查询** - 获取支持的视频和图片格式
3. **视频信息获取** - 分析视频文件基本信息
4. **视频帧提取** - 提取视频帧并返回详细信息
5. **Base64专用提取** - 只返回Base64编码数组
6. **JSON格式生成** - 生成特定格式的JSON数据
7. **操作日志** - 记录所有操作和错误信息

### 组件特性

- 响应式设计，支持移动端
- 实时操作日志
- 文件上传进度显示
- 错误处理和用户提示
- Base64数据复制功能
- JSON格式化显示

### API集成

所有API请求都通过 `apiRequest` 函数处理，包含：
- 统一的错误处理
- 请求超时设置（5分钟）
- 响应数据格式化

## 🐛 故障排查

### 常见问题

1. **依赖安装失败**
   ```bash
   # 清除缓存重新安装
   npm cache clean --force
   rm -rf node_modules package-lock.json
   npm install
   ```

2. **开发服务器启动失败**
   - 检查端口5221是否被占用
   - 确认Node.js版本是否符合要求

3. **API请求失败**
   - 检查后端服务是否正常运行
   - 确认API地址配置是否正确
   - 检查CORS配置

4. **构建失败**
   - 检查代码语法错误
   - 确认所有依赖都已正确安装

### 调试技巧

1. 打开浏览器开发者工具查看控制台错误
2. 检查网络请求状态
3. 查看Vue DevTools（需要安装浏览器扩展）

## 📝 更新日志

### v1.0.0
- 初始版本发布
- 支持所有核心功能
- Vue 3 + Element Plus UI
- 响应式设计
- 完整的错误处理

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

MIT License