# Request Logging SDK for Spring Boot 3

## 产品概述

Request Logging SDK 是一个基于 Spring Boot 3 的开发工具，用于方便其他项目引入并启用统一的 HTTP 请求日志记录功能。通过该 SDK，开发者可以快速实现以下功能：

1. **请求信息记录**：包括请求 IP 地址、代理信息、目标接口、请求参数等。
2. **响应信息记录**：包括响应结果（可配置是否打印）、HTTP 状态码、响应时间。
3. **异常信息捕获**：记录请求期间抛出的异常，包含异常类型、堆栈跟踪、错误代码位置等。
4. **可定制性**：允许用户基于配置动态启用/禁用日志功能，调整日志级别或自定义日志格式。
5. **轻量化与高性能**：在不影响业务系统性能的前提下，提供易于集成和扩展的功能。

## 功能描述

### 1. 全局请求日志记录

- 打印进入请求时的信息：
  - 请求者 IP 地址。
  - 请求头（支持指定过滤敏感信息）。
  - 代理信息（如 `X-Forwarded-For` 等）。
  - 请求 URL 和 HTTP 方法。
  - 请求参数（支持排除敏感字段）。
- 响应完成后打印：
  - HTTP 状态码。
  - 响应时间（以毫秒为单位）。
  - 响应结果（支持开关）。
- 异常时打印：
  - 异常类型。
  - 异常消息。
  - 异常堆栈及代码位置。

### 2. 日志格式

- JSON 格式化日志输出，方便后续集成日志收集工具（如 ELK 或 Graylog）。
- 可选的纯文本格式。

### 3. 性能优化

- 异步日志输出，减少对主线程的性能影响。
- 支持通过配置决定是否采样部分请求日志。

### 4. 配置功能

- 动态启用/禁用日志记录功能。
- 自定义日志级别（DEBUG、INFO、WARN、ERROR）。
- 可配置敏感字段的自动屏蔽。
- 支持响应结果内容长度限制。

### 5. 扩展性

- 支持通过 SPI 机制扩展日志功能。
- 提供自定义拦截器接口。

## 技术架构

- 基于 Spring Boot 3 构建。
- 使用 Spring AOP 实现全局日志拦截。
- 使用 `Slf4j` 作为日志接口，支持多种日志实现（如 Logback 或 Log4j2）。
- 支持通过 Spring Configuration 注入自定义配置。

## 目标用户

- 使用 Spring Boot 作为技术栈的开发团队。
- 需要统一标准化日志管理的项目。
- 希望降低开发人员日志开发和维护成本的企业。

## 未来建议

1. **日志采样功能**：适用于高流量场景，通过配置实现对部分请求日志的采样。
2. **安全增强**：支持自动检测并掩盖敏感信息（如身份证号、信用卡号等）。
3. **集成追踪 ID**：自动生成并记录唯一的请求追踪 ID，用于分布式系统的日志关联。
4. **Webhook 通知**：支持将异常日志推送至指定的 Webhook，便于实时监控。
5. **链路追踪集成**：与分布式链路追踪工具（如 Zipkin、Jaeger）集成，提供更全面的请求日志上下文。