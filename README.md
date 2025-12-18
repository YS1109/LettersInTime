# LettersInTime

Letters to Tomorrow - 定时邮件发送服务

---

## 📖 项目简介

LettersInTime 是一个基于 Spring Boot 的定时邮件发送服务，允许用户创建定时发送的邮件任务，在指定时间自动发送邮件。

### 核心功能

- ✅ 定时邮件发送
- ✅ 邮件内容审核（阿里云内容安全）
- ✅ 异步任务处理
- ✅ RESTful API 接口

### 技术栈

- **后端框架**: Spring Boot 2.7.18
- **数据库**: MySQL 8.0
- **持久层**: MyBatis-Plus
- **邮件服务**: Spring Mail (SMTP)
- **内容审核**: 阿里云内容安全
- **构建工具**: Maven
- **容器化**: Docker + Docker Compose

---

## 🚀 快速部署

### 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- Git

### 部署步骤

```bash
# 1. 克隆项目
git clone https://github.com/your-username/LettersInTime.git
cd LettersInTime

# 2. 配置环境变量
cp env.example .env
vim .env  # 编辑配置

# 3. 启动服务
docker-compose -f deploy/docker-compose.yml up -d --build

# 4. 查看日志
docker-compose -f deploy/docker-compose.yml logs -f app
```

### 一键更新

```bash
# Linux/Mac
./deploy/deploy.sh

# Windows
deploy\deploy.bat
```

**详细部署文档**：查看 [deploy/README.md](deploy/README.md)

---

## 📝 接口文档

### 1. 创建计划发送邮件任务

#### 接口信息

- **URL**: `/api/scheduledEmails/create`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **说明**: 在数据库中创建一条计划发送的邮件记录，由定时任务配合线程池在到期后异步发送。

#### 请求体（Request Body）

```json
{
  "to": "xxx@qq.com",
  "subject": "测试标题",
  "content": "测试内容",
  "scheduledTime": "2025-12-31T10:00:00"
}
```

**字段说明**：
- `to`（string，必填）: 收件人邮箱，必须是合法邮箱格式
- `subject`（string，必填）: 邮件主题，最大长度 4096 字符
- `content`（string，必填）: 邮件内容，非空字符串
- `scheduledTime`（string，必填）: 计划发送时间，yyyy-MM-dd HH:mm:ss 格式，例如 `2025-12-31 10:00:00`
  - **业务约束**: 必须晚于当前时间至少 5 分钟，否则会被拒绝

#### 统一响应结构（Response）

```json
{
  "code": 0,
  "message": "成功",
  "data": null
}
```

**字段说明**：
- `code`（int）: 错误码，`0` 表示成功，非 0 表示失败
- `message`（string）: 提示信息
- `data`（any）: 业务数据，当前创建任务接口返回 `null`

#### 成功示例

**请求**：

```bash
curl -X POST http://localhost:8080/api/scheduledEmails/create \
  -H "Content-Type: application/json" \
  -d '{
    "to": "user@example.com",
    "subject": "测试标题",
    "content": "测试内容",
    "scheduledTime": "2025-12-31 10:00:00"
  }'
```

**响应**：

```json
{
  "code": 0,
  "message": "成功",
  "data": null
}
```

#### 失败示例

**参数错误**（例如 `scheduledTime` 在 5 分钟以内 或 必填字段为空）：

```json
{
  "code": 1001,
  "message": "计划发送时间必须晚于当前时间至少5分钟",
  "data": null
}
```

**其他系统异常**：

```json
{
  "code": 2000,
  "message": "系统异常",
  "data": null
}
```

---

## 🔧 错误码说明

来自 `ErrorCode` 定义：

| 错误码 | 名称 | 说明 |
|--------|------|------|
| `0` | SUCCESS | 成功 |
| `1001` | PARAM_ERROR | 参数错误（包括 JSR-303 校验失败、业务参数校验失败） |
| `1004` | NOT_FOUND | 资源不存在 |
| `2000` | SYSTEM_ERROR | 系统异常（未捕获的运行时错误） |
| `3001` | MAIL_SEND_FAILED | 邮件发送失败（预留，当前发送异常时统一映射为系统异常，可根据需要细化） |

---

## ⚙️ 配置说明

### 配置方式

项目支持两种配置方式：

1. **Docker 部署（推荐）**：使用 `.env` 文件配置环境变量
2. **本地开发**：使用 `env-config.yml` 文件配置

### 配置优先级

Spring Boot 配置加载优先级（从高到低）：

1. **环境变量** - Docker Compose 注入（最高优先级）
2. **env-config.yml** - 本地开发配置文件
3. **application-{profile}.yml** - 环境特定配置
4. **application.yml** - 默认配置

### 快速配置

#### Docker 部署配置

```bash
# 1. 复制环境变量模板
cp env.example .env

# 2. 编辑配置（填入真实值）
vim .env
```

**必填配置项**：
```bash
# 数据库密码
MYSQL_ROOT_PASSWORD=your_secure_password

# 邮箱配置（QQ 邮箱使用授权码）
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_mail_auth_code

# 阿里云配置
ALIYUN_ACCESS_KEY_ID=your_key_id
ALIYUN_ACCESS_KEY_SECRET=your_key_secret
```

**获取 QQ 邮箱授权码**：  
QQ 邮箱 → 设置 → 账户 → POP3/SMTP 服务 → 开启并生成授权码

#### 本地开发配置

```bash
# 1. 复制配置模板
cp src/main/resources/env-config.yml.example src/main/resources/env-config.yml

# 2. 编辑配置
vim src/main/resources/env-config.yml
```

### 详细配置文档

查看 **[CONFIG.md](CONFIG.md)** 获取：
- 配置工作原理
- 完整配置项说明
- 配置验证方法
- 常见问题解决

---

## 📦 数据库

### 数据表结构

#### scheduled_email 表

```sql
CREATE TABLE `scheduled_email` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recipient` VARCHAR(255) NOT NULL COMMENT '收件人邮箱',
  `subject` VARCHAR(255) NOT NULL COMMENT '邮件主题',
  `content` TEXT NOT NULL COMMENT '邮件正文',
  `scheduled_time` DATETIME NOT NULL COMMENT '计划发送时间',
  `sent_time` DATETIME DEFAULT NULL COMMENT '实际发送时间',
  `status` VARCHAR(20) NOT NULL COMMENT '状态：CREATED / APPROVED / REJECTED / SENDING / SENT / FAILED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_scheduled_email_status_time` (`status`, `scheduled_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划发送邮件表';
```

### 数据持久化

- MySQL 数据存储在 `mysql-data/` 目录
- 容器删除/重启后数据不丢失
- 可直接备份该目录

---

## 🛠️ 开发指南

### 本地开发环境

#### 前置要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+

#### 启动步骤

```bash
# 1. 配置数据库
# 创建数据库
mysql -uroot -p < scripts/mysql_schema.sql

# 2. 配置环境变量
cp src/main/resources/env-config.yml.example src/main/resources/env-config.yml
vim src/main/resources/env-config.yml

# 3. 启动应用
mvn spring-boot:run

# 或使用 IDE 运行 LettersInTimeApplication.java
```

### 项目结构

```
LettersInTime/
├── src/
│   ├── main/
│   │   ├── java/com/ysoztf/release/letter/
│   │   │   ├── LettersInTimeApplication.java    # 主类
│   │   │   ├── controller/                       # 控制器
│   │   │   ├── service/                          # 业务逻辑
│   │   │   ├── mapper/                           # MyBatis Mapper
│   │   │   ├── entity/                           # 实体类
│   │   │   ├── dto/                              # 数据传输对象
│   │   │   ├── common/                           # 公共类
│   │   │   └── config/                           # 配置类
│   │   └── resources/
│   │       ├── application.yml                   # 主配置
│   │       ├── application-dev.yml               # 开发环境
│   │       ├── application-prod.yml              # 生产环境
│   │       └── env-config.yml                    # 敏感配置（不提交）
│   └── test/                                     # 测试代码
├── scripts/
│   └── mysql_schema.sql                          # 数据库初始化脚本
├── deploy/
│   ├── docker-compose.yml                        # Docker Compose 配置
│   ├── Dockerfile                                # Dockerfile
│   ├── deploy.sh                                 # 部署脚本
│   └── README.md                                 # 部署文档
├── pom.xml                                       # Maven 配置
├── env.example                                   # 环境变量模板
└── README.md                                     # 项目文档
```

---

## 📋 常用命令

### Docker 部署

```bash
# 启动服务
docker-compose -f deploy/docker-compose.yml up -d

# 停止服务
docker-compose -f deploy/docker-compose.yml down

# 查看日志
docker-compose -f deploy/docker-compose.yml logs -f app

# 重启服务
docker-compose -f deploy/docker-compose.yml restart

# 进入容器
docker exec -it letters-app bash
```

### Git 管理

```bash
# 拉取最新代码
git pull origin main

# 查看提交历史
git log --oneline

# 回滚到指定版本
git reset --hard <commit-hash>
```

### Maven 构建

```bash
# 编译
mvn clean compile

# 打包（跳过测试）
mvn clean package -DskipTests

# 运行测试
mvn test

# 清理
mvn clean
```

---

## 🔐 安全建议

### 1. 配置文件安全

```bash
# 设置 .env 文件权限
chmod 600 .env

# 不要提交敏感配置
# 已在 .gitignore 中配置：
# - .env
# - env-config.yml
# - mysql-data/
```

### 2. 使用强密码

- 数据库密码：至少 16 位，包含大小写字母、数字、特殊字符
- 定期更换密码

### 3. 限制端口访问

- 生产环境建议移除 MySQL 端口映射
- 配置防火墙规则

### 4. HTTPS

生产环境建议配置 HTTPS：
- 使用 Nginx 反向代理
- 申请 SSL 证书（Let's Encrypt）

---

## 📞 问题反馈

- **GitHub Issues**: [提交 Issue](https://github.com/YS1109/LettersInTime/issues)
- **文档**: [部署文档](deploy/README.md)

---

## 📄 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)
- [MySQL](https://www.mysql.com/)
- [Docker](https://www.docker.com/)

---

**快速部署，专注业务！** 🚀
