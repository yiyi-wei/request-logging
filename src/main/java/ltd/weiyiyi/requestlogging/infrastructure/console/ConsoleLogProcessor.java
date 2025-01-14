package ltd.weiyiyi.requestlogging.infrastructure.console;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

/**
 * 控制台日志处理器
 * 将请求日志输出到控制台
 *
 * @author weihan
 */
public class ConsoleLogProcessor implements RequestLogProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleLogProcessor.class);

    private RequestLoggingProperties properties;

    @Override
    public void init(RequestLoggingProperties properties) {
        this.properties = properties;
    }

    @Override
    public void process(RequestLog log) {
        String logMessage = formatLogMessage(log);
        switch (properties.getLogLevel().toUpperCase()) {
            case "ERROR" -> logger.error(logMessage);
            case "WARN" -> logger.warn(logMessage);
            case "DEBUG" -> logger.debug(logMessage);
            case "TRACE" -> logger.trace(logMessage);
            default -> logger.info(logMessage);
        }
    }

    @Override
    public void processRequestError(RequestLog log) {
        String logMessage = formatErrorLogMessage(log);
        logger.error(logMessage);
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
        if (properties.isShowTimestamp()) {
            sb.append("Timestamp     : ").append(log.getRequestTime()).append("\n");
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
                if (!Arrays.asList(properties.getExcludeHeaders()).contains(key)) {
                    sb.append("  ").append(key).append(": ").append(value).append("\n");
                }
            });
        }
        
        if (properties.isLogRequestBody() && log.getRequestBody() != null) {
            sb.append("Request Body  :\n").append(log.getRequestBody()).append("\n");
        }
        
        if (properties.isLogResponse()) {
            sb.append("Status        : ").append(log.getStatus()).append("\n");
            if (log.getResponseBody() != null) {
                sb.append("Response Body :\n").append(log.getResponseBody()).append("\n");
            }
        }

        if (log.getSystemMetrics() != null) {
            sb.append("System Metrics:\n");
            log.getSystemMetrics().forEach((key, value) -> 
                sb.append("  ").append(key).append(": ").append(value).append("\n")
            );
        }
    }
} 