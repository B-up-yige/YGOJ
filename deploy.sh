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

printf "${BLUE}=========================================${NC}\n"
printf "${BLUE}  YGOJ Docker 部署脚本${NC}\n"
printf "${BLUE}=========================================${NC}\n"
echo ""

# 检查是否为 root 用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        printf "${YELLOW}[警告] 建议使用 root 权限运行此脚本${NC}\n"
        printf "${YELLOW}[提示] 可以使用 sudo $0 运行${NC}\n"
        read -p "是否继续? (y/n): " continue_choice
        if ! echo "$continue_choice" | grep -qi '^y$'; then
            exit 1
        fi
    fi
}

# 安装基础工具
install_basic_tools() {
    printf "${YELLOW}[提示] 检查并安装基础工具...${NC}\n"
    
    case "$OS" in
        *"Ubuntu"*|*"Debian"*)
            if ! apt-get update -qq > /dev/null 2>&1; then
                printf "${RED}[错误] apt-get update 失败，请检查网络连接${NC}\n"
                exit 1
            fi
            if ! apt-get install -y -qq curl wget tar gzip > /dev/null 2>&1; then
                printf "${RED}[错误] 安装基础工具失败${NC}\n"
                exit 1
            fi
            ;;
        *"CentOS"*|*"Red Hat"*|*"Fedora"*)
            if ! yum install -y -q curl wget tar gzip > /dev/null 2>&1; then
                printf "${RED}[错误] 安装基础工具失败${NC}\n"
                exit 1
            fi
            ;;
    esac
    
    # 验证关键工具
    if ! command -v curl >/dev/null 2>&1; then
        printf "${RED}[错误] curl 安装失败，请手动安装${NC}\n"
        exit 1
    fi
    
    printf "${GREEN}[√] 基础工具检查完成${NC}\n"
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
    printf "${GREEN}[√] 检测到系统: $OS $VER${NC}\n"
}

# 安装固定版本 JDK 17
install_jdk() {
    printf "${YELLOW}[提示] 正在安装 JDK 17...${NC}\n"
    
    case "$OS" in
        *"Ubuntu"*|*"Debian"*)
            if ! apt-get update -qq > /dev/null 2>&1; then
                printf "${RED}[错误] apt-get update 失败${NC}\n"
                exit 1
            fi
            if ! apt-get install -y openjdk-17-jdk > /dev/null 2>&1; then
                printf "${RED}[错误] JDK 17 安装失败${NC}\n"
                exit 1
            fi
            ;;
        *"CentOS"*|*"Red Hat"*|*"Fedora"*)
            if ! yum install -y java-17-openjdk-devel > /dev/null 2>&1; then
                printf "${RED}[错误] JDK 17 安装失败${NC}\n"
                exit 1
            fi
            ;;
        *)
            printf "${RED}[错误] 不支持的操作系统，请手动安装 JDK 17${NC}\n"
            printf "${YELLOW}[提示] 访问: https://adoptium.net/${NC}\n"
            exit 1
            ;;
    esac
    
    # 检测 JAVA_HOME
    if [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
        JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    elif [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
        JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
    elif [ -d "/usr/lib/jvm/default-java" ]; then
        JAVA_HOME="/usr/lib/jvm/default-java"
    else
        JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    fi
    
    export JAVA_HOME
    
    printf "${GREEN}[√] JDK 17 安装成功: $(java -version 2>&1 | head -n 1)${NC}\n"
    printf "${GREEN}[√] JAVA_HOME: $JAVA_HOME${NC}\n"
}

# 检查 Java 是否安装
check_java() {
    JAVA_REQUIRED_VERSION="17"
    
    if ! command -v java >/dev/null 2>&1; then
        printf "${RED}[!] Java 未安装${NC}\n"
        read -p "是否自动安装 JDK ${JAVA_REQUIRED_VERSION}? (y/n): " install_java_choice
        if echo "$install_java_choice" | grep -qi '^y$'; then
            install_jdk
        else
            printf "${RED}[错误] 请先安装 JDK${NC}\n"
            exit 1
        fi
    else
        # 检测 Java 版本
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | grep -oP '\d+' | head -n 1)
        if [ "$JAVA_VERSION" = "$JAVA_REQUIRED_VERSION" ]; then
            printf "${GREEN}[√] Java 版本正确: $(java -version 2>&1 | head -n 1)${NC}\n"
            
            # 设置 JAVA_HOME
            if [ -z "$JAVA_HOME" ]; then
                if [ -d "/usr/lib/jvm/java-17-openjdk-amd64" ]; then
                    JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
                elif [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
                    JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
                elif [ -d "/usr/lib/jvm/default-java" ]; then
                    JAVA_HOME="/usr/lib/jvm/default-java"
                else
                    JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
                fi
                export JAVA_HOME
            fi
            printf "${GREEN}[√] JAVA_HOME: $JAVA_HOME${NC}\n"
        else
            printf "${YELLOW}[警告] Java 版本不匹配 (当前: $JAVA_VERSION, 需要: $JAVA_REQUIRED_VERSION)${NC}\n"
            read -p "是否安装正确的 JDK 版本? (y/n): " reinstall_choice
            if echo "$reinstall_choice" | grep -qi '^y$'; then
                install_jdk
            else
                printf "${RED}[错误] 请使用 JDK ${JAVA_REQUIRED_VERSION}${NC}\n"
                exit 1
            fi
        fi
    fi
}

# 安装固定版本 Maven (3.9.6)
install_maven() {
    printf "${YELLOW}[提示] 正在安装 Maven 3.9.6...${NC}\n"
    
    # 保存当前目录
    ORIGINAL_DIR=$(pwd)
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    
    MAVEN_VERSION="3.9.6"
    MAVEN_HOME="${SCRIPT_DIR}/.maven/apache-maven-${MAVEN_VERSION}"
    MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    DOWNLOAD_FILE="/tmp/apache-maven.tar.gz"
    
    # 下载并安装 Maven
    cd /tmp
    if ! curl -L "${MAVEN_URL}" -o "${DOWNLOAD_FILE}" > /dev/null 2>&1; then
        printf "${RED}[错误] Maven 下载失败，请检查网络连接${NC}\n"
        rm -f "${DOWNLOAD_FILE}"
        cd "$ORIGINAL_DIR"
        exit 1
    fi
    
    mkdir -p "${SCRIPT_DIR}/.maven"
    if ! tar xzf "${DOWNLOAD_FILE}" -C "${SCRIPT_DIR}/.maven" > /dev/null 2>&1; then
        printf "${RED}[错误] Maven 解压失败${NC}\n"
        rm -f "${DOWNLOAD_FILE}"
        cd "$ORIGINAL_DIR"
        exit 1
    fi
    rm -f "${DOWNLOAD_FILE}"
    
    # 返回原目录
    cd "$ORIGINAL_DIR"
    
    # 直接使用绝对路径验证安装
    MAVEN_BIN="${MAVEN_HOME}/bin/mvn"
    if [ ! -x "${MAVEN_BIN}" ]; then
        printf "${RED}[错误] Maven 安装失败${NC}\n"
        exit 1
    fi
    
    printf "${GREEN}[√] Maven 安装成功: $(${MAVEN_BIN} --version | head -n 1)${NC}\n"
    
    # 自动配置 Maven 阿里云镜像源
    configure_maven_mirror
}

# 配置 Maven 阿里云镜像源
configure_maven_mirror() {
    printf "${YELLOW}[提示] 配置 Maven 阿里云镜像源...${NC}\n"
    
    MAVEN_CONF_DIR="${MAVEN_HOME}/conf"
    SETTINGS_FILE="${MAVEN_CONF_DIR}/settings.xml"
    
    # 备份原始配置文件
    if [ -f "${SETTINGS_FILE}" ]; then
        cp "${SETTINGS_FILE}" "${SETTINGS_FILE}.bak"
    fi
    
    # 检测 JAVA_HOME
    JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    if [ ! -d "$JAVA_HOME" ]; then
        # 尝试常见路径
        if [ -d "/usr/lib/jvm/default-java" ]; then
            JAVA_HOME="/usr/lib/jvm/default-java"
        elif [ -d "/usr/java/latest" ]; then
            JAVA_HOME="/usr/java/latest"
        elif ls /usr/lib/jvm/java-* >/dev/null 2>&1; then
            JAVA_HOME=$(ls -d /usr/lib/jvm/java-* | head -n 1)
        else
            printf "${RED}[错误] 未找到 Java 安装路径，请先安装 JDK${NC}\n"
            return 1
        fi
    fi
    
    printf "${GREEN}[√] 检测到 JAVA_HOME: $JAVA_HOME${NC}\n"
    
    # 创建新的 settings.xml 配置阿里云镜像
    cat > "${SETTINGS_FILE}" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
  
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
  
  <profiles>
    <profile>
      <id>aliyun</id>
      <repositories>
        <repository>
          <id>aliyun-central</id>
          <url>https://maven.aliyun.com/repository/central</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
        <repository>
          <id>aliyun-spring</id>
          <url>https://maven.aliyun.com/repository/spring</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>aliyun-plugin</id>
          <url>https://maven.aliyun.com/repository/public</url>
          <releases>
            <enabled>true</enabled>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  
  <activeProfiles>
    <activeProfile>aliyun</activeProfile>
  </activeProfiles>
</settings>
EOF
    
    printf "${GREEN}[√] Maven 阿里云镜像源配置完成${NC}\n"
}

# 检查 Maven 是否安装
check_maven() {
    MAVEN_REQUIRED_VERSION="3.9.6"
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    MAVEN_BIN="${SCRIPT_DIR}/.maven/apache-maven-${MAVEN_REQUIRED_VERSION}/bin/mvn"
    
    if [ ! -x "${MAVEN_BIN}" ]; then
        printf "${RED}[!] Maven 未安装${NC}\n"
        read -p "是否自动安装 Maven ${MAVEN_REQUIRED_VERSION}? (y/n): " install_maven_choice
        if echo "$install_maven_choice" | grep -qi '^y$'; then
            install_maven
        else
            printf "${RED}[错误] 请先安装 Maven${NC}\n"
            exit 1
        fi
    else
        CURRENT_VERSION=$(${MAVEN_BIN} --version | head -n 1 | grep -oP '\d+\.\d+\.\d+')
        if [ "$CURRENT_VERSION" = "$MAVEN_REQUIRED_VERSION" ]; then
            printf "${GREEN}[√] Maven 版本正确: $(${MAVEN_BIN} --version | head -n 1)${NC}\n"
        else
            printf "${YELLOW}[警告] Maven 版本不匹配 (当前: $CURRENT_VERSION, 需要: $MAVEN_REQUIRED_VERSION)${NC}\n"
            read -p "是否安装正确的 Maven 版本? (y/n): " reinstall_choice
            if echo "$reinstall_choice" | grep -qi '^y$'; then
                install_maven
            fi
        fi
    fi
}

# 安装 Docker
install_docker() {
    printf "${YELLOW}[提示] 正在安装 Docker...${NC}\n"
    
    case "$OS" in
        *"Ubuntu"*|*"Debian"*)
            if ! apt-get update -qq > /dev/null 2>&1; then
                printf "${RED}[错误] apt-get update 失败${NC}\n"
                exit 1
            fi
            if ! apt-get install -y -qq apt-transport-https ca-certificates curl software-properties-common > /dev/null 2>&1; then
                printf "${RED}[错误] 安装 Docker 依赖失败${NC}\n"
                exit 1
            fi
            if ! curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add - > /dev/null 2>&1; then
                printf "${RED}[错误] 添加 Docker GPG 密钥失败${NC}\n"
                exit 1
            fi
            add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
            if ! apt-get update -qq > /dev/null 2>&1; then
                printf "${RED}[错误] apt-get update 失败${NC}\n"
                exit 1
            fi
            if ! apt-get install -y docker-ce docker-ce-cli containerd.io > /dev/null 2>&1; then
                printf "${RED}[错误] Docker 安装失败${NC}\n"
                exit 1
            fi
            ;;
        *"CentOS"*|*"Red Hat"*|*"Fedora"*)
            if ! yum install -y -q yum-utils > /dev/null 2>&1; then
                printf "${RED}[错误] 安装 yum-utils 失败${NC}\n"
                exit 1
            fi
            yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
            if ! yum install -y -q docker-ce docker-ce-cli containerd.io > /dev/null 2>&1; then
                printf "${RED}[错误] Docker 安装失败${NC}\n"
                exit 1
            fi
            systemctl enable docker
            systemctl start docker
            ;;
        *)
            printf "${RED}[错误] 不支持的操作系统，请手动安装 Docker${NC}\n"
            printf "${YELLOW}[提示] 访问: https://docs.docker.com/engine/install/${NC}\n"
            exit 1
            ;;
    esac
    
    # 启动 Docker 服务
    if ! systemctl start docker > /dev/null 2>&1; then
        printf "${RED}[错误] Docker 服务启动失败${NC}\n"
        exit 1
    fi
    systemctl enable docker > /dev/null 2>&1
    
    # 添加当前用户到 docker 组
    usermod -aG docker $USER
    
    printf "${GREEN}[√] Docker 安装成功${NC}\n"
}

# 安装 Docker Compose
install_docker_compose() {
    printf "${YELLOW}[提示] 正在安装 Docker Compose...${NC}\n"
    
    COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d\" -f4)
    if [ -z "$COMPOSE_VERSION" ]; then
        printf "${RED}[错误] 获取 Docker Compose 版本失败${NC}\n"
        exit 1
    fi
    
    DOWNLOAD_FILE="/usr/local/bin/docker-compose"
    printf "${YELLOW}[提示] 正在下载 Docker Compose ${COMPOSE_VERSION}...${NC}\n"
#    if ! curl -L "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o "${DOWNLOAD_FILE}"; then
    if ! curl -L "https://gitee.com/yige123_gitee/qxjg/releases/download/preview0.1/docker-compose-linux-x86_64" -o "${DOWNLOAD_FILE}"; then
        printf "${RED}[错误] Docker Compose 下载失败${NC}\n"
        rm -f "${DOWNLOAD_FILE}"
        exit 1
    fi
    
    chmod +x "${DOWNLOAD_FILE}"
    
    if ! command -v docker-compose >/dev/null 2>&1; then
        printf "${RED}[错误] Docker Compose 安装失败${NC}\n"
        rm -f "${DOWNLOAD_FILE}"
        exit 1
    fi
    
    printf "${GREEN}[√] Docker Compose 安装成功${NC}\n"
}

# 配置 Docker 镜像源
configure_docker_mirror() {
    printf "${YELLOW}[提示] Docker 换源配置${NC}\n"
    echo ""
    echo "请选择镜像源:"
    echo "1. 阿里云镜像加速器"
    echo "2. 腾讯云镜像加速器"
    echo "3. 网易云镜像加速器"
    echo "4. 中科大镜像源"
    echo "5. 多源聚合加速（推荐）"
    echo "6. 恢复默认源"
    echo ""
    read -p "请输入选项 (1-6): " mirror_choice
    
    mkdir -p /etc/docker
    
    case $mirror_choice in
        1)
            echo ""
            printf "${YELLOW}[提示] 配置阿里云镜像加速器${NC}\n"
            printf "${YELLOW}[提示] 请访问 https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors 获取专属加速地址${NC}\n"
            read -p "请输入阿里云加速地址: " aliyun_url
            if [ -n "$aliyun_url" ]; then
                cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["$aliyun_url"]
}
EOF
                printf "${GREEN}[√] 阿里云镜像源配置完成${NC}\n"
            fi
            ;;
        2)
            cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://mirror.ccs.tencentyun.com"]
}
EOF
            printf "${GREEN}[√] 腾讯云镜像源配置完成${NC}\n"
            ;;
        3)
            cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://hub-mirror.c.163.com"]
}
EOF
            printf "${GREEN}[√] 网易云镜像源配置完成${NC}\n"
            ;;
        4)
            cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://docker.mirrors.ustc.edu.cn"]
}
EOF
            printf "${GREEN}[√] 中科大镜像源配置完成${NC}\n"
            ;;
        5)
            cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://do.nark.eu.org",
    "https://dc.j8.work",
    "https://docker.m.daocloud.io",
    "https://dockerproxy.com",
    "https://docker.mirrors.ustc.edu.cn",
    "https://docker.nju.edu.cn"
  ]
}
EOF
            printf "${GREEN}[√] 多源聚合加速配置完成（推荐）${NC}\n"
            ;;
        6)
            rm -f /etc/docker/daemon.json
            printf "${GREEN}[√] 已恢复默认源${NC}\n"
            ;;
        *)
            printf "${RED}[错误] 无效选项${NC}\n"
            return
            ;;
    esac
    
    # 重启 Docker 服务
    printf "${YELLOW}[提示] 重启 Docker 服务...${NC}\n"
    systemctl daemon-reload
    systemctl restart docker
    printf "${GREEN}[√] Docker 服务已重启${NC}\n"
}

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker >/dev/null 2>&1; then
        printf "${RED}[!] Docker 未安装${NC}\n"
        read -p "是否自动安装 Docker? (y/n): " install_docker_choice
        if echo "$install_docker_choice" | grep -qi '^y$'; then
            install_docker
        else
            printf "${RED}[错误] 请先安装 Docker${NC}\n"
            exit 1
        fi
    else
        printf "${GREEN}[√] Docker 已安装: $(docker --version)${NC}\n"
    fi
}

# 检查 Docker Compose 是否安装
check_docker_compose() {
    if ! command -v docker-compose >/dev/null 2>&1; then
        printf "${RED}[!] Docker Compose 未安装${NC}\n"
        read -p "是否自动安装 Docker Compose? (y/n): " install_compose_choice
        if echo "$install_compose_choice" | grep -qi '^y$'; then
            install_docker_compose
        else
            printf "${RED}[错误] 请先安装 Docker Compose${NC}\n"
            exit 1
        fi
    else
        printf "${GREEN}[√] Docker Compose 已安装: $(docker-compose --version)${NC}\n"
    fi
}

# 检查 Docker 服务状态
check_docker_service() {
    if ! systemctl is-active --quiet docker; then
        printf "${YELLOW}[警告] Docker 服务未运行，正在启动...${NC}\n"
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
    echo "4. Maven 构建项目"
    echo "5. 停止所有服务"
    echo "6. 查看服务状态"
    echo "7. 查看日志"
    echo "8. Docker 换源配置"
    echo "9. 清理无用镜像和容器"
    echo "10. 查看磁盘使用情况"
    echo "0. 退出"
    echo ""
}

# 构建项目
build_project() {
    printf "${YELLOW}[提示] 开始 Maven 构建...${NC}\n"
    echo ""
    
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    MAVEN_REQUIRED_VERSION="3.9.6"
    MAVEN_BIN="${SCRIPT_DIR}/.maven/apache-maven-${MAVEN_REQUIRED_VERSION}/bin/mvn"
    
    # 确保 Maven 可用
    if [ ! -x "${MAVEN_BIN}" ]; then
        printf "${RED}[错误] Maven 未找到，请重新运行脚本安装 Maven${NC}\n"
        return 1
    fi
    
    # 设置 JAVA_HOME（如果未设置）
    if [ -z "$JAVA_HOME" ]; then
        JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
        export JAVA_HOME
    fi
    
    printf "${GREEN}[√] JAVA_HOME: $JAVA_HOME${NC}\n"
    printf "${GREEN}[√] Maven 版本: $(${MAVEN_BIN} --version | head -n 1)${NC}\n"
    
    # 执行 Maven 构建
    cd "$(dirname "$0")"
    printf "${YELLOW}[提示] 执行: ${MAVEN_BIN} clean package -DskipTests -B${NC}\n"
    echo ""
    
    if ! ${MAVEN_BIN} clean package -DskipTests -B; then
        echo ""
        printf "${RED}[错误] Maven 构建失败，请检查错误信息${NC}\n"
        return 1
    fi
    
    echo ""
    printf "${GREEN}[√] Maven 构建成功${NC}\n"
    echo ""
    printf "${GREEN}生成的 JAR 包:${NC}\n"
    find . -name "*.jar" -path "*/target/*" ! -name "*-original.jar" | while read jar_file; do
        printf "  - $jar_file\n"
    done
}

# 完整部署
deploy_all() {
    printf "${YELLOW}[提示] 开始完整部署...${NC}\n"
    
    # 先构建项目
    build_project
    if [ $? -ne 0 ]; then
        printf "${RED}[错误] 构建失败，终止部署${NC}\n"
        return 1
    fi
    
    # 启动 Docker 服务
    if ! docker-compose up -d --build; then
        printf "${RED}[错误] Docker 服务启动失败${NC}\n"
        return 1
    fi
    
    echo ""
    printf "${GREEN}[√] 部署完成！${NC}\n"
    echo ""
    echo "服务访问地址:"
    echo "  - 前端: http://localhost:3000"
    echo "  - Gateway: http://localhost:80"
    echo "  - Nacos: http://localhost:8848/nacos (nacos/nacos)"
    echo "  - RabbitMQ: http://localhost:15672 (root/root)"
}

# 启动基础设施
deploy_infrastructure() {
    printf "${YELLOW}[提示] 启动基础设施服务...${NC}\n"
    if ! docker-compose up -d mysql redis rabbitmq nacos; then
        printf "${RED}[错误] 基础设施启动失败${NC}\n"
        return 1
    fi
    printf "${GREEN}[√] 基础设施启动完成！${NC}\n"
}

# 启动应用服务
deploy_services() {
    printf "${YELLOW}[提示] 启动应用服务...${NC}\n"
    
    # 先构建项目
    build_project
    if [ $? -ne 0 ]; then
        printf "${RED}[错误] 构建失败，终止部署${NC}\n"
        return 1
    fi
    
    if ! docker-compose up -d gateway service-user service-problem service-record service-judger file-system frontend; then
        printf "${RED}[错误] 应用服务启动失败${NC}\n"
        return 1
    fi
    printf "${GREEN}[√] 应用服务启动完成！${NC}\n"
}

# 停止所有服务
stop_services() {
    printf "${YELLOW}[提示] 停止所有服务...${NC}\n"
    if ! docker-compose down; then
        printf "${RED}[错误] 服务停止失败${NC}\n"
        return 1
    fi
    printf "${GREEN}[√] 所有服务已停止${NC}\n"
}

# 查看服务状态
show_status() {
    printf "${YELLOW}[提示] 服务状态:${NC}\n"
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
    printf "${YELLOW}[提示] 清理无用镜像和容器...${NC}\n"
    if ! docker system prune -a -f; then
        printf "${RED}[错误] 清理失败${NC}\n"
        return 1
    fi
    echo ""
    printf "${GREEN}[√] 清理完成${NC}\n"
}

# 查看磁盘使用
show_disk_usage() {
    printf "${YELLOW}[提示] Docker 磁盘使用情况:${NC}\n"
    docker system df
}

# 主程序
main() {
    check_root
    detect_os
    install_basic_tools
    check_java
    check_maven
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
                build_project
                ;;
            5)
                stop_services
                ;;
            6)
                show_status
                ;;
            7)
                show_logs
                ;;
            8)
                configure_docker_mirror
                ;;
            9)
                cleanup
                ;;
            10)
                show_disk_usage
                ;;
            0)
                printf "${GREEN}再见！${NC}\n"
                exit 0
                ;;
            *)
                printf "${RED}[错误] 无效选项${NC}\n"
                ;;
        esac
        
        echo ""
        printf "按回车键继续..."
        read dummy
    done
}

# 运行主程序
main
