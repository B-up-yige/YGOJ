@echo off
chcp 65001 >nul
echo =========================================
echo   YGOJ Docker 部署脚本
echo =========================================
echo.

REM 检查 Docker 是否安装
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] Docker 未安装，请先安装 Docker Desktop
    pause
    exit /b 1
)

where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] Docker Compose 未安装
    pause
    exit /b 1
)

echo [√] Docker 环境检查通过
echo.

echo 请选择部署模式:
echo 1. 完整部署（包含所有服务）
echo 2. 仅启动基础设施（MySQL, Redis, RabbitMQ, Nacos）
echo 3. 仅启动应用服务
echo 4. 停止所有服务
echo 5. 查看服务状态
echo 6. 查看日志
echo.
set /p choice="请输入选项 (1-6): "

if "%choice%"=="1" (
    echo.
    echo [提示] 开始完整部署...
    docker-compose up -d --build
    echo.
    echo [√] 部署完成！
    echo.
    echo 服务访问地址:
    echo   - 前端: http://localhost:3000
    echo   - Gateway: http://localhost:80
    echo   - Nacos: http://localhost:8848/nacos (nacos/nacos)
    echo   - RabbitMQ: http://localhost:15672 (root/root)
) else if "%choice%"=="2" (
    echo.
    echo [提示] 启动基础设施服务...
    docker-compose up -d mysql redis rabbitmq nacos
    echo.
    echo [√] 基础设施启动完成！
) else if "%choice%"=="3" (
    echo.
    echo [提示] 启动应用服务...
    docker-compose up -d gateway service-user service-problem service-record service-judger file-system frontend
    echo.
    echo [√] 应用服务启动完成！
) else if "%choice%"=="4" (
    echo.
    echo [提示] 停止所有服务...
    docker-compose down
    echo.
    echo [√] 所有服务已停止
) else if "%choice%"=="5" (
    echo.
    echo [提示] 服务状态:
    docker-compose ps
) else if "%choice%"=="6" (
    echo.
    set /p service_name="请输入要查看的服务名称 (留空查看所有): "
    if "%service_name%"=="" (
        docker-compose logs -f
    ) else (
        docker-compose logs -f %service_name%
    )
) else (
    echo.
    echo [错误] 无效选项
    pause
    exit /b 1
)

echo.
echo =========================================
echo   常用命令:
echo =========================================
echo   查看日志: docker-compose logs -f [service-name]
echo   重启服务: docker-compose restart [service-name]
echo   停止服务: docker-compose down
echo   清理数据: docker-compose down -v
echo =========================================
echo.
pause
