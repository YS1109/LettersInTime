# LettersInTime 部署指南

## 🚀 快速开始

### 1. 克隆项目

```bash
# CentOS/Linux
cd /opt  # 或其他目录
git clone https://github.com/your-username/LettersInTime.git
cd LettersInTime
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp env.example .env

# 编辑配置
vim .env
```

**必填配置项**：

```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=your_secure_password

# 邮件配置（QQ 邮箱使用授权码）
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_mail_auth_code

# 阿里云内容安全配置
ALIYUN_ACCESS_KEY_ID=your_key_id
ALIYUN_ACCESS_KEY_SECRET=your_key_secret
```

### 3. 启动服务

```bash
# 赋予脚本执行权限
chmod +x deploy/deploy.sh

# 启动服务（首次启动需要 3-5 分钟编译）
docker-compose -f deploy/docker-compose.yml up -d --build

# 查看启动日志
docker-compose -f deploy/docker-compose.yml logs -f app
```

---

## 🔄 更新部署

### 一键自动更新（推荐）

```bash
# Linux/Mac
./deploy/deploy.sh

# Windows
deploy\deploy.bat
```

脚本会自动：
1. ✅ 检查并拉取最新代码（git pull）
2. ✅ 重启容器
3. ✅ 容器内重新编译
4. ✅ 启动新版本
5. ✅ 显示部署日志

### 手动更新

```bash
# 1. 拉取最新代码
git pull origin main

# 2. 重启容器
docker-compose -f deploy/docker-compose.yml down
docker-compose -f deploy/docker-compose.yml up -d --build

# 3. 查看日志
docker-compose -f deploy/docker-compose.yml logs -f app
```

---

## 📋 常用命令

### 服务管理

```bash
# 启动服务
docker-compose -f deploy/docker-compose.yml up -d

# 停止服务（数据保留）
docker-compose -f deploy/docker-compose.yml down

# 重启服务
docker-compose -f deploy/docker-compose.yml restart

# 仅重启应用（不重启数据库）
docker-compose -f deploy/docker-compose.yml restart app

# 查看服务状态
docker-compose -f deploy/docker-compose.yml ps
```

### 日志查看

```bash
# 查看所有日志
docker-compose -f deploy/docker-compose.yml logs

# 实时跟踪应用日志
docker-compose -f deploy/docker-compose.yml logs -f app

# 查看最近 100 行日志
docker-compose -f deploy/docker-compose.yml logs --tail=100 app
```

### 版本管理

```bash
# 查看当前版本
git log -1 --oneline

# 查看提交历史
git log --oneline -10

# 回滚到指定版本
git reset --hard <commit-hash>
docker-compose -f deploy/docker-compose.yml restart app

# 查看所有分支
git branch -a

# 切换分支
git checkout <branch-name>
./deploy/deploy.sh
```

### 容器操作

```bash
# 进入应用容器
docker exec -it letters-app bash

# 进入 MySQL 容器
docker exec -it letters-mysql bash

# 连接 MySQL 数据库
docker exec -it letters-mysql mysql -uroot -p

# 查看容器资源使用
docker stats letters-app letters-mysql

# 查看容器内代码
docker exec letters-app ls -la /app
```

---

## 💾 数据管理

### 数据持久化

MySQL 数据存储在 `mysql-data/` 目录：
- ✅ 容器删除后数据不丢失
- ✅ 容器重启后数据自动恢复
- ✅ 可直接备份该目录

### 备份数据库

```bash
# 备份数据目录
tar -czf mysql-backup-$(date +%Y%m%d).tar.gz ./mysql-data

# 或使用 mysqldump
docker exec letters-mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} letters-in-time > backup.sql
```

### 恢复数据库

```bash
# 方式 1：恢复数据目录
docker-compose -f deploy/docker-compose.yml down
tar -xzf mysql-backup-20251218.tar.gz
docker-compose -f deploy/docker-compose.yml up -d

# 方式 2：导入 SQL 文件
docker exec -i letters-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} letters-in-time < backup.sql
```

---

## 🔧 验证部署

### 1. 检查服务状态

```bash
docker-compose -f deploy/docker-compose.yml ps

# 输出应该显示两个服务都是 Up
# NAME             STATUS
# letters-app      Up
# letters-mysql    Up (healthy)
```

### 2. 测试 API

```bash
curl -X POST http://localhost:8080/api/scheduledEmails/create \
  -H "Content-Type: application/json" \
  -d '{
    "to": "test@example.com",
    "subject": "测试邮件",
    "content": "这是一封测试邮件",
    "scheduledTime": "2025-12-31 10:00:00"
  }'

# 成功返回：{"code":0,"message":"成功","data":null}
```

---

## ⚙️ 配置说明

### 配置工作原理

Docker 部署时，配置通过以下流程生效：

```
.env 文件
   ↓
Docker Compose 读取
   ↓
注入为容器环境变量
   ↓
Spring Boot 读取环境变量（最高优先级）
   ↓
应用使用环境变量配置
```

**重要**：`application-prod.yml` 使用 `${ENV_VAR:default}` 格式，**优先读取环境变量**。

详细配置说明：[../CONFIG.md](../CONFIG.md)

### 端口配置

默认端口可在 `.env` 中修改：

```bash
# 应用端口
APP_PORT=8080

# MySQL 端口
MYSQL_PORT=3306
```

### JVM 参数

在 `deploy/Dockerfile` 中修改：

```dockerfile
java -Xms256m -Xmx512m ...
```

### Maven 镜像

加速 Maven 构建，创建 `.m2/settings.xml`：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

挂载到容器（修改 `deploy/docker-compose.yml`）：

```yaml
volumes:
  - ..:/app
  - maven-repo:/root/.m2
  - ../.m2/settings.xml:/root/.m2/settings.xml:ro  # 添加这行
```

---

## 🛠️ 故障排查

### 应用启动失败

```bash
# 1. 查看应用日志
docker-compose -f deploy/docker-compose.yml logs app

# 2. 检查 MySQL 是否就绪
docker-compose -f deploy/docker-compose.yml ps mysql
# 状态应该是 Up (healthy)

# 3. 重启应用
docker-compose -f deploy/docker-compose.yml restart app
```

### 端口被占用

```bash
# 查看端口占用
# Linux
lsof -i :8080
# CentOS
netstat -tlnp | grep 8080

# 修改 .env 中的端口
APP_PORT=9090
```

### 编译失败

```bash
# 清理并重新编译
docker-compose -f deploy/docker-compose.yml down
rm -rf target/
docker-compose -f deploy/docker-compose.yml up -d --build
```

### Git 拉取失败

```bash
# 检查本地更改
git status

# 保存本地更改
git stash

# 拉取更新
git pull

# 恢复本地更改
git stash pop
```

---

## 🔐 安全建议

### 1. 配置文件安全

```bash
# 设置 .env 文件权限
chmod 600 .env

# 设置数据目录权限
chmod 700 mysql-data/

# 确保不提交敏感文件
echo ".env" >> .gitignore
echo "mysql-data/" >> .gitignore
```

### 2. 使用强密码

```bash
# 生成随机密码
openssl rand -base64 32
```

### 3. 限制数据库访问

如果不需要外部访问 MySQL，可以在 `deploy/docker-compose.yml` 中注释掉端口映射：

```yaml
mysql:
  # ports:
  #   - "${MYSQL_PORT:-3306}:3306"
```

### 4. 防火墙配置

```bash
# CentOS 7
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --reload

# Ubuntu
ufw allow 8080/tcp
ufw enable
```

---

## 📖 工作原理

```
宿主机（CentOS/Linux）
├── /opt/LettersInTime/  (Git 仓库)
│   ├── src/              (源代码)
│   ├── pom.xml
│   └── deploy/
│       ├── docker-compose.yml
│       ├── Dockerfile
│       └── deploy.sh
│
│   通过 Volume 挂载到容器
│         ↓
├── Docker Container
│   ├── /app (挂载点)
│   │   └── (宿主机代码)
│   ├── Maven 环境
│   └── 启动流程：
│       1. 检查 pom.xml
│       2. mvn clean package
│       3. java -jar target/*.jar
│
└── mysql-data/  (数据持久化)
```

---

## 📞 获取帮助

- 查看主文档：[../README.md](../README.md)
- 提交 Issue：[GitHub Issues](https://github.com/YS1109/LettersInTime/issues)
- 查看日志：`docker-compose -f deploy/docker-compose.yml logs -f`

---

**快速部署，持续更新！** 🚀
