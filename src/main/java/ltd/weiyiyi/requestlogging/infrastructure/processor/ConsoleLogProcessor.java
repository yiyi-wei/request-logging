package ltd.weiyiyi.requestlogging.infrastructure.processor;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;
import ltd.weiyiyi.requestlogging.infrastructure.formatter.LogFormatter;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 控制台日志处理器
 */
public class ConsoleLogProcessor implements RequestLogProcessor {
    private static final Logger log = LoggerFactory.getLogger(ConsoleLogProcessor.class);
    private RequestLoggingProperties properties;
    private LogFormatter logFormatter;

    @Override
    public void init(RequestLoggingProperties properties) {
        this.properties = properties;
        this.logFormatter = new LogFormatter(properties);
    }

    @Override
    public void processRequestStart(RequestLog requestLog) {
        if (properties.isEnableConsoleLogging()) {
            log.info(logFormatter.logRequestStart(requestLog));
        }
    }

    @Override
    public void processRequestComplete(RequestLog requestLog) {
        if (properties.isEnableConsoleLogging()) {
            log.info(logFormatter.logRequestComplete(requestLog));
        }
    }

    @Override
    public void processRequestError(RequestLog requestLog) {
        if (properties.isEnableConsoleLogging()) {
            log.error(logFormatter.logRequestError(requestLog));
        }
    }
} 