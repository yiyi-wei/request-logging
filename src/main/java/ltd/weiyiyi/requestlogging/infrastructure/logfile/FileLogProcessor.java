package ltd.weiyiyi.requestlogging.infrastructure.logfile;

import com.alibaba.fastjson2.JSON;
import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import ltd.weiyiyi.requestlogging.infrastructure.spi.LogFileStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件日志处理器
 * 将请求日志写入文件
 *
 * @author weihan
 */
public class FileLogProcessor implements RequestLogProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FileLogProcessor.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private RequestLoggingProperties properties;
    private Path logDir;
    private LogFileStrategy logFileStrategy;

    @Override
    public void init(RequestLoggingProperties properties) {
        this.properties = properties;
        if (!properties.isEnableFileLogging()) {
            return;
        }

        try {
            // 创建日志目录
            this.logDir = Paths.get(properties.getLogFileBaseDir());
            Files.createDirectories(logDir);

            // 初始化日志文件策略
            this.logFileStrategy = createLogFileStrategy();
            if (this.logFileStrategy != null) {
                this.logFileStrategy.init(logDir.toString());
            }
        } catch (IOException e) {
            logger.error("Failed to initialize file log processor", e);
        }
    }

    @Override
    public void process(RequestLog log) {
        if (!properties.isEnableFileLogging() || logFileStrategy == null) {
            return;
        }

        try {
            File logFile = logFileStrategy.getLogFile(log);
            if (logFile != null) {
                writeLog(logFile, formatLogMessage(log));
            }
        } catch (Exception e) {
            logger.error("Failed to write log to file", e);
        }
    }

    @Override
    public void processRequestError(RequestLog log) {
        if (!properties.isEnableFileLogging() || logFileStrategy == null) {
            return;
        }

        try {
            File logFile = logFileStrategy.getLogFile(log);
            if (logFile != null) {
                writeLog(logFile, formatErrorLogMessage(log));
            }
        } catch (Exception e) {
            logger.error("Failed to write error log to file", e);
        }
    }

    private LogFileStrategy createLogFileStrategy() {
        return switch (properties.getLogFileStrategy().toLowerCase()) {
            case "daily" -> new DailyRollingStrategy(properties.getMaxHistory());
            case "size" -> new SizeBasedRollingStrategy(properties.getMaxFileSize(), properties.getObjectPoolMaxSize());
            default -> {
                logger.warn("Unknown log file strategy: {}, using daily strategy", properties.getLogFileStrategy());
                yield new DailyRollingStrategy(properties.getMaxHistory());
            }
        };
    }

    private void writeLog(File file, String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(content);
        } catch (IOException e) {
            logger.error("Failed to write to log file: " + file.getAbsolutePath(), e);
        }
    }

    private String formatLogMessage(RequestLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(properties.getSeparator()).append("\n");
        sb.append(properties.getRequestStartFlag()).append("\n");
        appendCommonInfo(sb, log);
        sb.append("\n").append(properties.getSeparator());
        return sb.toString();
    }

    private String formatErrorLogMessage(RequestLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(properties.getSeparator()).append("\n");
        sb.append(properties.getRequestErrorFlag()).append("\n");
        appendCommonInfo(sb, log);
        
        if (log.getException() != null) {
            sb.append("Exception     : ").append(log.getException()).append("\n");
            sb.append("Message       : ").append(log.getExceptionMessage()).append("\n");
            if (log.getStackTrace() != null) {
                sb.append("Stack Trace   :\n").append(log.getStackTrace());
            }
        }
        
        sb.append("\n").append(properties.getSeparator());
        return sb.toString();
    }

    private void appendCommonInfo(StringBuilder sb, RequestLog log) {
        LocalDateTime timestamp = log.getRequestTime();
        if (timestamp != null && properties.isShowTimestamp()) {
            sb.append("Date          : ").append(timestamp.format(DATE_FORMATTER)).append("\n");
            sb.append("Time          : ").append(timestamp.format(TIME_FORMATTER)).append("\n");
        }
        
        sb.append("Trace ID      : ").append(log.getTraceId()).append("\n");
        sb.append("Method        : ").append(log.getMethod()).append("\n");
        sb.append("URI           : ").append(log.getUri()).append("\n");
        
        if (log.getQueryString() != null) {
            sb.append("Query String  : ").append(log.getQueryString()).append("\n");
        }
        
        sb.append("Client IP     : ").append(log.getClientIp()).append("\n");
        
        if (properties.isLogHeaders() && log.getHeaders() != null) {
            sb.append("Headers       :\n");
            log.getHeaders().forEach((key, value) -> {
                if (!java.util.Arrays.asList(properties.getExcludeHeaders()).contains(key)) {
                    sb.append("  ").append(key).append(": ").append(value).append("\n");
                }
            });
        }
        
        if (properties.isLogRequestBody() && log.getRequestBody() != null) {
            sb.append("Request Body  :\n");
            if (properties.isJsonPrettyPrint()) {
                try {
                    Object jsonObject = JSON.parse(log.getRequestBody());
                    sb.append(JSON.toJSONString(jsonObject, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat));
                } catch (Exception e) {
                    sb.append(log.getRequestBody());
                }
            } else {
                sb.append(log.getRequestBody());
            }
            sb.append("\n");
        }
        
        if (properties.isLogResponse()) {
            sb.append("Status        : ").append(log.getStatus()).append("\n");
            if (log.getResponseBody() != null) {
                sb.append("Response Body :\n");
                if (properties.isJsonPrettyPrint()) {
                    try {
                        Object jsonObject = JSON.parse(log.getResponseBody());
                        sb.append(JSON.toJSONString(jsonObject, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat));
                    } catch (Exception e) {
                        sb.append(log.getResponseBody());
                    }
                } else {
                    sb.append(log.getResponseBody());
                }
                sb.append("\n");
            }
        }

        if (log.getSystemMetrics() != null) {
            sb.append("System Metrics:\n");
            log.getSystemMetrics().forEach((key, value) -> 
                sb.append("  ").append(key).append(": ").append(value).append("\n")
            );
        }
    }

    @Override
    public void close() {
        if (logFileStrategy != null) {
            logFileStrategy.cleanup();
        }
    }
} 