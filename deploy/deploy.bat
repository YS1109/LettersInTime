@echo off
REM LettersInTime 自动部署脚本 (Windows)
REM 用途：自动拉取最新代码并重启容器

setlocal enabledelayedexpansion

REM 切换到项目根目录
cd /d "%~dp0\.."

echo ==========================================
echo   LettersInTime 自动部署 (Windows)
echo ==========================================
echo.

REM 检查是否在 Git 仓库中
if not exist ".git" (
    echo 错误: 当前目录不是 Git 仓库
    echo 请确保在项目根目录运行此脚本
    pause
    exit /b 1
)

REM 检查 Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未安装 Docker
    pause
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未安装 Docker Compose
    pause
    exit /b 1
)

echo ✓ 环境检查通过
echo.

REM 显示当前版本
echo 当前版本:
git log -1 --oneline
echo.

REM 拉取最新代码
echo 拉取最新代码...
for /f "tokens=*" %%i in ('git rev-parse --abbrev-ref HEAD') do set CURRENT_BRANCH=%%i
echo 当前分支: %CURRENT_BRANCH%
echo.

git fetch origin

REM 检查是否有更新
git status -uno | findstr "Your branch is up to date" >nul
if %errorlevel%==0 (
    echo ✓ 已是最新版本，无需更新
    echo.
    set /p confirm="是否强制重启容器？(Y/N): "
    if /i not "!confirm!"=="Y" (
        echo 操作完成
        pause
        exit /b 0
    )
) else (
    echo 发现新版本，开始更新...
    git pull origin %CURRENT_BRANCH%
    echo.
    echo ✓ 代码更新完成
    echo.
)

REM 询问是否继续
set /p confirm="是否重启容器以应用更新？(Y/N): "
if /i not "%confirm%"=="Y" (
    echo 已取消容器重启
    pause
    exit /b 0
)

REM 检查配置文件
if not exist "deploy\docker-compose.yml" (
    echo 错误: 未找到 deploy\docker-compose.yml
    pause
    exit /b 1
)

if not exist ".env" (
    echo 警告: 未找到 .env 文件
    if exist "env.example" (
        set /p create_env="是否从 env.example 创建 .env？(Y/N): "
        if /i "!create_env!"=="Y" (
            copy env.example .env >nul
            echo ✓ 已创建 .env 文件
            echo ⚠ 请编辑 .env 文件填入正确配置后重新运行
            pause
            exit /b 1
        )
    )
)

REM 停止现有容器
echo.
echo 停止现有容器...
docker-compose -f deploy\docker-compose.yml down

REM 重新构建并启动
echo.
echo 重新构建并启动容器...
echo 代码将在容器内重新编译，需要 2-5 分钟...
echo.

docker-compose -f deploy\docker-compose.yml up -d --build

REM 等待启动
echo.
echo 等待服务启动...
timeout /t 15 /nobreak >nul

REM 检查服务状态
echo.
echo 服务状态:
docker-compose -f deploy\docker-compose.yml ps

REM 显示应用日志
echo.
echo 应用启动日志:
docker-compose -f deploy\docker-compose.yml logs --tail=30 app

echo.
echo ==========================================
echo   部署完成！
echo ==========================================
echo.
echo 当前版本:
git log -1 --oneline
echo.
echo 常用命令:
echo   - 查看实时日志: docker-compose -f deploy\docker-compose.yml logs -f app
echo   - 重启服务: docker-compose -f deploy\docker-compose.yml restart
echo   - 停止服务: docker-compose -f deploy\docker-compose.yml down
echo.
echo 测试 API:
echo   curl -X POST http://localhost:8080/api/scheduledEmails/create ^
echo     -H "Content-Type: application/json" ^
echo     -d "{\"to\":\"test@example.com\",\"subject\":\"测试\",\"content\":\"内容\",\"scheduledTime\":\"2025-12-31T10:00:00\"}"
echo.
pause

