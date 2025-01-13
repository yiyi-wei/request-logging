package ltd.weiyiyi.requestlogging.infrastructure.spi;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;

/**
 * 请求日志处理器接口
 */
public interface RequestLogProcessor {
    /**
     * 初始化处理器
     *
     * @param properties 日志配置属性
     */
    void init(RequestLoggingProperties properties);

    /**
     * 处理请求开始日志
     *
     * @param requestLog 请求日志对象
     */
    void processRequestStart(RequestLog requestLog);

    /**
     * 处理请求完成日志
     *
     * @param requestLog 请求日志对象
     */
    void processRequestComplete(RequestLog requestLog);

    /**
     * 处理请求错误日志
     *
     * @param requestLog 请求日志对象
     */
    void processRequestError(RequestLog requestLog);
} 