基础技术栈
- spring cloud
- nacos 2.4.3
- sentinel 1.8.6
- seata 2.1.0
- gateway

数据库: mysql5.7.40

## Docker 部署

### 架构说明

本项目采用 Docker Bridge 网络实现服务间通信，**仅暴露 Gateway 和前端端口**到宿主机，其他服务通过内部网络通信。

```
外部访问 → Frontend(3000) → Gateway(80) → 微服务内部网络
                                    ↓
                            ┌─────────────────┐
                            │  ygoj-network   │
                            │  (Docker Bridge)│
                            ├─────────────────┤
                            │ • service-user  │
                            │ • service-problem│
                            │ • service-record│
                            │ • service-judger│
                            │ • file-system   │
                            ├─────────────────┤
                            │ • mysql         │
                            │ • redis         │
                            │ • rabbitmq      │
                            │ • nacos         │
                            │ • seata-server  │
                            │ • sentinel      │
                            └─────────────────┘
```

### 前置要求

**Linux:**
- Ubuntu 20.04+ / CentOS 7+ / Debian 10+
- 脚本会自动安装 Docker 和 Docker Compose

**Windows:**
- Docker Desktop 20.10+
- Docker Compose 2.0+

### 快速开始

**Linux 系统：**

1. **赋予执行权限**
```bash
chmod +x deploy.sh
```

2. **运行部署脚本**
```bash
# 方式一：直接运行
sudo ./deploy.sh

# 方式二：使用 sh 运行
sudo sh deploy.sh
```

**Windows 系统：**

1. **双击运行部署脚本**
```
deploy.bat
```

2. **选择部署模式**
   - 选项 1: 完整部署所有服务（自动构建 + 部署）
   - 选项 2: 仅启动基础设施（MySQL, Redis, RabbitMQ, Nacos）
   - 选项 3: 仅启动应用服务（自动构建 + 部署）
   - 选项 4: Maven 构建项目
   - 选项 5: 停止所有服务
   - 选项 6: 查看服务状态
   - 选项 7: 查看日志
   - 选项 8: Docker 换源配置（支持阿里云/腾讯云/网易云）
   - 选项 9: 清理无用镜像和容器
   - 选项 10: 查看磁盘使用情况

**直接使用 Docker Compose：**
```bash
# 首次部署（构建镜像）
docker-compose up -d --build

# 后续启动
docker-compose up -d
```

如需自定义配置（如修改密码），可创建 `.env` 文件：
```env
MYSQL_ROOT_PASSWORD=your_password
RABBITMQ_PASSWORD=your_password
GATEWAY_PORT=8080
```

### 服务端口

#### 对外暴露端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3000 | Vue 前端应用 |
| Gateway | 80 | API 网关（可通过 GATEWAY_PORT 环境变量修改） |

#### 内部服务端口（不暴露到宿主机）

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8848 | 服务注册与配置中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672/15672 | 消息队列（管理界面） |
| Service User | 8001 | 用户服务 |
| Service Problem | 8000 | 题目服务 |
| Service Record | 8002 | 提交记录服务 |
| Service Judger | 8003 | 判题服务 |
| File System | 9201 | 文件服务 |
| Seata | 8091 | 分布式事务 |
| Sentinel | 8858 | 流量控制 |

> **安全提示**：数据库和中间件服务仅在 Docker 内部网络可访问，外部无法直接连接，提升安全性。

### 常用命令

```bash
# 完整部署
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f [service-name]

# 重启服务
docker-compose restart [service-name]

# 停止所有服务
docker-compose down

# 停止并清理数据卷（谨慎使用）
docker-compose down -v
```

### 配置方式

本项目通过 **JVM 启动参数**（`-D` 参数）在 Docker 启动时动态注入配置，application.yaml 中保留默认配置。

**优势：**
- ✅ 配置与代码分离，无需修改 yaml 文件
- ✅ 通过 docker-compose.yml 集中管理所有配置
- ✅ 支持运行时覆盖，灵活性强
- ✅ 符合 12-Factor App 原则

**配置示例：**
```yaml
# docker-compose.yml 中的配置方式
environment:
  SPRING_OPTS: >-
    -Dspring.cloud.nacos.server-addr=nacos:8848
    -Dspring.datasource.url=jdbc:mysql://mysql:3306/user
    -Dspring.datasource.username=root
    -Dspring.datasource.password=${MYSQL_ROOT_PASSWORD:-123456}
```

### 环境变量配置

虽然配置通过 JVM 参数传递，但仍可通过 `.env` 文件管理敏感信息：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | 123456 |
| `RABBITMQ_USERNAME` | RabbitMQ 用户名 | root |
| `RABBITMQ_PASSWORD` | RabbitMQ 密码 | root |
| `GATEWAY_PORT` | Gateway 外部端口 | 80 |

> **提示**：如需自定义配置，可在项目根目录创建 `.env` 文件（不会被提交到版本控制）。

### 注意事项

1. **脚本会自动安装 Maven 3.9.6 并执行项目构建**
2. 数据库会自动初始化
3. 文件数据持久化在 Docker volume `file-data` 中
4. **仅需确保 80 和 3000 端口未被占用**（其他服务不暴露到宿主机）
5. Nacos 默认账号密码: nacos/nacos
6. RabbitMQ 默认账号密码: root/root
7. MySQL root 密码: 123456（可通过 `.env` 文件修改）
8. **判题服务需要访问 Docker API**：已自动挂载宿主机的 `/var/run/docker.sock`
9. Windows 用户需安装 Docker Desktop 并启用 WSL 2 后端
10. 脚本支持自动检测和引导安装 Docker/Docker Compose/Maven
11. 内置 Docker 镜像源配置功能，加速镜像下载
12. **Docker 镜像使用本地 JAR 包构建，提升构建速度**
13. **所有配置通过 JVM 参数（-D）注入**，不使用环境变量占位符
14. **内部服务通过 Docker Bridge 网络通信**，使用容器名作为主机名

### 故障排查

1. **服务启动失败**
   ```bash
   # 查看具体服务日志
   docker-compose logs [service-name]
   ```

2. **数据库连接失败**
   - 检查 MySQL 是否启动成功
   - 确认数据库已正确初始化
   - 验证环境变量配置：`docker exec ygoj-service-user env | grep MYSQL`

3. **Nacos 注册失败**
   - 等待 Nacos 完全启动（约 30-60 秒）
   - 检查网络连接: `docker network inspect ygoj_ygoj-network`

4. **端口冲突**
   - 修改 `.env` 中的 `GATEWAY_PORT` 配置
   - 或修改 `docker-compose.yml` 中的前端端口映射
   - 重新启动：`docker-compose down && docker-compose up -d`

5. **配置未生效**
   - 检查 `.env` 文件是否存在且格式正确
   - 验证环境变量：`docker exec ygoj-gateway env | grep NACOS`
   - 重新构建：`docker-compose up -d --build`

6. **测试容器间连通性**
   ```bash
   docker exec ygoj-gateway ping mysql
   docker exec ygoj-service-user ping redis
   ```

### 调试技巧

```bash
# 进入 MySQL 容器
docker exec -it ygoj-mysql mysql -uroot -p

# 进入 Redis 容器
docker exec -it ygoj-redis redis-cli

# 查看容器环境变量
docker exec ygoj-service-user env | grep MYSQL

# 临时暴露内部服务端口（调试用）
docker run --rm -it --network ygoj_ygoj-network nicolaka/netshoot ping mysql
```
