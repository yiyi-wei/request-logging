# Request Logging SDK for Spring Boot 3

一个用于记录HTTP请求/响应信息和异常日志的日志管理工具。

## 特性

- 完整的请求日志记录：记录请求方法、URI、查询参数、客户端IP、请求头和请求体
- 响应日志记录：记录响应状态码和响应体
- 异常捕获：自动捕获并记录请求处理过程中的异常
- 高度可配置：支持自定义日志格式、日志级别、采样率等
- 框架兼容：专为Spring Boot 3设计，完全兼容Spring MVC和Spring WebFlux
- 分布式支持：通过Trace ID关联同一请求链路的所有日志
- 日志文件管理：支持按日期或大小滚动的日志文件策略
- 高级特性：异步日志记录、采样控制等

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>ltd.weiyiyi</groupId>
    <artifactId>request-logging-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 启用日志功能

在Spring Boot应用的主类上添加`@EnableRequestLogging`注解：

```java
@SpringBootApplication
@EnableRequestLogging
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 配置

在`application.yml`或`application.properties`中配置：

```yaml
request-logging:
  # 基本配置
  enabled: true                    # 是否启用日志功能
  log-level: INFO                  # 日志级别

  # 日志内容配置
  log-headers: true               # 是否记录请求头
  log-request-body: true          # 是否记录请求体
  log-response: true              # 是否记录响应内容
  request-body-max-length: 500    # 请求体最大长度
  response-max-length: 500        # 响应内容最大长度
  exclude-headers:                # 需要排除的请求头
    - Authorization
    - Cookie
  exclude-parameters:             # 需要排除的请求参数
    - password
    - token

  # 美化打印配置
  pretty-print: true              # 是否启用美化打印
  separator: "###################" # 日志分隔符
  request-start-flag: ">>> Request Start >>>"  # 请求开始标记
  request-end-flag: "<<< Request End <<<"      # 请求结束标记
  request-error-flag: "--- Request Error ---"  # 请求错误标记
  show-timestamp: true           # 是否显示时间戳
  timestamp-format: "yyyy-MM-dd HH:mm:ss.SSS"  # 时间戳格式
  show-separator: true           # 是否显示分隔符
  json-indent: 2                 # JSON缩进空格数
  separate-log-level: true       # 是否区分请求和响应的日志级别

  # 颜色输出配置
  enable-color-output: true      # 是否启用颜色输出

  # 日志级别颜色配置
  # INFO级别 - 绿色文字
  info-foreground-color: "(0,255,0)"
  info-background-color: null

  # WARN级别 - 橙色文字
  warn-foreground-color: "(255,165,0)"
  warn-background-color: null

  # ERROR级别 - 红色文字，浅红色背景
  error-foreground-color: "(255,0,0)"
  error-background-color: "(255,200,200)"

  # DEBUG级别 - 深天蓝色文字
  debug-foreground-color: "(0,191,255)"
  debug-background-color: null

  # TRACE级别 - 灰色文字
  trace-foreground-color: "(128,128,128)"
  trace-background-color: null

  # 请求开始标记 - 绿色文字，浅绿色背景
  request-start-foreground-color: "(0,255,0)"
  request-start-background-color: "(200,255,200)"

  # 请求结束标记 - 蓝色文字，浅蓝色背景
  request-end-foreground-color: "(0,0,255)"
  request-end-background-color: "(200,200,255)"

  # 重置颜色
  reset-color: "\u001B[0m"

  # 文件日志配置
  enable-file-logging: true       # 是否启用文件日志
  log-file-base-dir: data         # 日志文件基础目录
  log-file-strategy: daily        # 日志文件策略：daily/size
  max-file-size: 100MB            # 单个文件大小限制（size策略）
  max-history: 30                 # 日志保留天数（daily策略）

  # 性能相关配置
  sampling-rate: 100             # 采样率（0-100）
  async-logging: true            # 是否启用异步日志
  async-core-pool-size: 2        # 异步线程池核心线程数
  async-max-pool-size: 5         # 异步线程池最大线程数
  async-queue-capacity: 100      # 异步线程池队列容量
  enable-object-pool: true       # 是否启用对象池
  object-pool-max-size: 200      # 对象池最大容量
```

## 日志格式

### 请求开始日志
```
###################
>>> Request Start >>>
Timestamp      : 2025-01-13T14:03:29.351+08:00
HTTP Method    : GET
Endpoint       : /api/v1/login
Full URL       : https://example.com/api/v1/login?id=123&type=abc
Client IP      : 192.168.1.100
Authentication : Bearer eyJhbGciOiJ...*** (部分掩码)
Trace ID       : f43d8e2c-bc91-48f5-8d12-0a3e5d174b3c

Headers        :
  - Host       : example.com
  - User-Agent : Mozilla/5.0 (Windows NT 10.0; Win64; x64)
  - Accept     : application/json
  - Content-Type: application/json
  - X-Forwarded-For: 10.0.0.1

Query Params   :
  - id         : 123
  - type       : abc

Request Body   : 
{
  "username": "john.doe",
  "password": "*****"  # 敏感信息掩码
}

Service Instance:
  - Instance ID: service-instance-1
  - Host       : 10.1.0.5
###################
```

### 请求结束日志
```
###################
<<< Request End <<<
Timestamp      : 2025-01-13T14:03:29.437+08:00
HTTP Status    : 200
Response Time  : 86ms
Trace ID       : f43d8e2c-bc91-48f5-8d12-0a3e5d174b3c

Headers        :
  - Content-Type : application/json
  - Cache-Control: no-cache
  - Set-Cookie   : session_id=abc123; Path=/; HttpOnly

Response Body  :
{
  "status": "success",
  "message": "Login successful",
  "data": {
    "userId": "u12345",
    "token": "eyJhbGciOiJIUz...***"
  }
}

Error Details  : None
###################
```

### 错误日志
```
###################
--- Request Error ---
Timestamp      : 2025-01-13T14:03:29.437+08:00
HTTP Status    : 500
Response Time  : 86ms
Trace ID       : f43d8e2c-bc91-48f5-8d12-0a3e5d174b3c

Headers        :
  - Content-Type : application/json
  - Cache-Control: no-cache

Error Details  :
  - Error Type   : AuthenticationException
  - Error Message: Invalid credentials provided.
  - Stack Trace  : 
        com.example.auth.AuthenticationException: Invalid credentials provided.
          at com.example.auth.AuthService.authenticate(AuthService.java:45)
          at com.example.controller.AuthController.login(AuthController.java:28)
          at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
          at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
          at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
          ...

Error Context  :
  - Request Context:
      - Endpoint     : /api/v1/login
      - Query Params : id=123&type=abc
  - System Context:
      - CPU Load     : 75%
      - Memory Usage : 1.2GB / 2GB
      - Thread Count : 15
      - Environment  : Production
###################
```

## 高级功能

### MDC跟踪

SDK支持通过MDC（Mapped Diagnostic Context）将Trace ID添加到应用中的所有日志中。只需配置`enable-mdc-trace`和`trace-packages`即可：

```yaml
request-logging:
  enable-mdc-trace: true
  trace-packages:
    - com.example.controller
    - com.example.service
```

这样，指定包路径下的所有日志都会自动包含Trace ID：

```
2024-01-01 12:00:00.000 [INFO] [trace-id] UserService - Processing user request
```

### 日志文件管理

SDK提供两种日志文件滚动策略：

1. 按日期滚动（daily）：每天创建一个新的日志文件
   ```yaml
   request-logging:
     rolling-strategy: daily
     max-history: 30  # 保留30天的日志文件
   ```

2. 按大小滚动（size）：当日志文件达到指定大小时创建新文件
   ```yaml
   request-logging:
     rolling-strategy: size
     max-file-size: 100  # 单个文件最大100MB
   ```

### 采样控制

通过配置采样率可以控制日志记录的频率：

```yaml
request-logging:
  sampling-rate: 0.1  # 只记录10%的请求
```

## 注意事项

1. 该SDK仅支持Spring Boot 3.x版本
2. 建议在开发环境启用完整日志，生产环境适当调整采样率
3. 日志文件路径请确保应用有写入权限
4. 控制台彩色日志在Windows终端可能显示不正常
