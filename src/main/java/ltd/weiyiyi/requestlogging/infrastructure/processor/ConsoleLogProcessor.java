package ltd.weiyiyi.requestlogging.infrastructure.processor;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;
import ltd.weiyiyi.requestlogging.infrastructure.formatter.LogFormatter;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import ltd.weiyiyi.requestlogging.infrastructure.util.SystemMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 控制台日志处理器
 * 将请求日志输出到控制台
 *
 * @author weihan
 */
public class ConsoleLogProcessor implements RequestLogProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleLogProcessor.class);

    private RequestLoggingProperties properties;
    private LogFormatter logFormatter;
    private final SystemMetricsCollector systemMetricsCollector = new SystemMetricsCollector();

    public ConsoleLogProcessor() {
        // 无参构造函数，用于SPI加载
    }

    @Override
    public void init(RequestLoggingProperties properties) {
        this.properties = properties;
        this.logFormatter = new LogFormatter(properties, systemMetricsCollector);
    }

    @Override
    public void process(RequestLog log) {
        String logMessage;
        if (log.getException() != null) {
            logMessage = logFormatter.logRequestError(log);
            logger.error(logMessage);
        } else if (log.getResponseTime() != null) {
            logMessage = logFormatter.logRequestComplete(log);
            logWithLevel(logMessage);
        } else {
            logMessage = logFormatter.logRequestStart(log);
            logWithLevel(logMessage);
        }
    }

    private void logWithLevel(String message) {
        String logLevel = properties.getLogLevel().toUpperCase();
        switch (logLevel) {
            case "ERROR" -> logger.error(message);
            case "WARN" -> logger.warn(message);
            case "DEBUG" -> logger.debug(message);
            case "TRACE" -> logger.trace(message);
            default -> logger.info(message);
        }
    }
} 