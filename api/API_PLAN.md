# TimeFlow API 规划

本文档概述了 TimeFlow 应用的 RESTful API 结构，旨在支持云同步等功能。

## API 设计原则

- **版本控制:** 大部分业务端点都以 `/api/v1` 作为前缀。
- **身份认证:** 受保护的端点需要在 `Authorization` 请求头中提供 JWT Bearer 令牌。
- **数据格式:** 使用 `application/json`。

---

## 0. 健康检查 API (`/ping`)

用于检查服务是否存活。

| 方法    | 端点         | 描述               | 认证       |
|:------|:-----------|:-----------------|:---------|
| `GET` | `/ping`    | 检查服务的可用性。        | &#10006; |
| `GET` | `/version` | 获取服务的构建版本和时间等信息。 | &#10006; |

---

## 1. 用户认证 API (`/auth`)

处理用户注册和登录，可公开访问。

| 方法     | 端点                             | 描述                                                             | 认证       |
|:-------|:-------------------------------|:---------------------------------------------------------------|:---------|
| `GET`  | `/auth/check-email`            | 检查指定的邮箱地址是否已经被注册。                                              | &#10006; |
| `POST` | `/auth/register`               | 使用邮箱、密码和收到的验证码注册一个新用户。                                         | &#10006; |
| `POST` | `/auth/send-verification-code` | 请求向指定邮箱发送一个注册验证码。                                              | &#10006; |
| `POST` | `/auth/login`                  | 用户登录并获取 JWT 令牌。                                                | &#10006; |
| `POST` | `/auth/logout`                 | 登出并撤销当前的 Refresh Token。                                        | &#10004; |
| `POST` | `/auth/refresh`                | 使用 Refresh Token 获取新的 Access Token。客户端可选择是否同时轮换 Refresh Token。 | &#10004; |

---

## 2. 课程表 API (`/schedules`)

管理用户的课程表集合。

| 方法       | 端点                        | 描述                         | 认证       |
|:---------|:--------------------------|:---------------------------|:---------|
| `GET`    | `/schedules`              | 获取当前用户的所有课程表概要列表。          | &#10004; |
| `GET`    | `/schedules/{scheduleId}` | 获取指定 ID 的单个课程表的完整信息。       | &#10004; |
| `PUT`    | `/schedules/{scheduleId}` | 创建或完整更新指定 ID 的课程表。         | &#10004; |
| `DELETE` | `/schedules/{scheduleId}` | 永久删除指定 ID 的课程表。软删除请使用 PUT。 | &#10004; |

---

## 3. 课程 API (`/schedules/{scheduleId}/courses`)

管理特定课程表内的课程。注意：这些操作也可以通过更新父级 `Schedule` 对象（通过 `PUT /schedules/{scheduleId}`）来间接实现。

| 方法       | 端点                                           | 描述                     | 认证       |
|:---------|:---------------------------------------------|:-----------------------|:---------|
| `GET`    | `/schedules/{scheduleId}/courses`            | 获取指定课程表下的所有课程概要列表。     | &#10004; |
| `GET`    | `/schedules/{scheduleId}/courses/{courseId}` | 获取指定 ID 的单个课程的完整信息。    | &#10004; |
| `PUT`    | `/schedules/{scheduleId}/courses/{courseId}` | 创建或更新指定课程表中的指定 ID 的课程。 | &#10004; |
| `DELETE` | `/schedules/{scheduleId}/courses/{courseId}` | 从指定课程表中删除指定 ID 的课程。    | &#10004; |

---

## 4. 用户 API (`/users/me`)

管理用户个人资料和偏好设置。

| 方法    | 端点                            | 描述                                                | 认证       |
|:------|:------------------------------|:--------------------------------------------------|:---------|
| `GET` | `/users/me`                   | 获取当前已登录用户的个人资料。                                   | &#10004; |
| `GET` | `/users/me/selected-schedule` | 获取当前选中的课程表 ID 及其更新时间（`scheduleId` 和 `updatedAt`）。 | &#10004; |
| `PUT` | `/users/me/selected-schedule` | 更新当前选中的课程表，请求体需包含 `scheduleId` 和 `updatedAt`。     | &#10004; |

> **注意**: 主题设置（主题模式、动态颜色、主题颜色等）属于本地 UI 偏好，应存储在客户端本地（如 DataStore），无需云端同步。

---

## 5. AI API (`/ai`)

使用 LLM 从课程表图片中提取课程信息。

| 方法     | 端点                     | 描述                          | 认证       |
|:-------|:-----------------------|:----------------------------|:---------|
| `GET`  | `/ai/info`             | 查询 AI 功能状态、配额和图片限制。         | &#10004; |
| `POST` | `/ai/extract-schedule` | 从课程表图片中提取课程信息。支持流式和非流式两种模式。 | &#10004; |

### 配额限制

每个用户默认每 6 个月可使用 4 次 AI 提取功能。管理员可在数据库中将用户的 `ai_unlimited` 设为 `true` 以解除限制。

### 请求

```json
POST /api/v1/ai/extract-schedule
Content-Type: application/json
Authorization: Bearer <access_token>

{
"image": "<base64_encoded_image>",
"stream": false
}
```

- `image`: Base64 编码的图片。服务端会在图片超过大小或分辨率限制时自动缩放。
- `stream`: 是否启用流式响应，默认 `false`。

### 非流式响应（`stream: false`，默认）

直接返回 JSON 数组：

```json
[
  {"name":"高等数学","teacher":"张三","classroom":"A101","time":[1,2],"weekday":0,"week":[1,2,3,...,16],"note":"必修 3学分"},
  {"name":"英语","teacher":"李四","classroom":"B202","time":[3,4],"weekday":1,"week":[1,2,3,...,16],"note":null}
]
```

### 流式响应（`stream: true`）

与 OpenAI Streaming API 格式一致：

```
data: <text chunk>

data: <text chunk>

data: [DONE]
```

客户端累积所有 `data:` 行的文本（忽略 `[DONE]`），然后解析完整文本为 JSONL 格式的课程列表。

### 响应头

| Header             | 描述                     |
|:-------------------|:-----------------------|
| `X-Ai-Model`       | 处理本次请求的模型 ID           |
| `X-Ai-Quota-Used`  | 本次请求后的已用次数             |
| `X-Ai-Quota-Limit` | 配额上限，`unlimited` 表示无限制 |

### 提取的课程 Schema

每个 JSONL 行对应一个课程，字段如下：

| 字段          | 类型      | 描述                       |
|:------------|:--------|:-------------------------|
| `name`      | string  | 课程名称                     |
| `teacher`   | string? | 授课教师，多位教师用 "/" 分隔        |
| `classroom` | string? | 上课教室                     |
| `time`      | int[]   | 节次范围，如 `[1, 3]` 表示第1-3节  |
| `weekday`   | int     | 星期几，0=周一, 6=周日           |
| `week`      | int[]?  | 教学周列表，如 `[1,2,3,...,16]` |
| `note`      | string? | 备注信息（课程类型、学分、课程代码、选课人数等） |

### 服务端配置

**基础配置：**

| 配置项                     | 默认值             | 描述                    |
|:------------------------|:----------------|:----------------------|
| `ai.enabled`            | `false`         | 是否启用 AI 提取功能          |
| `ai.maxImageSizeBytes`  | `2097152` (2MB) | 图片最大字节数，超出时自动缩放       |
| `ai.maxImageResolution` | `2048`          | 图片最大分辨率（长边像素），超出时自动缩放 |
| `ai.quotaPerHalfYear`   | `4`             | 每用户每6个月的默认配额          |

**提供商配置 (`ai.providers.<name>`)：**

每个提供商有一个自定义名称，配置协议格式、API 密钥和可选的自定义端点。

| 字段         | 描述                                                       |
|:-----------|:---------------------------------------------------------|
| `format`   | 协议格式：`openai`、`openrouter`、`google`、`anthropic`、`ollama` |
| `apiKey`   | API 密钥                                                   |
| `endpoint` | 自定义端点 URL（可选，用于 NVIDIA API、本地代理等）                        |

**模型配置 (`ai.models[]`)：**

| 字段         | 默认值  | 描述             |
|:-----------|:-----|:---------------|
| `id`       | (必填) | 模型 ID          |
| `provider` | (必填) | 引用的提供商名称       |
| `weight`   | `1`  | 权重，越大被选中概率越高   |
| `rpm`      | `0`  | 每分钟请求上限，0 表示不限 |
| `rpd`      | `0`  | 每天请求上限，0 表示不限  |

请求时按权重随机选择模型；若选中的模型调用失败，自动尝试其他模型；仅当所有模型均不可用时才返回错误。

**配置示例：**

```yaml
ai:
  enabled: true
  providers:
    nvidia:
      format: "openai"
      apiKey: "nvapi-..."
      endpoint: "https://integrate.api.nvidia.com/v1/chat/completions"
    openrouter:
      format: "openrouter"
      apiKey: "sk-or-..."
  models:
    - id: "qwen/qwen3.5-122b-a10b"
      provider: nvidia
      weight: 1
    - id: "google/gemini-2.5-flash"
      provider: openrouter
      weight: 2
      rpm: 10
      rpd: 1000
```

### 客户端直连 LLM

如果用户提供自己的 API 密钥，客户端可直接使用 `api:ai` 模块中的 `ScheduleExtractor` 调用 LLM，无需经过服务端：

```kotlin
val extractor = ScheduleExtractor(
   provider = "openai",
   apiKey = userApiKey,
   model = "gpt-4.1-mini",
   endpoint = "https://integrate.api.nvidia.com/v1/chat/completions" // 可选
)
val courses = extractor.extract(imageBase64)
// 或流式
extractor.extractStreaming(imageBase64).collect { delta -> ... }
```

---

## 6. 云同步逻辑

API 不提供专门的 `/sync` 端点。客户端应使用现有的 CRUD API 实现同步功能。

### 推荐的同步实现方式:

1. **获取服务端数据概要**:
   ```
   GET /api/v1/schedules
   GET /api/v1/users/me/selected-schedule
   ```
   返回所有课程表的摘要信息（包含 `updatedAt` 时间戳）以及选中的课程表 ID 和更新时间（`scheduleId` 和 `updatedAt`）。

2. **比较时间戳**:
    - 客户端将本地课程表的 `updatedAt` 与服务端进行比较
    - 比较本地的 `selectedScheduleUpdatedAt` 与服务端返回的值，同步选中状态
    - 对于 `updatedAt` 较新的课程表，下载完整数据

3. **下载更新的课程表**:
   ```
   GET /api/v1/schedules/{scheduleId}
   ```
   获取服务端更新过的课程表完整数据。

4. **上传本地更改**:
   ```
   PUT /api/v1/schedules/{scheduleId}        # 创建、更新或软删除（设置 deleted=true）
   PUT /api/v1/users/me/selected-schedule
   DELETE /api/v1/schedules/{scheduleId}     # 仅用于永久删除
   ```
   将本地的新建、修改、删除操作推送到服务端。

5. **冲突处理**:
    - 如果客户端和服务端都修改了同一课程表，比较 `updatedAt` 时间戳
    - 向用户展示两个版本的最后修改时间，让用户选择保留哪个版本

### 时间戳字段:

- `createdAt`: 课程表创建时间（Instant, 不可变）
- `updatedAt`: 课程表最后更新时间（Instant, 每次修改时更新）
- `selectedScheduleUpdatedAt`: 选中课程表的更新时间（Instant, 每次选择时自动设置）
