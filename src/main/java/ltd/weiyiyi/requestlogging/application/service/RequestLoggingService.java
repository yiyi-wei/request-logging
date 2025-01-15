package ltd.weiyiyi.requestlogging.application.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;
import ltd.weiyiyi.requestlogging.infrastructure.processor.ConsoleLogProcessor;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import ltd.weiyiyi.requestlogging.infrastructure.util.SystemMetricsCollector;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 请求日志服务
 * 负责处理请求日志的记录和分发
 *
 * @author weihan
 */
public class RequestLoggingService {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingService.class);

    private final RequestLoggingProperties properties;
    private final List<RequestLogProcessor> logProcessors = new ArrayList<>();
    private final ThreadLocal<String> traceId = new ThreadLocal<>();
    private final GenericObjectPool<RequestLog> requestLogPool;
    private final SystemMetricsCollector systemMetricsCollector;
    private final AsyncLogProcessingService asyncLogProcessingService;

    public RequestLoggingService(RequestLoggingProperties properties, 
                               GenericObjectPool<RequestLog> requestLogPool,
                               SystemMetricsCollector systemMetricsCollector,
                               AsyncLogProcessingService asyncLogProcessingService) {
        this.properties = properties;
        this.requestLogPool = requestLogPool;
        this.systemMetricsCollector = systemMetricsCollector;
        this.asyncLogProcessingService = asyncLogProcessingService;
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
        if (skipLogging()) {
            return;
        }

        try {
            String currentTraceId = generateTraceId();
            if (properties.isEnableMdcTrace()) {
                MDC.put(properties.getTraceIdKey(), currentTraceId);
            }

            RequestLog log = borrowRequestLog();
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
                    if (content.length > 0) {
                        String requestBody = new String(content);
                        if (requestBody.length() > properties.getRequestBodyMaxLength()) {
                            requestBody = requestBody.substring(0, properties.getRequestBodyMaxLength()) + "...";
                        }
                        log.setRequestBody(requestBody);
                    }
                }
            }
            
            if (properties.isEnableAsyncLogging()) {
                processLogAsync(log);
            } else {
                processLogSync(log);
            }

        } catch (Exception e) {
            logger.error("Error logging request", e);
        }
    }

    private RequestLog createBaseLog(ContentCachingRequestWrapper request) {
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

        return log;
    }

    /**
     * 记录响应日志
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    public void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (skipLogging()) {
            return;
        }

        try {
            RequestLog log = createBaseLog(request);

            if (response != null && properties.isLogResponse()) {
                log.setStatus(response.getStatus());

                byte[] content = response.getContentAsByteArray();
                if (content.length > 0) {
                    String responseBody = new String(content);
                    if (responseBody.length() > properties.getResponseMaxLength()) {
                        responseBody = responseBody.substring(0, properties.getResponseMaxLength()) + "...";
                    }
                    log.setResponseBody(responseBody);
                }
            }
            
            if (properties.isEnableAsyncLogging()) {
                processLogAsync(log);
            } else {
                processLogSync(log);
            }
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
        if (skipLogging()) {
            return;
        }

        try {
            RequestLog log = createBaseLog(request);

            if (response != null) {
                log.setStatus(response.getStatus());
            }

            if (exception != null) {
                log.setException(exception.getClass().getName());
                log.setExceptionMessage(exception.getMessage());
                // 添加堆栈跟踪信息
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);
                log.setStackTrace(sw.toString());
            }

            if (properties.isEnableAsyncLogging()) {
                processErrorLogAsync(log);
            } else {
                processErrorLogSync(log);
            }
        } catch (Exception e) {
            logger.error("Error logging error", e);
        }
    }

    /**
     * @return is skip these log
     */
    private boolean skipLogging() {
        return !properties.isEnabled() || !(Math.floor(Math.random() * 100)  < properties.getSamplingRate());
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

    private Map<String, String> getHeaders(ContentCachingRequestWrapper request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
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

    /**
     * 处理请求日志
     */
    private void processRequestLog(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        if (skipProcess()) {
            return;
        }

        RequestLog log = null;
        try {
            log = borrowRequestLog();
            populateRequestLog(log, request, response);
            // processLog(log);
        } catch (Exception e) {
            logger.error("Error processing request log", e);
        } finally {
            returnRequestLog(log);
        }
    }

    private boolean skipProcess() {
        // 检查采样率
        if (properties.getSamplingRate() < 100) {
            double random = ThreadLocalRandom.current().nextDouble() * 100;
            return !(random <= properties.getSamplingRate());
        }
        return false;
    }

    private RequestLog borrowRequestLog() {
        try {
            return properties.isEnableObjectPool() && requestLogPool != null
                    ? requestLogPool.borrowObject()
                    : new RequestLog();
        } catch (Exception e) {
            logger.warn("Failed to borrow RequestLog from pool, creating new instance", e);
            return new RequestLog();
        }
    }

    private void returnRequestLog(RequestLog log) {
        if (log != null && properties.isEnableObjectPool() && requestLogPool != null) {
            try {
                requestLogPool.returnObject(log);
            } catch (Exception e) {
                logger.warn("Failed to return RequestLog to pool", e);
            }
        }
    }

    private void populateRequestLog(RequestLog log, ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        log.setRequestTime(LocalDateTime.now());
        log.setResponseTime(LocalDateTime.now());
        log.setTraceId(traceId.get());
        log.setSystemMetrics(systemMetricsCollector.collectMetrics());

        if (request != null) {
            log.setMethod(request.getMethod());
            log.setUri(request.getRequestURI());
            log.setQueryString(request.getQueryString());
            log.setClientIp(getClientIp(request));
            if (properties.isLogHeaders()) {
                log.setHeaders(getHeaders(request));
            }
            if (properties.isLogRequestBody()) {
                log.setRequestBody(getRequestBody(request));
            }
        }

        if (response != null && properties.isLogResponse()) {
            log.setStatus(response.getStatus());
            log.setResponseBody(getResponseBody(response));
        }
    }

    private void populateErrorLog(RequestLog log, ContentCachingRequestWrapper request, 
                                ContentCachingResponseWrapper response, Exception exception) {
        populateRequestLog(log, request, response);
        
        if (exception != null) {
            log.setException(exception.getClass().getName());
            log.setExceptionMessage(exception.getMessage());
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            log.setStackTrace(sw.toString());
        }
    }

    /**
     * 同步处理日志
     */
    private void processLogSync(RequestLog log) {
        logProcessors.forEach(processor -> {
            try {
                processor.process(log);
            } catch (Exception e) {
                logger.error("Error in log processor: {}", processor.getClass().getName(), e);
            }
        });
    }

    /**
     * 异步处理日志
     */
    private void processLogAsync(RequestLog log) {
        asyncLogProcessingService.processLogAsync(logProcessors, log);
    }

    /**
     * 同步处理错误日志
     */
    private void processErrorLogSync(RequestLog log) {
        logProcessors.forEach(processor -> {
            try {
                processor.processRequestError(log);
            } catch (Exception e) {
                logger.error("Error in error log processor: {}", processor.getClass().getName(), e);
            }
        });
    }

    /**
     * 异步处理错误日志
     */
    private void processErrorLogAsync(RequestLog log) {
        asyncLogProcessingService.processErrorLogAsync(logProcessors, log);
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        
        int length = Math.min(content.length, properties.getRequestBodyMaxLength());
        try {
            String body = new String(content, 0, length, request.getCharacterEncoding());
            return properties.isJsonPrettyPrint() ? formatJson(body) : body;
        } catch (Exception e) {
            logger.warn("Failed to read request body", e);
            return "[Failed to read request body]";
        }
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        
        int length = Math.min(content.length, properties.getResponseMaxLength());
        try {
            String body = new String(content, 0, length, response.getCharacterEncoding());
            return properties.isJsonPrettyPrint() ? formatJson(body) : body;
        } catch (Exception e) {
            logger.warn("Failed to read response body", e);
            return "[Failed to read response body]";
        }
    }

    private String formatJson(String json) {
        try {
            Object jsonObject = JSON.parse(json);
            return JSON.toJSONString(jsonObject, JSONWriter.Feature.PrettyFormat);
        } catch (Exception e) {
            return json;
        }
    }
} 