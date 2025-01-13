package ltd.weiyiyi.requestlogging.application.service;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;
import ltd.weiyiyi.requestlogging.infrastructure.processor.ConsoleLogProcessor;
import ltd.weiyiyi.requestlogging.infrastructure.logfile.FileLogProcessor;
import ltd.weiyiyi.requestlogging.infrastructure.formatter.LogFormatter;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.UUID;

/**
 * 请求日志服务
 */
public class RequestLoggingService {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingService.class);

    private final RequestLoggingProperties properties;
    private final LogFormatter logFormatter;
    private final List<RequestLogProcessor> logProcessors = new ArrayList<>();
    private final ThreadLocal<String> traceId = new ThreadLocal<>();

    public RequestLoggingService(RequestLoggingProperties properties) {
        this.properties = properties;
        this.logFormatter = new LogFormatter(properties);
        initLogProcessors();
    }

    private void initLogProcessors() {
        // 加载SPI实现的处理器
        ServiceLoader<RequestLogProcessor> processors = ServiceLoader.load(RequestLogProcessor.class);
        processors.forEach(processor -> {
            processor.init(properties);
            logProcessors.add(processor);
        });

        // 如果没有找到任何处理器，添加默认的控制台处理器
        if (logProcessors.isEmpty()) {
            ConsoleLogProcessor defaultProcessor = new ConsoleLogProcessor();
            defaultProcessor.init(properties);
            logProcessors.add(defaultProcessor);
        }
    }

    /**
     * 记录请求日志
     *
     * @param request HTTP请求对象
     */
    public void logRequest(ContentCachingRequestWrapper request) {
        if (!shouldLog()) {
            return;
        }

        try {
            String currentTraceId = generateTraceId();
            if (properties.isEnableMdcTrace()) {
                MDC.put(properties.getTraceIdKey(), currentTraceId);
            }

            RequestLog log = new RequestLog();
            log.setRequestTime(LocalDateTime.now());
            log.setTraceId(currentTraceId);

            if (request != null) {
                // 获取通用请求信息
                log.setMethod(request.getMethod());
                log.setUri(request.getRequestURI());
                log.setQueryString(request.getQueryString());
                log.setClientIp(getClientIp(request));

                // 记录请求头
                if (properties.isLogHeaders()) {
                    log.setHeaders(getHeaders(request));
                }

                // 记录请求体
                if (properties.isLogRequestBody()) {
                    byte[] content = request.getContentAsByteArray();
                    if (content != null && content.length > 0) {
                        String requestBody = new String(content);
                        if (requestBody.length() > properties.getRequestBodyMaxLength()) {
                            requestBody = requestBody.substring(0, properties.getRequestBodyMaxLength()) + "...";
                        }
                        log.setRequestBody(requestBody);
                    }
                }
            }
            
            logProcessors.forEach(processor -> processor.processRequestStart(log));
        } catch (Exception e) {
            logger.error("Error logging request", e);
        }
    }

    /**
     * 记录响应日志
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    public void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (!shouldLog()) {
            return;
        }

        try {
            RequestLog log = new RequestLog();
            log.setRequestTime(LocalDateTime.now());
            log.setResponseTime(LocalDateTime.now());
            log.setTraceId(traceId.get());

            if (request != null) {
                log.setMethod(request.getMethod());
                log.setUri(request.getRequestURI());
                log.setQueryString(request.getQueryString());
                log.setClientIp(getClientIp(request));
            }

            if (response != null && properties.isLogResponse()) {
                log.setStatus(response.getStatus());

                byte[] content = response.getContentAsByteArray();
                if (content != null && content.length > 0) {
                    String responseBody = new String(content);
                    if (responseBody.length() > properties.getResponseMaxLength()) {
                        responseBody = responseBody.substring(0, properties.getResponseMaxLength()) + "...";
                    }
                    log.setResponseBody(responseBody);
                }
            }
            
            logProcessors.forEach(processor -> processor.processRequestComplete(log));
        } catch (Exception e) {
            logger.error("Error logging response", e);
        }
    }

    /**
     * 记录错误日志
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param exception 异常对象
     */
    public void logError(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, Exception exception) {
        if (!shouldLog()) {
            return;
        }

        try {
            RequestLog log = new RequestLog();
            log.setRequestTime(LocalDateTime.now());
            log.setResponseTime(LocalDateTime.now());
            log.setTraceId(traceId.get());

            if (request != null) {
                log.setMethod(request.getMethod());
                log.setUri(request.getRequestURI());
                log.setQueryString(request.getQueryString());
                log.setClientIp(getClientIp(request));
            }

            if (response != null) {
                log.setStatus(response.getStatus());
            }

            if (exception != null) {
                log.setException(exception.getClass().getName());
                log.setExceptionMessage(exception.getMessage());
            }

            logProcessors.forEach(processor -> processor.processRequestError(log));
        } catch (Exception e) {
            logger.error("Error logging error", e);
        }
    }

    private boolean shouldLog() {
        return properties.isEnabled() && Math.random() < properties.getSamplingRate();
    }

    private String generateTraceId() {
        String currentTraceId = traceId.get();
        if (currentTraceId == null) {
            currentTraceId = UUID.randomUUID().toString();
            traceId.set(currentTraceId);
        }
        return currentTraceId;
    }

    private String getClientIp(ContentCachingRequestWrapper request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private java.util.Map<String, String> getHeaders(ContentCachingRequestWrapper request) {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    /**
     * 清理线程本地变量
     */
    public void clearThreadLocals() {
        traceId.remove();
        if (properties.isEnableMdcTrace()) {
            MDC.remove(properties.getTraceIdKey());
        }
    }
} 