## LettersInTime
Letters to Tomorrow

### 接口文档

#### 1. 创建计划发送邮件任务

- **URL**: `/api/scheduled-emails/create`
- **Method**: `POST`
- **Content-Type**: `application/json`
- **说明**: 在数据库中创建一条计划发送的邮件记录，由定时任务配合线程池在到期后异步发送。

##### 1.1 请求体（Request Body）

```json
{
  "to": "xxx@qq.com",
  "subject": "测试标题",
  "content": "测试内容",
  "scheduledTime": "2025-12-31T10:00:00"
}
```

- **字段说明**：
  - `to`（string，必填）: 收件人邮箱，必须是合法邮箱格式。
  - `subject`（string，必填）: 邮件主题，最大长度 255 字符。
  - `content`（string，必填）: 邮件内容，非空字符串。
  - `scheduledTime`（string，必填）: 计划发送时间，ISO-8601 格式，例如 `2025-12-31T10:00:00`。
    - 业务约束：**必须晚于当前时间至少 5 分钟**，否则会被拒绝。

##### 1.2 统一响应结构（Response）

所有接口统一返回结构为：

```json
{
  "code": 0,
  "message": "成功",
  "data": null
}
```

- **字段说明**：
  - `code`（int）: 错误码，`0` 表示成功，非 0 表示失败。
  - `message`（string）: 提示信息。
  - `data`（any）: 业务数据，当前创建任务接口返回 `null`。

##### 1.3 成功示例

请求：

```http
POST /api/scheduled-emails/create HTTP/1.1
Content-Type: application/json

{
  "to": "user@example.com",
  "subject": "测试标题",
  "content": "测试内容",
  "scheduledTime": "2025-12-31T10:00:00"
}
```

响应：

```json
{
  "code": 0,
  "message": "成功",
  "data": null
}
```

##### 1.4 失败示例

- **参数错误**（例如 `scheduledTime` 在 5 分钟以内 或 必填字段为空）：

```json
{
  "code": 1001,
  "message": "计划发送时间必须晚于当前时间至少5分钟",
  "data": null
}
```

- **其他系统异常**：

```json
{
  "code": 2000,
  "message": "系统异常",
  "data": null
}
```

### 错误码说明

来自 `ErrorCode` 定义：

- `0` (**SUCCESS**)：成功
- `1001` (**PARAM_ERROR**)：参数错误（包括 JSR-303 校验失败、业务参数校验失败）
- `1004` (**NOT_FOUND**)：资源不存在
- `2000` (**SYSTEM_ERROR**)：系统异常（未捕获的运行时错误）
- `3001` (**MAIL_SEND_FAILED**)：邮件发送失败（预留，当前发送异常时统一映射为系统异常，可根据需要细化）

### 环境与配置说明（简要）

- **环境切换**：通过 `spring.profiles.active` 切换：
  - 开发环境：`dev`
  - 正式环境：`prod`
- **配置文件**：
  - 公共：`application.yml`
  - 开发：`application-dev.yml`（导入 `mail-config-dev.yml`、`db-config-dev.yml`）
  - 正式：`application-prod.yml`（导入 `mail-config-prod.yml`、`db-config-prod.yml`）
  - 各环境真实敏感配置文件已在 `.gitignore` 中忽略，模板以 `*.example` 形式提供。
