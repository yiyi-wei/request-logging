package ltd.weiyiyi.requestlogging.infrastructure.compatibility;

/**
 * Servlet 适配器接口
 * 用于处理 Spring Boot 2 和 Spring Boot 3 的请求响应对象
 *
 * @author weihan
 */
public interface ServletAdapter {
    /**
     * 获取请求头
     *
     * @param headerName 请求头名称
     * @return 请求头值
     */
    String getHeader(String headerName);

    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    String getRemoteAddr();

    /**
     * 获取请求URI
     *
     * @return 请求URI
     */
    String getRequestURI();

    /**
     * 获取请求方法
     *
     * @return 请求方法
     */
    String getMethod();

    /**
     * 获取查询字符串
     *
     * @return 查询字符串
     */
    String getQueryString();

    /**
     * 获取内容长度
     *
     * @return 内容长度
     */
    int getContentLength();

    /**
     * 获取请求体字节数组
     *
     * @return 请求体字节数组
     */
    byte[] getContentAsByteArray();

    /**
     * 获取响应状态
     *
     * @return 响应状态
     */
    int getStatus();

    /**
     * 获取响应体字节数组
     *
     * @return 响应体字节数组
     */
    byte[] getResponseContentAsByteArray();

    /**
     * 复制响应体到原始响应
     *
     * @throws java.io.IOException 如果发生IO异常
     */
    void copyBodyToResponse() throws java.io.IOException;
} 