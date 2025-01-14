package ltd.weiyiyi.requestlogging.infrastructure.filter;

import ltd.weiyiyi.requestlogging.application.service.RequestLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求日志过滤器
 *
 * @author weihan
 */
public class RequestLoggingFilter implements Filter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final RequestLoggingService requestLoggingService;
    private int order = Ordered.LOWEST_PRECEDENCE - 10;

    public RequestLoggingFilter(RequestLoggingService requestLoggingService) {
        this.requestLoggingService = requestLoggingService;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (shouldNotFilter(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        try {
            requestLoggingService.logRequest(wrappedRequest);
            chain.doFilter(wrappedRequest, wrappedResponse);
            requestLoggingService.logResponse(wrappedRequest, wrappedResponse);
            wrappedResponse.copyBodyToResponse();
        } catch (Exception e) {
            requestLoggingService.logError(wrappedRequest, wrappedResponse, e);
            throw e;
        } finally {
            requestLoggingService.clearThreadLocals();
        }
    }

    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/actuator") || 
               path.contains("/swagger") || 
               path.contains("/v3/api-docs") ||
               path.contains("/webjars") ||
               path.contains("/favicon.ico");
    }
} 