# LettersInTime 项目结构说明

## 📁 目录结构

```
LettersInTime/
├── deploy/                           # 部署文件夹
│   ├── docker-compose.yml           # Docker Compose 配置
│   ├── Dockerfile                   # Docker 镜像构建文件
│   ├── deploy.sh                    # Linux/Mac 部署脚本
│   ├── deploy.bat                   # Windows 部署脚本
│   └── README.md                    # 部署文档
│
├── src/                             # 源代码
│   ├── main/
│   │   ├── java/com/ysoztf/release/letter/
│   │   │   ├── LettersInTimeApplication.java    # 主类
│   │   │   ├── controller/                       # 控制器层
│   │   │   │   └── ScheduledEmailController.java
│   │   │   ├── service/                          # 业务逻辑层
│   │   │   │   ├── ScheduledEmailService.java
│   │   │   │   ├── MailService.java
│   │   │   │   ├── TextModerationService.java
│   │   │   │   └── impl/
│   │   │   ├── mapper/                           # 数据访问层
│   │   │   │   └── ScheduledEmailMapper.java
│   │   │   ├── entity/                           # 实体类
│   │   │   │   ├── ScheduledEmail.java
│   │   │   │   └── ScheduledEmailStatus.java
│   │   │   ├── dto/                              # 数据传输对象
│   │   │   │   └── ScheduledEmailCreateRequest.java
│   │   │   ├── common/                           # 公共类
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   ├── BizException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── config/                           # 配置类
│   │   │       ├── AsyncConfig.java
│   │   │       └── AliyunGreenProperties.java
│   │   └── resources/
│   │       ├── application.yml                   # 主配置文件
│   │       ├── application-dev.yml               # 开发环境配置
│   │       ├── application-prod.yml              # 生产环境配置
│   │       ├── env-config.yml                    # 敏感配置（不提交）
│   │       ├── env-config.yml.example            # 配置模板
│   │       └── logback-spring.xml                # 日志配置
│   └── test/                                     # 测试代码
│
├── scripts/                          # 脚本文件
│   └── mysql_schema.sql             # 数据库初始化脚本
│
├── mysql-data/                       # MySQL 数据目录（自动生成，不提交）
├── logs/                             # 日志目录（自动生成）
├── target/                           # 编译输出目录（自动生成，不提交）
│
├── pom.xml                           # Maven 配置
├── env.example                       # 环境变量模板
├── .env                              # 环境变量配置（不提交）
├── .gitignore                        # Git 忽略规则
├── README.md                         # 项目文档
└── PROJECT-STRUCTURE.md              # 本文档
```

---

## 🎯 核心文件说明

### 部署相关

| 文件 | 说明 |
|------|------|
| `deploy/docker-compose.yml` | Docker Compose 编排配置，定义 MySQL 和应用服务 |
| `deploy/Dockerfile` | Docker 镜像构建文件，容器内编译和运行 |
| `deploy/deploy.sh` | 自动部署脚本（Linux/Mac），执行 git pull + 重启容器 |
| `deploy/deploy.bat` | 自动部署脚本（Windows） |
| `deploy/README.md` | 详细的部署文档 |

### 配置文件

| 文件 | 说明 |
|------|------|
| `env.example` | 环境变量模板，包含所有配置项的示例 |
| `.env` | 实际环境变量配置（需从模板复制并填入真实值） |
| `application.yml` | Spring Boot 主配置 |
| `application-dev.yml` | 开发环境配置 |
| `application-prod.yml` | 生产环境配置 |
| `env-config.yml` | 敏感配置（邮件、数据库、阿里云密钥等） |
| `pom.xml` | Maven 项目配置，定义依赖和构建规则 |

### 代码文件

| 文件 | 说明 |
|------|------|
| `LettersInTimeApplication.java` | Spring Boot 主类，应用入口 |
| `ScheduledEmailController.java` | RESTful API 控制器 |
| `ScheduledEmailService.java` | 邮件任务业务逻辑 |
| `MailService.java` | 邮件发送服务 |
| `TextModerationService.java` | 阿里云内容审核服务 |
| `ScheduledEmailMapper.java` | MyBatis 数据访问接口 |
| `ScheduledEmail.java` | 邮件任务实体类 |

---

## 🔄 工作流程

### 1. 开发流程

```
开发者修改代码
     ↓
git add & commit
     ↓
git push to GitHub
     ↓
服务器执行 ./deploy/deploy.sh
     ↓
自动拉取最新代码
     ↓
容器内重新编译
     ↓
重启应用
```

### 2. 部署流程

```
克隆项目到服务器
     ↓
配置 .env 文件
     ↓
docker-compose -f deploy/docker-compose.yml up -d --build
     ↓
容器内自动：
  1. 检查代码目录
  2. mvn clean package
  3. java -jar target/*.jar
     ↓
应用启动，监听 8080 端口
```

### 3. 更新流程

```
执行 ./deploy/deploy.sh
     ↓
git pull origin main
     ↓
docker-compose restart
     ↓
容器内重新编译
     ↓
应用重启
```

---

## 📦 数据持久化

### MySQL 数据

- **位置**: `./mysql-data/`
- **特点**: 挂载到容器，数据保存在宿主机
- **备份**: 直接备份该目录即可

### 应用日志

- **位置**: `./logs/`
- **日志文件**: `app.log`, `app.YYYY-MM-DD.log`
- **配置**: `src/main/resources/logback-spring.xml`

### Maven 依赖缓存

- **位置**: Docker Volume `maven-repo`
- **作用**: 加速构建，避免每次重新下载依赖

---

## 🚀 快速命令参考

### 部署命令

```bash
# 首次部署
docker-compose -f deploy/docker-compose.yml up -d --build

# 更新部署
./deploy/deploy.sh

# 停止服务
docker-compose -f deploy/docker-compose.yml down

# 查看日志
docker-compose -f deploy/docker-compose.yml logs -f app
```

### 开发命令

```bash
# 本地编译
mvn clean package -DskipTests

# 本地运行
mvn spring-boot:run

# 运行测试
mvn test
```

### Git 命令

```bash
# 拉取最新代码
git pull origin main

# 查看状态
git status

# 查看提交历史
git log --oneline

# 回滚版本
git reset --hard <commit-hash>
```

---

## 🔧 配置优先级

Spring Boot 配置加载优先级（从高到低）：

1. **环境变量** (Docker Compose 从 `.env` 注入) - **最高优先级**
2. **env-config.yml** (本地开发敏感配置)
3. **application-{profile}.yml** (环境特定配置，使用环境变量占位符)
4. **application.yml** (默认配置)

**重要**：`application-prod.yml` 中的配置使用 `${ENV_VAR:default}` 格式，优先读取环境变量。

**详细说明**：查看 [CONFIG.md](CONFIG.md)

---

## 📝 最佳实践

### 1. 配置管理

- ✅ 使用 `env.example` 作为模板
- ✅ 不要提交 `.env` 和 `env-config.yml`
- ✅ 敏感信息通过环境变量注入
- ✅ 使用强密码

### 2. 版本管理

- ✅ 开发分支：`dev`
- ✅ 生产分支：`main`
- ✅ 使用 Git tag 标记版本：`git tag -a v1.0.0 -m "Release 1.0.0"`
- ✅ 保持提交历史清晰

### 3. 部署管理

- ✅ 使用 `./deploy/deploy.sh` 自动化部署
- ✅ 部署前先在 dev 环境测试
- ✅ 定期备份 `mysql-data` 目录
- ✅ 监控日志文件大小

### 4. 安全管理

- ✅ 定期更新依赖版本
- ✅ 使用 HTTPS（生产环境）
- ✅ 限制数据库端口访问
- ✅ 配置防火墙规则

---

## 🆘 故障排查

### 应用无法启动

1. 查看日志：`docker-compose -f deploy/docker-compose.yml logs app`
2. 检查配置：确认 `.env` 文件配置正确
3. 检查 MySQL：`docker-compose -f deploy/docker-compose.yml ps mysql`

### 编译失败

1. 清理缓存：`rm -rf target/`
2. 检查代码：确保没有语法错误
3. 重新构建：`docker-compose -f deploy/docker-compose.yml up -d --build`

### 数据库连接失败

1. 检查密码：确认 `.env` 中的密码正确
2. 检查网络：确认容器在同一网络
3. 检查健康状态：`docker inspect letters-mysql | grep -A 10 Health`

---

## 📚 相关文档

- [项目 README](README.md) - 项目介绍和 API 文档
- [部署文档](deploy/README.md) - 详细部署指南
- [环境变量模板](env.example) - 配置说明

---

**清晰的结构，高效的开发！** 🚀

