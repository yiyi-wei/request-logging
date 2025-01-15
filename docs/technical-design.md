# Request Logging SDK 技术设计文档

## 1. 架构设计

### 1.1 DDD分层架构

采用DDD（领域驱动设计）的分层架构：

```
ltd.weiyiyi.requestlogging
├── application        // 应用层：协调领域对象和领域服务
│   └── service       // 应用服务
├── domain            // 领域层：核心业务逻辑
│   ├── model        // 领域模型
│   ├── service      // 领域服务
│   └── repository   // 仓储接口
├── infrastructure    // 基础设施层：技术细节实现
│   ├── config       // 配置类
│   ├── filter       // 过滤器
│   ├── aspect       // AOP切面
│   ├── logfile      // 日志文件策略实现
│   └── spi          // SPI扩展
└── interfaces        // 接口层：对外暴露的接口
    └── facade       // 外观模式，统一接口
```

### 1.2 核心组件

1. **RequestLoggingFilter**
   - 位于基础设施层
   - 实现请求/响应包装和日志记录
   - 使用装饰器模式包装HttpServletRequest和HttpServletResponse

2. **RequestLoggingAspect**
   - 位于基础设施层
   - 使用AOP处理Controller层异常
   - 实现统一的异常日志记录

3. **RequestLoggingService**
   - 位于应用层
   - 协调领域服务和基础设施服务
   - 实现日志记录的核心业务逻辑

## 2. 高级特性设计

### 2.1 SPI扩展机制

#### 设计目标
- 允许用户自定义日志处理逻辑
- 支持可插拔的日志存储方式
- 提供灵活的日志格式化能力

#### 实现方案
1. 日志处理器SPI：
```java
public interface RequestLogProcessor {
    void process(RequestLog log);
}
```

2. 日志文件策略SPI：
```java
public interface LogFileStrategy {
    File getLogFile(RequestLog log);
    String getStrategyName();
    void init(String baseDir);
    void cleanup();
}
```

3. 配置SPI加载：
```
META-INF/services/ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor
META-INF/services/ltd.weiyiyi.requestlogging.infrastructure.spi.LogFileStrategy
```

### 2.2 日志文件策略

#### 内置策略

1. **按天滚动策略（DailyRollingStrategy）**
   - 每天生成一个新的日志文件
   - 文件名格式：request-yyyy-MM-dd.log
   - 支持配置保留天数
   - 自动清理过期日志文件

2. **按大小滚动策略（SizeBasedRollingStrategy）**
   - 当文件达到指定大小时创建新文件
   - 文件名格式：request-yyyy-MM-dd-sequence.log
   - 支持配置单个文件大小限制
   - 支持配置文件数量限制

#### 自定义策略
用户可以通过实现LogFileStrategy接口来自定义日志文件策略：

```java
public class CustomLogFileStrategy implements LogFileStrategy {
    @Override
    public File getLogFile(RequestLog log) {
        // 自定义逻辑
    }

    @Override
    public String getStrategyName() {
        return "custom";
    }

    // ...其他方法实现
}
```

### 2.3 异步日志处理

#### 设计目标
- 降低日志记录对主业务流程的影响
- 提供可配置的异步策略
- 确保日志不丢失

#### 实现方案
1. 使用Spring的@Async注解
2. 配置专用的异步线程池
3. 实现优雅关闭机制

### 2.4 采样策略

#### 设计目标
- 支持灵活的采样规则
- 在高并发场景下降低系统负载
- 保证关键请求必定记录

#### 实现方案
1. 基于请求特征的采样
2. 支持配置采样率
3. 提供白名单机制

### 2.5 日志美化

#### 设计目标
- 提供美观、易读的日志格式
- 支持自定义日志样式
- 区分请求开始和结束日志
- 支持JSON格式化

#### 实现方案
1. 配置项：
```yaml
request-logging:
  pretty-print: true                 # 是否启用美化打印
  separator: "===================="   # 日志分隔符
  request-start-flag: ">>> Request Start >>>"  # 请求开始标记
  request-end-flag: "<<< Request End <<<"      # 请求结束标记
  show-timestamp: true              # 是否显示时间戳
  timestamp-format: "yyyy-MM-dd HH:mm:ss.SSS"  # 时间戳格式
  json-indent: 2                    # JSON缩进空格数
  show-separator: true              # 是否显示分隔符
  show-sequence-number: true        # 是否显示请求序号
  separate-log-level: true          # 是否区分请求和响应的日志级别
```

2. 日志格式示例：
```
====================
[2024-01-09 15:30:45.123] [1] >>> Request Start >>>
Method: POST
URI: /api/users?type=new
Client IP: 192.168.1.1
Headers:
  user-agent: Mozilla/5.0
  content-type: application/json
Request Body:
{
  "username": "test",
  "email": "test@example.com"
}
Trace ID: 7b6fe19a8f3e4d5c9b2a1d8e7c6f3b2a
====================

====================
[2024-01-09 15:30:45.456] [1] <<< Request End <<<
Status: 200
Processing Time: 333ms
Response Body:
{
  "id": 1,
  "username": "test",
  "created_at": "2024-01-09T15:30:45Z"
}
====================
```

3. 格式化工具：
- LogFormatter类负责日志格式化
- 支持JSON内容的美化
- 使用StringBuilder提高性能
- 支持自定义日志样式

4. 性能优化：
- 使用ThreadLocal缓存日期格式化器
- 可配置是否启用美化功能
- JSON格式化采用延迟处理

## 3. 测试设计

### 3.1 单元测试

#### 测试范围
1. 日志文件策略测试
   - 文件创建和滚动
   - 并发访问
   - 清理机制
2. 日志处理逻辑测试
   - 请求信息记录
   - 响应信息记录
   - 异常捕获
3. 配置加载测试
   - 属性注入
   - 条件装配

#### 测试工具
- JUnit 5
- Spring Boot Test
- Mockito
- AssertJ

### 3.2 测试覆盖率

使用JaCoCo进行测试覆盖率统计：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <!-- ... 配置详情 ... -->
</plugin>
```

#### 覆盖率要求
- 行覆盖率：>= 80%
- 分支覆盖率：>= 70%
- 方法覆盖率：>= 80%

### 3.3 测试执行

1. 运行所有测试：
```bash
mvn clean test
```

2. 生成测试报告：
```bash
mvn jacoco:report
```

3. 查看测试报告：
```
target/site/jacoco/index.html
```

## 4. 性能优化

### 4.1 内存优化
- 使用对象池复用对象
- 控制日志内容大小
- 采用异步批量处理

### 4.2 CPU优化
- 采用采样策略减少处理量
- 使用高效的JSON序列化
- 优化正则表达式匹配

## 5. 安全考虑

### 5.1 数据脱敏
- 支持配置敏感字段
- 提供默认脱敏规则
- 允许自定义脱敏策略

### 5.2 访问控制
- 日志访问权限控制
- 敏感信息加密存储
- 审计日志记录

## 6. 使用指南

### 6.1 基本使用

1. 添加依赖：
```xml
<dependency>
    <groupId>ltd.weiyiyi</groupId>
    <artifactId>request-logging-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. 启用功能：
```java
@SpringBootApplication
@EnableRequestLogging
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 6.2 配置说明

```yaml
request-logging:
  # 基本配置
  enabled: true                    # 是否启用日志功能
  log-level: INFO                  # 日志级别
  log-file-strategy: daily        # 日志文件策略：daily/size
  log-file-base-dir: logs         # 日志文件基础目录
  max-file-size: 100MB            # 单个文件大小限制（size策略）
  max-history: 30                 # 日志保留天数（daily策略）

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
  separator: "===================" # 日志分隔符
  request-start-flag: ">>> Request Start >>>"  # 请求开始标记
  request-end-flag: "<<< Request End <<<"      # 请求结束标记
  show-timestamp: true           # 是否显示时间戳
  timestamp-format: "yyyy-MM-dd HH:mm:ss.SSS"  # 时间戳格式
  json-indent: 2                 # JSON缩进空格数
  show-separator: true           # 是否显示分隔符
  show-sequence-number: true     # 是否显示请求序号
  separate-log-level: true       # 是否区分请求和响应的日志级别

  # 性能相关配置
  sampling-rate: 100             # 采样率（0-100）
  async-logging: true            # 是否启用异步日志
  async-core-pool-size: 2        # 异步线程池核心线程数
  async-max-pool-size: 5         # 异步线程池最大线程数
  async-queue-capacity: 100      # 异步线程池队列容量
  enable-object-pool: true       # 是否启用对象池
  object-pool-max-size: 200      # 对象池最大容量
```

### 6.3 自定义扩展

1. 自定义日志处理器：
```java
public class CustomLogProcessor implements RequestLogProcessor {
    @Override
    public void process(RequestLog log) {
        // 自定义处理逻辑
    }
}
```

2. 自定义日志文件策略：
```java
public class CustomFileStrategy implements LogFileStrategy {
    // 实现相关方法
}
```

## 7. 常见问题

### 7.1 性能影响
- 使用异步日志降低影响
- 配置合适的采样率
- 控制日志内容大小

### 7.2 磁盘空间
- 配置合理的文件大小限制
- 设置适当的日志保留期
- 启用自动清理机制

### 7.3 并发处理
- 使用线程安全的组件
- 实现并发访问控制
- 优化锁粒度
