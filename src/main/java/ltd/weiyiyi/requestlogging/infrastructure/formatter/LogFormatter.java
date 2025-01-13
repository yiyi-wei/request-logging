package ltd.weiyiyi.requestlogging.infrastructure.formatter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.color.ColorPair;
import ltd.weiyiyi.requestlogging.infrastructure.color.ColorProcessorFactory;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
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

    public LogFormatter(RequestLoggingProperties properties) {
        this.properties = properties;
        this.requestStartColor = new ColorPair(properties.getRequestStartColor(), null);
        this.requestEndColor = new ColorPair(properties.getRequestEndColor(), null);
        this.errorColor = new ColorPair(properties.getErrorColor(), null);
        this.timestampFormatter = DateTimeFormatter.ofPattern(properties.getTimestampFormat())
            .withZone(ZoneId.systemDefault());
    }

    public String logRequestStart(RequestLog requestLog) {
        StringBuilder builder = new StringBuilder();
        
        // Separator
        builder.append(properties.getRequestStartFlag()).append("\n");

        // Request Start Flag
        builder.append(properties.getRequestStartFlag()).append("\n");
        
        // Basic Info
        builder.append(String.format("Timestamp      : %s\n", LocalDateTime.now().format(timestampFormatter)));
        builder.append(String.format("HTTP Method    : %s\n", requestLog.getMethod()));
        builder.append(String.format("Endpoint       : %s\n", requestLog.getUri()));
        builder.append(String.format("Full URL       : %s\n", buildFullUrl(requestLog)));
        builder.append(String.format("Client IP      : %s\n", requestLog.getClientIp()));
        
        // Authentication (if available)
        if (requestLog.getHeaders() != null && requestLog.getHeaders().containsKey("Authorization")) {
            String auth = requestLog.getHeaders().get("Authorization");
            builder.append(String.format("Authentication : %s\n", maskSensitiveData(auth)));
        }
        
        builder.append(String.format("Trace ID       : %s\n", requestLog.getTraceId()));

        // Headers
        if (properties.isLogHeaders() && requestLog.getHeaders() != null && !requestLog.getHeaders().isEmpty()) {
            builder.append("Headers        :\n");
            formatHeaders(builder, requestLog.getHeaders());
        }
        
        // Query Parameters
        if (requestLog.getQueryString() != null && !requestLog.getQueryString().isEmpty()) {
            builder.append("\nQuery Params   :\n");
            formatQueryParams(builder, requestLog.getQueryString());
        }
        
        // Request Body
        if (properties.isLogRequestBody() && requestLog.getRequestBody() != null) {
            builder.append("\nRequest Body   : \n");
            builder.append(formatJson(requestLog.getRequestBody()));
        }
        
        // Service Instance
        builder.append("\nService Instance:\n");
        builder.append(String.format("  - Instance ID: %s\n", getInstanceId()));
        builder.append(String.format("  - Host       : %s\n", getHostInfo()));
        
        // Separator
        //builder.append("###################");
        builder.append(this.properties.getSeparator());

        return ColorProcessorFactory.processColor(requestStartColor, builder.toString());
    }

    public String logRequestComplete(RequestLog requestLog) {
        StringBuilder builder = new StringBuilder();
        
        // Separator
        //builder.append("###################\n");
        builder.append(properties.getSeparator()).append("\n");

        // Request End Flag
        builder.append(properties.getRequestEndFlag()).append("\n");
        
        // Basic Info
        builder.append(String.format("Timestamp      : %s\n", LocalDateTime.now().format(timestampFormatter)));
        builder.append(String.format("HTTP Status    : %d\n", requestLog.getStatus()));
        builder.append(String.format("Response Time  : %dms\n", requestLog.getProcessingTime()));
        builder.append(String.format("Trace ID       : %s\n", requestLog.getTraceId()));
        builder.append("\n");
        
        // Headers
        if (properties.isLogHeaders() && requestLog.getHeaders() != null) {
            builder.append("Headers        :\n");
            formatHeaders(builder, requestLog.getHeaders());
        }
        
        // Response Body
        if (properties.isLogResponse() && requestLog.getResponseBody() != null) {
            builder.append("\nResponse Body  :\n");
            builder.append(formatJson(requestLog.getResponseBody()));
        }
        
        builder.append("\nError Details  : None");
        
        // Separator
        builder.append("\n").append(properties.getSeparator());

        return ColorProcessorFactory.processColor(requestEndColor, builder.toString());
    }

    public String logRequestError(RequestLog requestLog) {
        StringBuilder builder = new StringBuilder();
        
        // Separator
        builder.append(properties.getSeparator()).append("\n");

        // Error Flag
        builder.append(properties.getRequestErrorFlag()).append("\n");
        
        // Basic Info
        builder.append(String.format("Timestamp      : %s\n", LocalDateTime.now().format(timestampFormatter)));
        builder.append(String.format("HTTP Status    : %d\n", requestLog.getStatus()));
        builder.append(String.format("Response Time  : %dms\n", requestLog.getProcessingTime()));
        builder.append(String.format("Trace ID       : %s\n", requestLog.getTraceId()));
        builder.append("\n");
        
        // Headers
        if (properties.isLogHeaders() && requestLog.getHeaders() != null) {
            builder.append("Headers        :\n");
            formatHeaders(builder, requestLog.getHeaders());
        }
        
        // Error Details
        builder.append("\nError Details  :\n");
        if (requestLog.getException() != null) {
            builder.append(String.format("  - Error Type   : %s\n", requestLog.getException()));
            builder.append(String.format("  - Error Message: %s\n", requestLog.getExceptionMessage()));
        }
        
        // Error Context
        builder.append("\nError Context  :\n");
        builder.append("  - Request Context:\n");
        builder.append(String.format("      - Endpoint     : %s\n", requestLog.getUri()));
        if (requestLog.getQueryString() != null) {
            builder.append(String.format("      - Query Params : %s\n", requestLog.getQueryString()));
        }
        
        // System Context
        appendSystemContext(builder);
        
        // Separator
        builder.append("\n").append(properties.getSeparator());

        return ColorProcessorFactory.processColor(errorColor, builder.toString());
    }

    private void formatHeaders(StringBuilder builder, Map<String, String> headers) {
        headers.forEach((key, value) -> {
            String maskedValue = shouldMaskHeader(key) ? maskSensitiveData(value) : value;
            builder.append(String.format("  - %-10s : %s\n", key, maskedValue));
        });
    }

    private void formatQueryParams(StringBuilder builder, String queryString) {
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                builder.append(String.format("  - %-10s : %s\n", keyValue[0], keyValue[1]));
            }
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

    private String buildFullUrl(RequestLog requestLog) {
        StringBuilder url = new StringBuilder();
        url.append("https://").append(getHostInfo());
        url.append(requestLog.getUri());
        if (requestLog.getQueryString() != null && !requestLog.getQueryString().isEmpty()) {
            url.append("?").append(requestLog.getQueryString());
        }
        return url.toString();
    }

    private boolean shouldMaskHeader(String headerName) {
        return headerName.toLowerCase().contains("authorization") || 
               headerName.toLowerCase().contains("cookie") ||
               headerName.toLowerCase().contains("token");
    }

    private String maskSensitiveData(String data) {
        if (data == null || data.length() < 8) {
            return "*****";
        }
        return data.substring(0, 3) + "...***";
    }

    private String getInstanceId() {
        return "service-instance-1";
    }

    private String getHostInfo() {
        return "example.com";
    }

    private void appendSystemContext(StringBuilder builder) {
        builder.append("  - System Context:\n");
        builder.append(String.format("      - CPU Load     : %.0f%%\n", 75.0));
        builder.append(String.format("      - Memory Usage : %.1fGB / %.1fGB\n", 1.2, 2.0));
        builder.append(String.format("      - Thread Count : %d\n", Thread.activeCount()));
        builder.append(String.format("      - Environment  : %s\n", "Production"));
    }
} 