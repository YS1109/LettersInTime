# LettersInTime 配置说明

## 📋 配置优先级

Spring Boot 配置加载优先级（从高到低）：

1. **环境变量** (Docker Compose 注入的环境变量) - **最高优先级**
2. **env-config.yml** (本地开发配置文件)
3. **application-{profile}.yml** (环境特定配置)
4. **application.yml** (默认配置)

## 🔄 配置工作原理

### Docker 部署模式（推荐）

当使用 Docker Compose 部署时：

```yaml
# docker-compose.yml 注入环境变量
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/letters-in-time
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD}
  SPRING_MAIL_USERNAME: ${MAIL_USERNAME}
  SPRING_MAIL_PASSWORD: ${MAIL_PASSWORD}
  ...
```

Spring Boot 会：
1. ✅ **优先使用环境变量**中的配置
2. ❌ 忽略 `env-config.yml` 中的同名配置
3. ✅ 使用环境变量中指定的数据库、邮箱等配置

**配置流程**：
```
.env 文件
   ↓
Docker Compose 读取
   ↓
注入为容器环境变量
   ↓
Spring Boot 读取环境变量
   ↓
应用使用环境变量配置
```

### 本地开发模式

当本地直接运行应用时：

```bash
mvn spring-boot:run
```

Spring Boot 会：
1. ✅ 读取 `env-config.yml` 文件
2. ✅ 使用文件中的数据库、邮箱等配置
3. ❌ 不读取 `.env` 文件（`.env` 是给 Docker Compose 用的）

## 📝 配置文件详解

### 1. `.env` - Docker 环境变量文件

**位置**: 项目根目录  
**用途**: Docker Compose 部署时使用  
**提交**: ❌ 不提交到 Git（在 `.gitignore` 中）

```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=letters-in-time

# 邮件配置
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_mail_auth_code

# 阿里云配置
ALIYUN_ACCESS_KEY_ID=your_key_id
ALIYUN_ACCESS_KEY_SECRET=your_key_secret
```

### 2. `env-config.yml` - 本地开发配置文件

**位置**: `src/main/resources/`  
**用途**: 本地开发时使用  
**提交**: ❌ 不提交到 Git（在 `.gitignore` 中）

```yaml
spring:
  mail:
    host: smtp.qq.com
    username: your_email@qq.com
    password: your_mail_auth_code
  
  datasource:
    url: jdbc:mysql://localhost:3306/letters-in-time
    username: root
    password: 12345678

aliyun:
  green:
    access-key-id: your_key_id
    access-key-secret: your_key_secret
```

### 3. `env-config.yml.example` - 配置模板

**位置**: `src/main/resources/`  
**用途**: 配置文件模板  
**提交**: ✅ 提交到 Git

```yaml
# 复制此文件为 env-config.yml 并填入真实配置
spring:
  mail:
    username: your_email@qq.com
    password: your_mail_auth_code
  datasource:
    password: your_db_password
```

### 4. `application-prod.yml` - 生产环境配置

**位置**: `src/main/resources/`  
**用途**: 生产环境配置，使用环境变量占位符  
**提交**: ✅ 提交到 Git

```yaml
spring:
  datasource:
    # ${ENV_VAR:default_value} 格式
    # 优先使用环境变量 ENV_VAR，如果没有则使用 default_value
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/letters-in-time}
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD:12345678}
  
  mail:
    host: ${SPRING_MAIL_HOST:smtp.qq.com}
    username: ${SPRING_MAIL_USERNAME:}
    password: ${SPRING_MAIL_PASSWORD:}
```

## 🚀 快速配置指南

### Docker 部署配置

```bash
# 1. 复制环境变量模板
cp env.example .env

# 2. 编辑 .env 文件，填入真实配置
vim .env

# 3. 启动服务（会自动使用 .env 中的配置）
docker-compose -f deploy/docker-compose.yml up -d --build
```

**不需要**修改 `env-config.yml`，因为环境变量优先级更高！

### 本地开发配置

```bash
# 1. 复制配置模板
cp src/main/resources/env-config.yml.example src/main/resources/env-config.yml

# 2. 编辑配置文件，填入真实配置
vim src/main/resources/env-config.yml

# 3. 启动应用
mvn spring-boot:run
```

## 🔍 验证配置

### 查看生效的配置

**方法 1：查看应用日志**

```bash
# Docker 部署
docker-compose -f deploy/docker-compose.yml logs app | grep -i "datasource\|mail"

# 本地运行
# 查看控制台输出
```

**方法 2：查看环境变量（Docker）**

```bash
# 进入容器
docker exec -it letters-app bash

# 查看环境变量
env | grep -E "SPRING_|ALIYUN_|MAIL_|MYSQL_"
```

**方法 3：添加日志输出**

在 `LettersInTimeApplication.java` 中添加：

```java
@PostConstruct
public void logConfig() {
    log.info("数据库 URL: {}", environment.getProperty("spring.datasource.url"));
    log.info("邮箱账号: {}", environment.getProperty("spring.mail.username"));
}
```

## ❓ 常见问题

### Q1: Docker 部署时配置不生效？

**检查步骤**：

```bash
# 1. 确认 .env 文件存在
ls -la .env

# 2. 查看 .env 内容
cat .env

# 3. 查看容器环境变量
docker exec letters-app env | grep SPRING

# 4. 重启容器
docker-compose -f deploy/docker-compose.yml restart app
```

### Q2: 本地开发时找不到 env-config.yml？

```bash
# 1. 确认文件是否存在
ls -la src/main/resources/env-config.yml

# 2. 如果不存在，从模板复制
cp src/main/resources/env-config.yml.example src/main/resources/env-config.yml

# 3. 编辑配置
vim src/main/resources/env-config.yml
```

### Q3: 修改配置后没有生效？

**Docker 部署**：
```bash
# 修改 .env 后需要重启容器
docker-compose -f deploy/docker-compose.yml restart app
```

**本地开发**：
```bash
# 修改 env-config.yml 后需要重启应用
# 在 IDE 中停止并重新运行
```

### Q4: 如何在 Docker 中使用本地配置文件？

**不推荐**！应该使用环境变量。如果确实需要：

```yaml
# docker-compose.yml
volumes:
  - ..:/app
  - ../custom-env-config.yml:/app/src/main/resources/env-config.yml:ro
```

### Q5: 配置优先级如何验证？

创建测试配置：

```yaml
# env-config.yml
spring:
  datasource:
    password: file_password

# 环境变量
SPRING_DATASOURCE_PASSWORD=env_password
```

启动应用，查看日志，应该使用 `env_password`（环境变量优先）。

## 🔐 安全最佳实践

### 1. 不要提交敏感配置

```bash
# 确保 .gitignore 包含：
.env
env-config.yml
mysql-data/
```

### 2. 使用强密码

```bash
# 生成强密码
openssl rand -base64 32
```

### 3. 定期轮换密钥

- 数据库密码：每 3-6 个月
- 邮箱授权码：每 6 个月
- 阿里云密钥：每 6 个月

### 4. 限制配置文件权限

```bash
chmod 600 .env
chmod 600 src/main/resources/env-config.yml
```

## 📊 配置对照表

| 配置项 | .env 变量名 | application-prod.yml 占位符 | env-config.yml 路径 |
|--------|-------------|------------------------------|---------------------|
| 数据库 URL | - | SPRING_DATASOURCE_URL | spring.datasource.url |
| 数据库用户名 | MYSQL_USERNAME | SPRING_DATASOURCE_USERNAME | spring.datasource.username |
| 数据库密码 | MYSQL_ROOT_PASSWORD | SPRING_DATASOURCE_PASSWORD | spring.datasource.password |
| 邮箱账号 | MAIL_USERNAME | SPRING_MAIL_USERNAME | spring.mail.username |
| 邮箱密码 | MAIL_PASSWORD | SPRING_MAIL_PASSWORD | spring.mail.password |
| 阿里云 Key ID | ALIYUN_ACCESS_KEY_ID | ALIYUN_GREEN_ACCESS_KEY_ID | aliyun.green.access-key-id |
| 阿里云 Key Secret | ALIYUN_ACCESS_KEY_SECRET | ALIYUN_GREEN_ACCESS_KEY_SECRET | aliyun.green.access-key-secret |

## 🔄 配置迁移

### 从文件配置迁移到环境变量

```bash
# 1. 查看当前 env-config.yml 配置
cat src/main/resources/env-config.yml

# 2. 将配置迁移到 .env
cat > .env << EOF
MYSQL_ROOT_PASSWORD=your_password
MAIL_USERNAME=your_email@qq.com
MAIL_PASSWORD=your_auth_code
ALIYUN_ACCESS_KEY_ID=your_key_id
ALIYUN_ACCESS_KEY_SECRET=your_key_secret
EOF

# 3. 使用 Docker 部署
docker-compose -f deploy/docker-compose.yml up -d --build
```

---

**配置清晰，部署简单！** 🚀

