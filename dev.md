# YGOJ - 在线评测系统开发文档

本文档包含 YGOJ 项目的详细技术实现、架构设计和开发指南。

## 目录

- [技术栈](#技术栈)
- [权限系统](#权限系统)
- [Docker 部署架构](#docker-部署架构)
- [配置管理](#配置管理)
- [服务端口说明](#服务端口说明)
- [故障排查](#故障排查)
- [调试技巧](#调试技巧)

---

## 技术栈

### 后端框架
- **Spring Cloud** - 微服务框架
- **Nacos 2.4.3** - 服务注册与配置中心
- **Sentinel 1.8.6** - 流量控制与熔断降级
- **Seata 2.1.0** - 分布式事务
- **Spring Cloud Gateway** - API 网关
- **Apache Shiro** - 权限认证框架
- **JWT** - 无状态认证

### 数据存储
- **MySQL 5.7.40** - 关系型数据库
- **Redis** - 缓存
- **RabbitMQ** - 消息队列

### 前端
- **Vue 3** - 前端框架
- **Vite** - 构建工具

---

## 权限系统

### 概述

YGOJ 采用 **Apache Shiro** 权限框架，基于 JWT + Realm 实现无状态认证和授权，支持：

1. **角色权限（ROLE）**：基于用户角色的粗粒度控制（USER, ADMIN, CONTEST_ADMIN等）
2. **细粒度权限（Permission）**：基于字符串标识的权限控制（problem:create, contest:manage等）
3. **组合逻辑**：支持 AND/OR 逻辑组合多个角色或权限

### 核心组件

- **Shiro 注解**：`@RequiresRoles`, `@RequiresPermissions` 等内置注解
- **JwtRealm**：JWT 认证和授权实现，从 Redis 获取用户信息
- **JwtFilter**：JWT Token 提取和验证过滤器
- **ShiroUtils**：权限检查工具类，提供便捷的静态方法
- **ShiroConfig**：Shiro 核心配置类

### 使用示例

#### 1. 需要登录才能访问
```java
import org.apache.shiro.authz.annotation.RequiresAuthentication;

@RequiresAuthentication
@GetMapping("/user/profile")
public Result getUserProfile() {
    ShiroUser user = ShiroUtils.getCurrentUser();
    return Result.success(user);
}
```

#### 2. 需要指定角色
```java
import org.apache.shiro.authz.annotation.RequiresRoles;

@RequiresRoles("ADMIN")
@PostMapping("/admin/delete-user")
public Result deleteUser(@RequestParam Long userId) {
    // 只有管理员可以删除用户
    return Result.success();
}
```

#### 3. 需要指定权限
```java
import org.apache.shiro.authz.annotation.RequiresPermissions;

@RequiresPermissions("problem:create")
@PostMapping("/problem/create")
public Result createProblem(@RequestBody Problem problem) {
    // 只有拥有 problem:create 权限的用户可以创建题目
    return Result.success();
}
```

#### 4. 多角色检查（OR 逻辑）
```java
@RequiresRoles(value = {"ADMIN", "CONTEST_ADMIN"}, logical = Logical.OR)
@PostMapping("/contest/manage")
public Result manageContest() {
    // ADMIN 或 CONTEST_ADMIN 都可以访问
    return Result.success();
}
```

#### 5. 多权限检查（AND 逻辑）
```java
@RequiresPermissions(value = {"problem:edit", "problem:delete"}, logical = Logical.AND)
@DeleteMapping("/problem/delete/{id}")
public Result deleteProblem(@PathVariable Long id) {
    // 需要同时拥有编辑和删除权限
    return Result.success();
}
```

#### 6. 代码中动态检查权限
```java
import com.ygoj.common.shiro.ShiroUtils;

@GetMapping("/check")
public Result checkPermission() {
    // 检查是否已认证
    if (!ShiroUtils.isAuthenticated()) {
        return Result.error("请先登录");
    }
    
    // 检查角色
    if (!ShiroUtils.hasRole("ADMIN")) {
        return Result.error("需要管理员权限");
    }
    
    // 检查权限
    if (!ShiroUtils.hasPermission("problem:create")) {
        return Result.error("没有创建题目的权限");
    }
    
    // 获取当前用户信息
    ShiroUser user = ShiroUtils.getCurrentUser();
    Long userId = ShiroUtils.getCurrentUserId();
    String username = ShiroUtils.getCurrentUsername();
    
    return Result.success(user);
}
```

### 权限映射说明

**角色列表：**
- `USER` - 普通用户
- `ADMIN` - 系统管理员（拥有所有角色和权限）
- `CONTEST_ADMIN` - 比赛管理员
- `PROBLEM_ADMIN` - 题目管理员

**权限列表（由位运算自动转换）：**

| 权限字符串 | 说明 | 对应位 |
|-----------|------|--------|
| problem:view | 查看题目 | 0 |
| problem:submit | 提交代码 | 1 |
| problem:create | 创建题目 | 2 |
| problem:edit | 编辑题目 | 3 |
| problem:delete | 删除题目 | 4 |
| solution:view | 查看题解 | 5 |
| solution:create | 发布题解 | 6 |
| record:view | 查看提交记录 | 7 |
| ranking:view | 查看排行榜 | 8 |
| contest:create | 创建比赛 | 9 |
| contest:manage | 管理比赛 | 10 |
| contest:join | 参加比赛 | 11 |
| problemset:create | 创建题集 | 12 |
| problemset:manage | 管理题集 | 13 |
| problemset:view | 查看题集 | 14 |
| user:manage | 用户管理 | 15 |
| system:config | 系统设置 | 16 |

**角色继承关系：**
- `ADMIN` → 拥有 `USER`, `CONTEST_ADMIN`, `PROBLEM_ADMIN` 所有角色
- `CONTEST_ADMIN` → 拥有 `USER` 角色
- `PROBLEM_ADMIN` → 拥有 `USER` 角色

### 数据库配置

用户信息表（userinfo）包含以下权限字段：

```sql
-- 用户角色（默认 USER）
role VARCHAR(50) DEFAULT 'USER'

-- 位运算权限值（会自动转换为权限字符串）
permission BIGINT DEFAULT 3
```

**权限值说明：**
- 数据库中存储位运算值（如 3 = 第0位和第1位）
- Shiro Realm 会自动将其转换为权限字符串（如 "problem:view", "problem:submit"）
- 前端和后端代码中使用权限字符串进行判断

**初始管理员账号：**

系统初始化时会自动创建一个默认管理员账号：

- **用户名**: `admin`
- **密码**: `Admin@123456`
- **角色**: `ADMIN`
- **权限值**: `65535` (拥有所有权限)
- **邮箱**: `admin@ygoj.com`

使用该账号登录后，可以访问「系统管理」页面管理其他用户的权限。

**设置用户权限示例：**
```sql
-- 设置普通用户（查看题目 + 提交代码）
UPDATE userinfo SET role = 'USER', permission = 3 WHERE id = ?;

-- 设置题目管理员（题目相关所有权限）
UPDATE userinfo SET role = 'PROBLEM_ADMIN', permission = 63 WHERE id = ?;

-- 设置超级管理员（所有权限）
UPDATE userinfo SET role = 'ADMIN', permission = 131071 WHERE id = ?;
```

> **注意**：修改用户权限后，用户需要重新登录才能生效。

### JWT Token 结构

登录成功后，JWT payload 包含：
```json
{
  "username": "testuser",
  "userId": 1,
  "email": "test@example.com",
  "role": "USER",
  "permission": 3
}
```

### 前端调用

```javascript
// 登录获取 token
const response = await axios.post('/api/user/login', {
  loginStr: 'username',
  password: 'password'
});
const token = response.data.data;

// 后续请求携带 token
axios.get('/api/problem/1', {
  headers: {
    'Authorization': 'Bearer ' + token  // 推荐格式
  }
});
```

### 错误响应

- **401 未授权**：未登录或 Token 无效
- **403 权限不足**：已登录但权限不够

```json
{
  "code": 403,
  "message": "权限不足，无法访问该资源"
}
```

### Shiro 内置注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@RequiresAuthentication` | 需要认证 | 登录用户可访问 |
| `@RequiresRoles("ADMIN")` | 需要指定角色 | 仅管理员可访问 |
| `@RequiresPermissions("problem:create")` | 需要指定权限 | 有创建题目权限可访问 |
| `@RequiresUser` | 需要用户（记住我或认证） | 登录或记住我可访问 |
| `@RequiresGuest` | 需要游客（未认证） | 仅未登录用户可访问 |

### 注意事项

1. **新用户注册后需要在数据库中手动设置 role 和 permission**
2. **修改用户权限后，用户需要重新登录才能生效**
3. Token 默认有效期为 7 天
4. 推荐使用 `Authorization: Bearer <token>` 格式传递 Token
5. 公开接口（如登录、注册、题目列表）在 `ShiroConfig` 中配置为 `anon`
6. Shiro 会自动处理权限验证失败，返回统一的错误响应
7. 所有服务的 WebConfig 中原有拦截器已禁用，由 Shiro 统一接管权限控制

---

## Docker 部署架构

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

### 部署脚本使用说明

**Linux 系统：**

1. **赋予执行权限**
```bash
chmod +x deploy.sh
```

2. **运行部署脚本**
```bash
# 方式一：交互式菜单
sudo ./deploy.sh

# 方式二：直接执行选项
sudo ./deploy.sh 1  # 部署
sudo ./deploy.sh 2  # Docker换源
sudo ./deploy.sh 3  # 更新并部署
```

**Windows 系统：**

双击运行 `deploy.bat`，根据提示选择操作。

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

---

## 配置管理

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

---

## 服务端口说明

### 对外暴露端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 3000 | Vue 前端应用 |
| Gateway | 80 | API 网关（可通过 GATEWAY_PORT 环境变量修改） |

### 内部服务端口（不暴露到宿主机）

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

---

## 常用命令

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

---

## 故障排查

### 1. 服务启动失败
```bash
# 查看具体服务日志
docker-compose logs [service-name]
```

### 2. 数据库连接失败
- 检查 MySQL 是否启动成功
- 确认数据库已正确初始化
- 验证环境变量配置：`docker exec ygoj-service-user env | grep MYSQL`

### 3. Nacos 注册失败
- 等待 Nacos 完全启动（约 30-60 秒）
- 检查网络连接: `docker network inspect ygoj_ygoj-network`

### 4. 端口冲突
- 修改 `.env` 中的 `GATEWAY_PORT` 配置
- 或修改 `docker-compose.yml` 中的前端端口映射
- 重新启动：`docker-compose down && docker-compose up -d`

### 5. 配置未生效
- 检查 `.env` 文件是否存在且格式正确
- 验证环境变量：`docker exec ygoj-gateway env | grep NACOS`
- 重新构建：`docker-compose up -d --build`

### 6. 测试容器间连通性
```bash
docker exec ygoj-gateway ping mysql
docker exec ygoj-service-user ping redis
```

---

## 调试技巧

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

---

## 注意事项

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
