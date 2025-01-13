package ltd.weiyiyi.requestlogging.infrastructure.console;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;
import ltd.weiyiyi.requestlogging.infrastructure.formatter.LogFormatter;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 控制台日志处理器
 * 负责将日志输出到控制台
 *
 * @author weihan
 */
public class ConsoleLogProcessor implements RequestLogProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleLogProcessor.class);
    private RequestLoggingProperties properties;
    private LogFormatter logFormatter;

    public ConsoleLogProcessor() {
        // 无参构造函数，用于SPI加载
    }

    public void init(RequestLoggingProperties properties) {
        this.properties = properties;
        this.logFormatter = new LogFormatter(properties);
    }

    @Override
    public void processRequestStart(RequestLog log) {
        if (properties == null || !properties.isEnableConsoleLogging()) {
            return;
        }

        String logContent = logFormatter.logRequestStart(log);
        logger.info(logContent);
    }

    @Override
    public void processRequestComplete(RequestLog log) {
        if (properties == null || !properties.isEnableConsoleLogging()) {
            return;
        }

        String logContent = logFormatter.logRequestComplete(log);
        logger.info(logContent);
    }

    @Override
    public void processRequestError(RequestLog log) {
        if (properties == null || !properties.isEnableConsoleLogging()) {
            return;
        }

        String logContent = logFormatter.logRequestError(log);
        logger.error(logContent);
    }
} 