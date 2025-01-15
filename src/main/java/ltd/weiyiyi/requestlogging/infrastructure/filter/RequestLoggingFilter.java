package ltd.weiyiyi.requestlogging.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ltd.weiyiyi.requestlogging.application.service.RequestLoggingService;
import org.springframework.core.Ordered;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 请求日志过滤器
 *
 * @author weihan
 */
public class RequestLoggingFilter implements Filter, Ordered {

    private final RequestLoggingService requestLoggingService;
    private int order = Ordered.LOWEST_PRECEDENCE - 10;

    @Override
    public int getOrder() {
        return order;
    }

    public RequestLoggingFilter(RequestLoggingService requestLoggingService) {
        this.requestLoggingService = requestLoggingService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        if (skipFilter(httpRequest)) {
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

    protected boolean skipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/actuator") || 
               path.contains("/swagger") || 
               path.contains("/v3/api-docs") ||
               path.contains("/webjars") ||
               path.contains("/favicon.ico");
    }

    public void setOrder(int order) {
        this.order = order;
    }
} 