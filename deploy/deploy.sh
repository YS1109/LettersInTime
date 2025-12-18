#!/bin/bash
#
# LettersInTime 自动部署脚本
# 用途：自动拉取最新代码并重启容器
#
# 使用方法：
#   chmod +x deploy.sh
#   ./deploy.sh
#

set -e

# 切换到项目根目录
cd "$(dirname "$0")/.."

echo "=========================================="
echo "  LettersInTime 自动部署"
echo "=========================================="
echo ""

# 检查是否在 Git 仓库中
if [ ! -d ".git" ]; then
    echo "❌ 错误: 当前目录不是 Git 仓库"
    echo "请确保在项目根目录运行此脚本"
    exit 1
fi

# 检查是否有未提交的更改
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️  警告: 检测到未提交的更改"
    git status --short
    echo ""
    read -p "是否继续拉取更新？未提交的更改可能被覆盖 (y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "已取消"
        exit 0
    fi
fi

# 检查 Docker
echo "🔍 检查环境..."
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: 未安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ 错误: 未安装 Docker Compose"
    exit 1
fi

echo "✅ 环境检查通过"
echo ""

# 显示当前版本
echo "📌 当前版本:"
git log -1 --oneline
echo ""

# 拉取最新代码
echo "📥 拉取最新代码..."
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "当前分支: $CURRENT_BRANCH"
echo ""

git fetch origin
LOCAL=$(git rev-parse @)
REMOTE=$(git rev-parse @{u})

if [ $LOCAL = $REMOTE ]; then
    echo "✅ 已是最新版本，无需更新"
    echo ""
    read -p "是否强制重启容器？(y/N) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "操作完成"
        exit 0
    fi
else
    echo "发现新版本，开始更新..."
    git pull origin $CURRENT_BRANCH
    echo ""
    echo "✅ 代码更新完成"
    echo ""
    echo "📝 更新内容:"
    git log $LOCAL..$REMOTE --oneline
    echo ""
fi

# 询问是否继续
read -p "是否重启容器以应用更新？(Y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Nn]$ ]]; then
    echo "已取消容器重启"
    exit 0
fi

# 检查配置文件
if [ ! -f "deploy/docker-compose.yml" ]; then
    echo "❌ 错误: 未找到 deploy/docker-compose.yml"
    exit 1
fi

if [ ! -f ".env" ]; then
    echo "⚠️  警告: 未找到 .env 文件"
    if [ -f "env.example" ]; then
        echo "是否从 env.example 创建 .env？"
        read -p "(Y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Nn]$ ]]; then
            cp env.example .env
            echo "✅ 已创建 .env 文件"
            echo "⚠️  请编辑 .env 文件填入正确配置后重新运行"
            exit 1
        fi
    fi
fi

# 停止现有容器
echo ""
echo "🛑 停止现有容器..."
docker-compose -f deploy/docker-compose.yml down

# 重新构建并启动
echo ""
echo "🚀 重新构建并启动容器..."
echo "   代码将在容器内重新编译，需要 2-5 分钟..."
echo ""

docker-compose -f deploy/docker-compose.yml up -d --build

# 等待启动
echo ""
echo "⏳ 等待服务启动..."
sleep 15

# 检查服务状态
echo ""
echo "📊 服务状态:"
docker-compose -f deploy/docker-compose.yml ps

# 显示应用日志
echo ""
echo "📝 应用启动日志:"
docker-compose -f deploy/docker-compose.yml logs --tail=30 app

echo ""
echo "=========================================="
echo "  ✅ 部署完成！"
echo "=========================================="
echo ""
echo "📌 当前版本:"
git log -1 --oneline
echo ""
echo "常用命令:"
echo "  - 查看实时日志: docker-compose -f deploy/docker-compose.yml logs -f app"
echo "  - 重启服务: docker-compose -f deploy/docker-compose.yml restart"
echo "  - 停止服务: docker-compose -f deploy/docker-compose.yml down"
echo ""
echo "测试 API:"
echo '  curl -X POST http://localhost:8080/api/scheduledEmails/create \'
echo '    -H "Content-Type: application/json" \'
echo '    -d {"to":"test@example.com","subject":"测试","content":"内容","scheduledTime":"2025-12-31T10:00:00"}'"'"
echo ""
