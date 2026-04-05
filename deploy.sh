#!/bin/sh

# 设置颜色（如果支持）
if command -v tput >/dev/null 2>&1; then
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    BLUE='\033[0;34m'
    NC='\033[0m'
else
    RED=''
    GREEN=''
    YELLOW=''
    BLUE=''
    NC=''
fi

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  YGOJ Docker 部署脚本${NC}"
echo -e "${BLUE}=========================================${NC}"
echo ""

# 检查是否为 root 用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        echo -e "${YELLOW}[警告] 建议使用 root 权限运行此脚本${NC}"
        echo -e "${YELLOW}[提示] 可以使用 sudo bash $0 运行${NC}"
        read -p "是否继续? (y/n): " continue_choice
        if ! echo "$continue_choice" | grep -qi '^y$'; then
            exit 1
        fi
    fi
}

# 检测操作系统
detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
    elif type lsb_release >/dev/null 2>&1; then
        OS=$(lsb_release -si)
        VER=$(lsb_release -sr)
    else
        OS=$(uname -s)
        VER=$(uname -r)
    fi
    echo -e "${GREEN}[√] 检测到系统: $OS $VER${NC}"
}

# 安装 Docker
install_docker() {
    echo -e "${YELLOW}[提示] 正在安装 Docker...${NC}"
    
    case "$OS" in
        *"Ubuntu"*|*"Debian"*)
            apt-get update
            apt-get install -y apt-transport-https ca-certificates curl software-properties-common
            curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
            add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
            apt-get update
            apt-get install -y docker-ce docker-ce-cli containerd.io
            ;;
        *"CentOS"*|*"Red Hat"*|*"Fedora"*)
            yum install -y yum-utils
            yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
            yum install -y docker-ce docker-ce-cli containerd.io
            systemctl enable docker
            systemctl start docker
            ;;
        *)
            echo -e "${RED}[错误] 不支持的操作系统，请手动安装 Docker${NC}"
            echo -e "${YELLOW}[提示] 访问: https://docs.docker.com/engine/install/${NC}"
            exit 1
            ;;
    esac
    
    # 启动 Docker 服务
    systemctl start docker
    systemctl enable docker
    
    # 添加当前用户到 docker 组
    usermod -aG docker $USER
    
    echo -e "${GREEN}[√] Docker 安装成功${NC}"
}

# 安装 Docker Compose
install_docker_compose() {
    echo -e "${YELLOW}[提示] 正在安装 Docker Compose...${NC}"
    
    COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d\" -f4)
    curl -L "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    
    echo -e "${GREEN}[√] Docker Compose 安装成功${NC}"
}

# 配置 Docker 镜像源
configure_docker_mirror() {
    echo -e "${YELLOW}[提示] Docker 换源配置${NC}"
    echo ""
    echo "请选择镜像源:"
    echo "1. 阿里云镜像加速器"
    echo "2. 腾讯云镜像加速器"
    echo "3. 网易云镜像加速器"
    echo "4. 中科大镜像源"
    echo "5. 恢复默认源"
    echo ""
    read -p "请输入选项 (1-5): " mirror_choice
    
    mkdir -p /etc/docker
    
    case $mirror_choice in
        1)
            echo ""
            echo -e "${YELLOW}[提示] 配置阿里云镜像加速器${NC}"
            echo -e "${YELLOW}[提示] 请访问 https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors 获取专属加速地址${NC}"
            read -p "请输入阿里云加速地址: " aliyun_url
            if [ -n "$aliyun_url" ]; then
                cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["$aliyun_url"]
}
EOF
                echo -e "${GREEN}[√] 阿里云镜像源配置完成${NC}"
            fi
            ;;
        2)
            cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://mirror.ccs.tencentyun.com"]
}
EOF
            echo -e "${GREEN}[√] 腾讯云镜像源配置完成${NC}"
            ;;
        3)
            cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://hub-mirror.c.163.com"]
}
EOF
            echo -e "${GREEN}[√] 网易云镜像源配置完成${NC}"
            ;;
        4)
            cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://docker.mirrors.ustc.edu.cn"]
}
EOF
            echo -e "${GREEN}[√] 中科大镜像源配置完成${NC}"
            ;;
        5)
            rm -f /etc/docker/daemon.json
            echo -e "${GREEN}[√] 已恢复默认源${NC}"
            ;;
        *)
            echo -e "${RED}[错误] 无效选项${NC}"
            return
            ;;
    esac
    
    # 重启 Docker 服务
    echo -e "${YELLOW}[提示] 重启 Docker 服务...${NC}"
    systemctl daemon-reload
    systemctl restart docker
    echo -e "${GREEN}[√] Docker 服务已重启${NC}"
}

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}[!] Docker 未安装${NC}"
        read -p "是否自动安装 Docker? (y/n): " install_docker_choice
        if echo "$install_docker_choice" | grep -qi '^y$'; then
            install_docker
        else
            echo -e "${RED}[错误] 请先安装 Docker${NC}"
            exit 1
        fi
    else
        echo -e "${GREEN}[√] Docker 已安装: $(docker --version)${NC}"
    fi
}

# 检查 Docker Compose 是否安装
check_docker_compose() {
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}[!] Docker Compose 未安装${NC}"
        read -p "是否自动安装 Docker Compose? (y/n): " install_compose_choice
        if echo "$install_compose_choice" | grep -qi '^y$'; then
            install_docker_compose
        else
            echo -e "${RED}[错误] 请先安装 Docker Compose${NC}"
            exit 1
        fi
    else
        echo -e "${GREEN}[√] Docker Compose 已安装: $(docker-compose --version)${NC}"
    fi
}

# 检查 Docker 服务状态
check_docker_service() {
    if ! systemctl is-active --quiet docker; then
        echo -e "${YELLOW}[警告] Docker 服务未运行，正在启动...${NC}"
        systemctl start docker
        systemctl enable docker
    fi
}

# 主菜单
show_menu() {
    echo ""
    echo "请选择部署模式:"
    echo "1. 完整部署（包含所有服务）"
    echo "2. 仅启动基础设施（MySQL, Redis, RabbitMQ, Nacos）"
    echo "3. 仅启动应用服务"
    echo "4. 停止所有服务"
    echo "5. 查看服务状态"
    echo "6. 查看日志"
    echo "7. Docker 换源配置"
    echo "8. 清理无用镜像和容器"
    echo "9. 查看磁盘使用情况"
    echo "0. 退出"
    echo ""
}

# 完整部署
deploy_all() {
    echo -e "${YELLOW}[提示] 开始完整部署...${NC}"
    docker-compose up -d --build
    echo ""
    echo -e "${GREEN}[√] 部署完成！${NC}"
    echo ""
    echo "服务访问地址:"
    echo "  - 前端: http://localhost:3000"
    echo "  - Gateway: http://localhost:80"
    echo "  - Nacos: http://localhost:8848/nacos (nacos/nacos)"
    echo "  - RabbitMQ: http://localhost:15672 (root/root)"
}

# 启动基础设施
deploy_infrastructure() {
    echo -e "${YELLOW}[提示] 启动基础设施服务...${NC}"
    docker-compose up -d mysql redis rabbitmq nacos
    echo -e "${GREEN}[√] 基础设施启动完成！${NC}"
}

# 启动应用服务
deploy_services() {
    echo -e "${YELLOW}[提示] 启动应用服务...${NC}"
    docker-compose up -d gateway service-user service-problem service-record service-judger file-system frontend
    echo -e "${GREEN}[√] 应用服务启动完成！${NC}"
}

# 停止所有服务
stop_services() {
    echo -e "${YELLOW}[提示] 停止所有服务...${NC}"
    docker-compose down
    echo -e "${GREEN}[√] 所有服务已停止${NC}"
}

# 查看服务状态
show_status() {
    echo -e "${YELLOW}[提示] 服务状态:${NC}"
    docker-compose ps
}

# 查看日志
show_logs() {
    read -p "请输入要查看的服务名称 (留空查看所有): " service_name
    if [ -z "$service_name" ]; then
        docker-compose logs -f
    else
        docker-compose logs -f $service_name
    fi
}

# 清理无用资源
cleanup() {
    echo -e "${YELLOW}[提示] 清理无用镜像和容器...${NC}"
    docker system prune -a -f
    echo ""
    echo -e "${GREEN}[√] 清理完成${NC}"
}

# 查看磁盘使用
show_disk_usage() {
    echo -e "${YELLOW}[提示] Docker 磁盘使用情况:${NC}"
    docker system df
}

# 主程序
main() {
    check_root
    detect_os
    check_docker
    check_docker_compose
    check_docker_service
    
    while true; do
        show_menu
        read -p "请输入选项 (0-9): " choice
        
        case $choice in
            1)
                deploy_all
                ;;
            2)
                deploy_infrastructure
                ;;
            3)
                deploy_services
                ;;
            4)
                stop_services
                ;;
            5)
                show_status
                ;;
            6)
                show_logs
                ;;
            7)
                configure_docker_mirror
                ;;
            8)
                cleanup
                ;;
            9)
                show_disk_usage
                ;;
            0)
                echo -e "${GREEN}再见！${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}[错误] 无效选项${NC}"
                ;;
        esac
        
        echo ""
        read -p "按回车键继续..."
    done
}

# 运行主程序
main
