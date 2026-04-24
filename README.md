# YGOJ - 在线评测系统

一个基于 Spring Cloud 微服务架构的在线编程评测系统。

## 🚀 快速开始

### 前置要求

- **Linux**: Ubuntu 20.04+ / CentOS 7+ / Debian 10+
- 部署脚本会自动安装所需依赖（Docker、Maven、JDK）

### 一键部署

**Linux:**
```bash
chmod +x deploy.sh
sudo ./deploy.sh 1  # 直接部署
```

### 访问地址

部署完成后，访问以下地址：

- 🌐 **前端页面**: http://localhost:3000
- ⚙️ **Nacos 控制台**: http://localhost:8848/nacos (nacos/nacos)

### 默认超级管理员账号

- **用户名**: `admin`
- **密码**: `Admin@123456`

---

## 📖 文档

详细的技术文档请查看 [dev.md](dev.md)，包含：

- 项目架构与服务说明
- 权限系统设计与使用
- 讨论区功能说明
- Docker 部署架构详解
- 配置管理说明
- 故障排查指南
- 调试技巧

---

## 🛠️ 技术栈

### 后端
- **Spring Boot**: 3.3.4
- **Spring Cloud**: 2023.0.3
- **Spring Cloud Alibaba**: 2023.0.3.2
- **Spring Security**: JWT + RBAC 权限控制
- **Nacos 2.4.3**: 服务注册与配置中心
- **Sentinel 1.8.6**: 流量控制与熔断降级
- **Seata 2.1.0**: 分布式事务
- **MyBatis Plus**: ORM 框架
- **Docker Java 3.3.0**: 容器化判题

### 数据存储
- **MySQL 5.7.40**: 关系型数据库
- **Redis**: 缓存与 Token 存储
- **RabbitMQ 3.x**: 消息队列（判题任务）

### 前端
- **Vue 3.5.29**: 前端框架
- **Vite 7.3.1**: 构建工具
- **Element Plus 2.13.5**: UI 组件库
- **Pinia 3.0.4**: 状态管理
- **Vue Router 5.0.3**: 路由管理
- **ECharts 6.0.0**: 数据可视化
- **Markdown-it 14.1.1**: Markdown 渲染

### 部署
- **Docker & Docker Compose**: 容器化部署
- **Nginx**: 前端静态资源服务器

---

## 📝 常用命令

```bash
# 部署（先停止所有容器）
sudo ./deploy.sh 1

# Docker 换源
sudo ./deploy.sh 2

# 更新并部署（git pull + 部署）
sudo ./deploy.sh 3

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [service-name]

# 停止所有服务
docker-compose down
```

---

## ⚠️ 注意事项

1. 确保 **8848** 和 **3000** 端口未被占用
2. 首次部署需要下载依赖和构建镜像，请耐心等待（约 5-10 分钟）
3. 修改用户权限后需要重新登录才能生效
4. 判题服务需要访问 Docker API，已自动挂载 `/var/run/docker.sock`
5. 系统默认管理员账号：`admin` / `Admin@123456`
6. 新用户注册默认拥有基础权限（查看题目、提交代码、查看记录等）