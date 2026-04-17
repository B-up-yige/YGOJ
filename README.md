# YGOJ - 在线评测系统

一个基于 Spring Cloud 微服务架构的在线编程评测系统。

## 🚀 快速开始

### 前置要求

- **Linux**: Ubuntu 20.04+ / CentOS 7+ / Debian 10+
- **Windows**: Docker Desktop 20.10+
- 部署脚本会自动安装所需依赖（Docker、Maven、JDK）

### 一键部署

**Linux:**
```bash
chmod +x deploy.sh
sudo ./deploy.sh 1  # 直接部署
```

**Windows:**
双击运行 `deploy.bat`，选择选项 1 进行部署。

### 访问地址

部署完成后，访问以下地址：

- 🌐 **前端页面**: http://localhost:3000
- 🔗 **API 网关**: http://localhost:80
- ⚙️ **Nacos 控制台**: http://localhost:8848/nacos (nacos/nacos)
- 📨 **RabbitMQ 管理**: http://localhost:15672 (root/root)

### 默认管理员账号

- **用户名**: `admin`
- **密码**: `Admin@123456`

---

## 📖 文档

详细的技术文档请查看 [dev.md](dev.md)，包含：

- 权限系统设计与使用
- Docker 部署架构详解
- 配置管理说明
- 故障排查指南
- 调试技巧

---

## 🛠️ 技术栈

- **后端**: Spring Cloud, Nacos, Sentinel, Seata, Apache Shiro
- **数据库**: MySQL 5.7, Redis
- **消息队列**: RabbitMQ
- **前端**: Vue 3, Vite
- **部署**: Docker, Docker Compose

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

1. 确保 **80** 和 **3000** 端口未被占用
2. 首次部署需要下载依赖和构建镜像，请耐心等待
3. 修改用户权限后需要重新登录才能生效
4. 判题服务需要访问 Docker API，已自动挂载 `/var/run/docker.sock`

---

## 📄 License

本项目仅用于学习和研究目的。
