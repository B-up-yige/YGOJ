基础技术栈
- spring cloud
- nacos 2.4.3
- sentinel 1.8.6
- seata 2.1.0
- gateway

数据库: mysql5.7.40

## Docker 部署

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
   - 选项 1: 完整部署所有服务
   - 选项 2: 仅启动基础设施（MySQL, Redis, RabbitMQ, Nacos）
   - 选项 3: 仅启动应用服务
   - 选项 4: 停止所有服务
   - 选项 5: 查看服务状态
   - 选项 6: 查看日志
   - 选项 7: Docker 换源配置（支持阿里云/腾讯云/网易云）
   - 选项 8: 清理无用镜像和容器

### 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3000 | Vue 前端应用 |
| Gateway | 80 | API 网关 |
| Nacos | 8848 | 服务注册与配置中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RabbitMQ | 5672/15672 | 消息队列（管理界面） |
| Service User | 8001 | 用户服务 |
| Service Problem | 8000 | 题目服务 |
| Service Record | 8002 | 提交记录服务 |
| Service Judger | 8003 | 判题服务 |
| File System | 9201 | 文件服务 |

### 常用命令

```powershell
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

### 注意事项

1. 首次部署时，数据库会自动初始化
2. 文件数据持久化在 Docker volume `file-data` 中
3. 确保 80、3000、3306、6379、5672、8848 等端口未被占用
4. Nacos 默认账号密码: nacos/nacos
5. RabbitMQ 默认账号密码: root/root
6. MySQL root 密码: 123456
7. **判题服务需要访问 Docker API**：已自动挂载宿主机的 `/var/run/docker.sock`
8. Windows 用户需安装 Docker Desktop 并启用 WSL 2 后端
9. 脚本支持自动检测和引导安装 Docker/Docker Compose
10. 内置 Docker 镜像源配置功能，加速镜像下载

### 故障排查

1. **服务启动失败**
   ```powershell
   # 查看具体服务日志
   docker-compose logs [service-name]
   ```

2. **数据库连接失败**
   - 检查 MySQL 是否启动成功
   - 确认数据库已正确初始化

3. **Nacos 注册失败**
   - 等待 Nacos 完全启动（约 30-60 秒）
   - 检查网络连接: `docker network inspect ygoj_ygoj-network`

4. **端口冲突**
   - 修改 `docker-compose.yml` 中的端口映射
   - 或停止占用端口的其他服务
