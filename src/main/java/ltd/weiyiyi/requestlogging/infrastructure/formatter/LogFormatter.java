package ltd.weiyiyi.requestlogging.infrastructure.formatter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.color.ColorPair;
import ltd.weiyiyi.requestlogging.infrastructure.color.ColorProcessorFactory;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 日志格式化器
 */
public class LogFormatter {
    private final RequestLoggingProperties properties;
    private final ColorPair requestStartColor;
    private final ColorPair requestEndColor;
    private final ColorPair errorColor;
    private final DateTimeFormatter timestampFormatter;
    private final AtomicLong sequenceNumber = new AtomicLong(0);

    public LogFormatter(RequestLoggingProperties properties) {
        this.properties = properties;
        this.requestStartColor = new ColorPair(properties.getRequestStartColor(), null);
        this.requestEndColor = new ColorPair(properties.getRequestEndColor(), null);
        this.errorColor = new ColorPair(properties.getErrorColor(), null);
        this.timestampFormatter = DateTimeFormatter.ofPattern(properties.getTimestampFormat());
    }

    public String logRequestStart(RequestLog requestLog) {
        StringBuilder builder = new StringBuilder();
        if (properties.isShowSeparator()) {
            builder.append(properties.getSeparator()).append("\n");
        }
        
        appendTimestamp(builder);
        
        builder.append(formatBasicInfo(requestLog));
        builder.append(" ").append(properties.getRequestStartFlag());

        if (requestLog.getHeaders() != null) {
            builder.append("\nHeaders: ").append(formatJson(requestLog.getHeaders()));
        }

        if (requestLog.getRequestBody() != null) {
            builder.append("\nBody: ").append(formatJson(requestLog.getRequestBody()));
        }

        if (properties.isShowSeparator()) {
            builder.append("\n").append(properties.getSeparator());
        }

        return ColorProcessorFactory.processColor(requestStartColor, builder.toString());
    }

    public String logRequestComplete(RequestLog requestLog) {
        StringBuilder builder = new StringBuilder();
        if (properties.isShowSeparator()) {
            builder.append(properties.getSeparator()).append("\n");
        }
        
        appendTimestamp(builder);
        
        builder.append(formatBasicInfo(requestLog));
        builder.append(" ").append(properties.getRequestEndFlag());
        builder.append(" Status: ").append(requestLog.getStatus());
        builder.append(" Time: ").append(requestLog.getProcessingTime()).append("ms");

        if (requestLog.getResponseBody() != null) {
            builder.append("\nResponse: ").append(formatJson(requestLog.getResponseBody()));
        }

        if (properties.isShowSeparator()) {
            builder.append("\n").append(properties.getSeparator());
        }

        return ColorProcessorFactory.processColor(requestEndColor, builder.toString());
    }

    public String logRequestError(RequestLog requestLog) {
        StringBuilder builder = new StringBuilder();
        if (properties.isShowSeparator()) {
            builder.append(properties.getSeparator()).append("\n");
        }
        
        appendTimestamp(builder);
        
        builder.append(formatBasicInfo(requestLog));
        builder.append(" Request failed");
        builder.append(" Status: ").append(requestLog.getStatus());
        builder.append(" Time: ").append(requestLog.getProcessingTime()).append("ms");

        if (requestLog.getStackTrace() != null) {
            builder.append("\nError: ").append(requestLog.getStackTrace());
        }

        if (properties.isShowSeparator()) {
            builder.append("\n").append(properties.getSeparator());
        }

        return ColorProcessorFactory.processColor(errorColor, builder.toString());
    }

    private String formatBasicInfo(RequestLog requestLog) {
        StringBuilder builder = new StringBuilder();
        builder.append("[").append(requestLog.getTraceId()).append("]");
        builder.append(" [").append(requestLog.getMethod()).append("]");
        builder.append(" ").append(requestLog.getUri());

        if (requestLog.getQueryString() != null) {
            builder.append("?").append(requestLog.getQueryString());
        }

        return builder.toString();
    }

    private void appendTimestamp(StringBuilder builder) {
        if (properties.isShowTimestamp()) {
            builder.append("[").append(LocalDateTime.now().format(timestampFormatter)).append("] ");
        }
    }

    private String formatJson(Object obj) {
        if (obj == null) {
            return "";
        }

        if (!properties.isPrettyPrint()) {
            return obj.toString();
        }

        try {
            return JSON.toJSONString(obj, 
                JSONWriter.Feature.PrettyFormat,
                JSONWriter.Feature.WriteMapNullValue,
                JSONWriter.Feature.WriteNullListAsEmpty)
                .replaceAll("(?m)^(\\s+)", String.join("", Collections.nCopies(properties.getJsonIndent(), " ")));
        } catch (Exception e) {
            return obj.toString();
        }
    }
} 