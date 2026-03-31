# TimeFlow API

完整的请求体、返回值、状态码、响应头和数据模型请以 [OpenAPI 文档](../server/src/main/resources/openapi/documentation.yaml)为准：

## 基本约定

- 业务接口默认前缀为 `/api/v1`
- 健康检查接口使用根路径，例如 `/ping`
- 受保护接口需要携带 JWT Bearer Token
- 默认数据格式为 `application/json`

## 健康检查

| 方法 | 端点 | 描述 | 认证 |
|:---|:---|:---|:---|
| `GET` | `/ping` | 检查服务是否存活 | &#10006; |
| `GET` | `/version` | 获取服务构建信息 | &#10006; |

## 认证

| 方法 | 端点 | 描述 | 认证 |
|:---|:---|:---|:---|
| `GET` | `/auth/check-email` | 检查邮箱是否已注册 | &#10006; |
| `GET` | `/auth/email-verification` | 查询当前是否启用邮箱验证码注册 | &#10006; |
| `POST` | `/auth/send-verification-code` | 发送注册验证码 | &#10006; |
| `POST` | `/auth/register` | 注册用户 | &#10006; |
| `POST` | `/auth/login` | 登录并获取令牌 | &#10006; |
| `POST` | `/auth/logout` | 登出并撤销刷新令牌 | &#10004; |
| `POST` | `/auth/refresh` | 刷新访问令牌 | &#10004; |

## 用户

| 方法 | 端点 | 描述 | 认证 |
|:---|:---|:---|:---|
| `GET` | `/users/me` | 获取当前用户信息 | &#10004; |
| `GET` | `/users/me/selected-schedule` | 获取当前选中的课程表 | &#10004; |
| `PUT` | `/users/me/selected-schedule` | 更新当前选中的课程表 | &#10004; |

> 主题模式、动态颜色、主题颜色等 UI 偏好属于本地配置，不通过云端同步。

## 课程表

| 方法 | 端点 | 描述 | 认证 |
|:---|:---|:---|:---|
| `GET` | `/schedules` | 获取当前用户的课程表列表 | &#10004; |
| `GET` | `/schedules/{scheduleId}` | 获取指定课程表详情 | &#10004; |
| `PUT` | `/schedules/{scheduleId}` | 创建或完整更新指定课程表 | &#10004; |
| `DELETE` | `/schedules/{scheduleId}` | 永久删除指定课程表 | &#10004; |

## 课程

| 方法 | 端点 | 描述 | 认证 |
|:---|:---|:---|:---|
| `GET` | `/schedules/{scheduleId}/courses` | 获取指定课程表下的课程列表 | &#10004; |
| `GET` | `/schedules/{scheduleId}/courses/{courseId}` | 获取指定课程详情 | &#10004; |
| `PUT` | `/schedules/{scheduleId}/courses/{courseId}` | 创建或更新指定课程 | &#10004; |
| `DELETE` | `/schedules/{scheduleId}/courses/{courseId}` | 删除指定课程 | &#10004; |

## AI

| 方法 | 端点 | 描述 | 认证 |
|:---|:---|:---|:---|
| `GET` | `/ai/info` | 查询 AI 功能状态与限制信息 | &#10004; |
| `POST` | `/ai/extract-schedule` | 从课程表图片中提取课程信息 | &#10004; |

## 同步

| 方法 | 端点 | 描述 | 认证 |
|:---|:---|:---|:---|
| `GET` | `/sync/info` | 查询同步配额信息 | &#10004; |

## 同步实现建议

客户端可基于现有 CRUD 接口实现同步流程：

1. 使用 `GET /schedules` 拉取课程表概要。
2. 使用 `GET /users/me/selected-schedule` 同步当前选中课程表。
3. 使用 `GET /schedules/{scheduleId}` 拉取需要更新的完整课程表。
4. 使用 `PUT /schedules/{scheduleId}` 和 `PUT /users/me/selected-schedule` 上传本地更改。
5. 使用 `DELETE /schedules/{scheduleId}` 执行永久删除。

如需精确同步语义、字段定义和错误处理规则，请直接查阅 OpenAPI 文档。
