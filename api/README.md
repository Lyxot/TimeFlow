# TimeFlow Server

## 命令行参数

服务启动时支持以下命令行参数：

| 参数 | 说明 |
|:---|:---|
| `--config <path>` / `-c <path>` | 指定配置文件路径；不传时默认尝试加载 `server.yml` |
| `--env <path>` / `-e <path>` | 指定 `.env` 文件路径；不传时默认使用 `.env` |
| `--host <host>` / `-h <host>` | 直接覆盖监听主机 |
| `--port <port>` / `-p <port>` | 直接覆盖监听端口 |
| `-P:key=value` | 直接覆盖配置键，例如 `-P:jwt.issuer=timeflow-api` |

## 通过 Jar 文件启动

1. 下载最新版本的 Jar 文件
2. 运行以下命令：

```bash
java -jar timeflow-server.jar --config server.yml --env .env
```

## 通过 Docker 启动

仓库根目录提供了示例文件 [docker-compose.yml](../docker-compose.yml)，可用于同时启动 PostgreSQL 和 TimeFlow 后端服务。

### 启动前准备

1. 复制根目录的 `server.example.yml` 为本地实际配置文件 `server.yml` 并按需修改
2. 复制根目录的 `.env.example` 为实际使用的 `.env` 并按需修改
3. 准备 JWT 密钥文件 `jwt-private.pem` 和 `jwt-public.pem`，使用 OpenSSL 生成 ES256 (P-256) 私钥和公钥：
   ```bash
   openssl ecparam -genkey -name prime256v1 -noout -out jwt-private.pem
   openssl ec -in jwt-private.pem -pubout -out jwt-public.pem
   ```

`docker-compose.yml` 当前会挂载以下文件：

| 本地文件 | 容器内路径 | 用途 |
|:---|:---|:---|
| `./server.yml` | `/server.yml` | 服务端配置文件 |
| `./jwt-private.pem` | `/jwt-private.pem` | JWT 私钥 |
| `./jwt-public.pem` | `/jwt-public.pem` | JWT 公钥 |
| `./db` | `/var/lib/postgresql` | PostgreSQL 数据目录 |

### 启动命令

```bash
docker compose up -d
```

## 配置文件说明

> [server.example.yml](../server.example.yml) 是配置项说明和默认值示例的基准文件

服务启动时会按以下顺序合并配置，后面的来源会覆盖前面的值：

1. 内置默认值
2. 配置文件（默认 `server.yml`，或通过命令行指定）
3. `.env` 文件中的环境变量风格键值
4. 进程环境变量
5. Java System Properties
6. `-P:key=value` 形式的命令行属性覆盖

其中，`host` 和 `port` 还可以被 `--host` / `--port` 命令行参数进一步覆盖。

### 顶层配置概览

| 配置块 | 作用 |
|:---|:---|
| `host` / `port` | 服务监听地址与端口 |
| `jwt` | JWT 鉴权和密钥文件配置 |
| `postgres` | PostgreSQL 数据库连接配置 |
| `email` | 邮件发送与验证码配置 |
| `http` | 代理头、CORS、HSTS、HTTPS 重定向等 HTTP 中间件配置 |
| `sync` | 云同步配额配置 |
| `ai` | AI 课程表识别配置 |
| `webApp` | 是否由后端托管 Web 静态包 |
| `turnstile` | Cloudflare Turnstile 人机校验配置 |

### 基础监听

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `host` | `TIMEFLOW_HOST` | 服务监听主机，默认值为 `0.0.0.0` |
| `port` | `TIMEFLOW_PORT` | HTTP 监听端口，默认值为 `8080` |

对公网部署通常保持默认监听地址；如只需本机访问，可将 `host` 改为 `127.0.0.1`。

### JWT 配置

`jwt` 配置块用于访问令牌和刷新令牌的签发与校验。

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `jwt.domain` | `TIMEFLOW_JWT_DOMAIN` | JWT 域名信息 |
| `jwt.audience` | `TIMEFLOW_JWT_AUDIENCE` | JWT 目标受众，客户端应与之匹配 |
| `jwt.realm` | `TIMEFLOW_JWT_REALM` | 认证域名称 |
| `jwt.issuer` | `TIMEFLOW_JWT_ISSUER` | JWT 签发者标识 |
| `jwt.privateKeyPath` | `TIMEFLOW_JWT_PRIVATE_KEY_PATH` | 私钥文件路径 |
| `jwt.publicKeyPath` | `TIMEFLOW_JWT_PUBLIC_KEY_PATH` | 公钥文件路径 |
| `jwt.privateKeyPem` | `TIMEFLOW_JWT_PRIVATE_KEY_PEM` | 直接提供 PEM 格式私钥文本，优先于路径配置 |
| `jwt.publicKeyPem` | `TIMEFLOW_JWT_PUBLIC_KEY_PEM` | 直接提供 PEM 格式公钥文本，优先于路径配置 |

> 使用 OpenSSL 生成 ES256 (P-256) 私钥和公钥：
> ```bash
> openssl ecparam -genkey -name prime256v1 -noout -out jwt-private.pem
> openssl ec -in jwt-private.pem -pubout -out jwt-public.pem
> ```

### PostgreSQL 配置

`postgres` 配置块用于数据库连接和连接池。

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `postgres.host` | `TIMEFLOW_POSTGRES_HOST` | 数据库主机 |
| `postgres.port` | `TIMEFLOW_POSTGRES_PORT` | 数据库端口 |
| `postgres.database` | `TIMEFLOW_POSTGRES_DATABASE` | 数据库名称 |
| `postgres.user` | `TIMEFLOW_POSTGRES_USER` | 数据库用户名 |
| `postgres.password` | `TIMEFLOW_POSTGRES_PASSWORD` | 数据库密码 |
| `postgres.maximumPoolSize` | `TIMEFLOW_POSTGRES_MAXIMUM_POOL_SIZE` | 连接池最大连接数 |

生产环境建议为数据库账户设置最小权限，并避免使用默认空密码。

### 邮件与验证码

`email` 配置块用于注册验证码邮件发送。

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `email.verificationEnabled` | `TIMEFLOW_EMAIL_VERIFICATION_ENABLED` | 是否启用邮箱验证码注册 |
| `email.host` | `TIMEFLOW_EMAIL_HOST` | SMTP 服务器地址 |
| `email.port` | `TIMEFLOW_EMAIL_PORT` | SMTP 端口 |
| `email.username` | `TIMEFLOW_EMAIL_USERNAME` | SMTP 登录用户名 |
| `email.password` | `TIMEFLOW_EMAIL_PASSWORD` | SMTP 登录密码或授权码 |
| `email.from` | `TIMEFLOW_EMAIL_FROM` | 发件人邮箱地址 |
| `email.ssl` | `TIMEFLOW_EMAIL_SSL` | 是否启用 SSL 直连 |
| `email.codeExpirationMinutes` | `TIMEFLOW_EMAIL_CODE_EXPIRATION_MINUTES` | 验证码有效期，单位为分钟 |

如果 `verificationEnabled` 设为 `false`，注册流程中将不再要求验证码。

### Turnstile 配置

`turnstile` 配置块用于 Cloudflare Turnstile 人机校验。

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `turnstile.enabled` | `TIMEFLOW_TURNSTILE_ENABLED` | 是否启用 Turnstile |
| `turnstile.secretKey` | `TIMEFLOW_TURNSTILE_SECRET_KEY` | Turnstile 服务端密钥 |
| `turnstile.siteVerifyUrl` | `TIMEFLOW_TURNSTILE_SITE_VERIFY_URL` | 校验接口地址 |

启用后，相关注册或验证码接口将要求客户端提交 Turnstile token，具体接口行为请查看 OpenAPI 文档。

### HTTP 与安全策略

`http` 配置块用于反向代理适配和浏览器安全策略。

#### 代理头

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `http.forwardedHeaders.enabled` | `TIMEFLOW_HTTP_FORWARDED_HEADERS_ENABLED` | 是否启用标准 `Forwarded` 头支持 |
| `http.xForwardedHeaders.enabled` | `TIMEFLOW_HTTP_X_FORWARDED_HEADERS_ENABLED` | 是否启用 `X-Forwarded-*` 头支持 |

如果服务部署在 Nginx、Caddy、Traefik 或 Cloudflare 后面，通常需要按实际代理行为启用其中一项或两项。

#### CORS

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `http.cors.enabled` | `TIMEFLOW_HTTP_CORS_ENABLED` | 是否启用 CORS |
| `http.cors.anyHost` | `TIMEFLOW_HTTP_CORS_ANY_HOST` | 是否允许任意来源 |
| `http.cors.allowedHosts` | `TIMEFLOW_HTTP_CORS_ALLOWED_HOSTS` | 允许的来源列表 |
| `http.cors.allowCredentials` | `TIMEFLOW_HTTP_CORS_ALLOW_CREDENTIALS` | 是否允许携带凭据 |

#### HSTS

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `http.hsts.enabled` | `TIMEFLOW_HTTP_HSTS_ENABLED` | 是否启用 HSTS |
| `http.hsts.includeSubDomains` | `TIMEFLOW_HTTP_HSTS_INCLUDE_SUB_DOMAINS` | 是否对子域名生效 |
| `http.hsts.preload` | `TIMEFLOW_HTTP_HSTS_PRELOAD` | 是否声明允许加入 preload 列表 |
| `http.hsts.maxAgeInSeconds` | `TIMEFLOW_HTTP_HSTS_MAX_AGE_IN_SECONDS` | HSTS 缓存时长 |

#### HTTPS 重定向

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `http.httpsRedirect.enabled` | `TIMEFLOW_HTTP_HTTPS_REDIRECT_ENABLED` | 是否启用 HTTP 到 HTTPS 重定向 |
| `http.httpsRedirect.sslPort` | `TIMEFLOW_HTTP_HTTPS_REDIRECT_SSL_PORT` | HTTPS 端口 |
| `http.httpsRedirect.permanentRedirect` | `TIMEFLOW_HTTP_HTTPS_REDIRECT_PERMANENT_REDIRECT` | 是否使用永久重定向 |

### 同步配置

`sync` 配置块当前用于控制课程表同步配额。

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `sync.scheduleQuota` | `TIMEFLOW_SYNC_SCHEDULE_QUOTA` | 每个用户允许同步的最大课程表总数，包含软删除数据 |

### AI 配置

`ai` 配置块用于课程表图片识别功能。

#### 基础配置

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `ai.enabled` | `TIMEFLOW_AI_ENABLED` | 是否启用 AI 提取功能 |
| `ai.maxImageSizeBytes` | `TIMEFLOW_AI_MAX_IMAGE_SIZE_BYTES` | 图片最大字节数，超出时自动缩放 |
| `ai.maxImageResolution` | `TIMEFLOW_AI_MAX_IMAGE_RESOLUTION` | 图片最大分辨率，超出时自动缩放 |
| `ai.quotaPerHalfYear` | `TIMEFLOW_AI_QUOTA_PER_HALF_YEAR` | 每用户每 6 个月默认配额 |

#### 提供商配置

`ai.providers` 下的每个子项代表一个 LLM 提供商，键名可自定义，例如 `nvidia`、`openrouter`、`openai`。

| 字段 | 说明 |
|:---|:---|
| `format` | 协议格式，支持 `openai`、`openrouter`、`google`、`anthropic`、`ollama` |
| `apiKey` | 提供商 API 密钥 |
| `endpoint` | 可选，自定义 API 地址 |

#### 模型配置

`ai.models` 用于声明可用模型池。

| 字段 | 说明 |
|:---|:---|
| `id` | 模型 ID |
| `provider` | 引用的提供商名称 |
| `weight` | 选择权重，越大越容易被选中 |
| `rpm` | 每分钟请求上限，`0` 表示不限 |
| `rpd` | 每天请求上限，`0` 表示不限 |

服务会按权重选择模型；如果某个模型失败，会继续尝试其他可用模型。

### Web 静态包托管

`webApp` 配置块用于让后端直接托管前端 Web 产物。

| 配置项 | 环境变量 | 说明 |
|:---|:---|:---|
| `webApp.serveEnabled` | `TIMEFLOW_WEB_APP_SERVE_ENABLED` | 是否启用 Web 静态资源托管 |
| `webApp.zipPath` | `TIMEFLOW_WEB_APP_ZIP_PATH` | 自定义 Web ZIP 路径；留空时优先使用内置资源 |
| `webApp.location` | `TIMEFLOW_WEB_APP_LOCATION` | Web 应用挂载路径，例如 `app` 对应 `/app/` |

如果希望在根路径直接提供 Web 应用，可将 `location` 设为 `/`。
