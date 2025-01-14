package ltd.weiyiyi.requestlogging.infrastructure.spi;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingProperties;

/**
 * 请求日志处理器接口
 * 用于处理请求日志的SPI接口
 *
 * @author weihan
 */
public interface RequestLogProcessor {

    /**
     * 初始化处理器
     *
     * @param properties 日志配置属性
     */
    default void init(RequestLoggingProperties properties) {
        // 默认空实现
    }

    /**
     * 处理请求日志
     *
     * @param log 请求日志对象
     */
    void process(RequestLog log);

    /**
     * 处理请求错误日志
     *
     * @param log 请求日志对象
     */
    default void processRequestError(RequestLog log) {
        process(log);
    }

    /**
     * 关闭处理器
     */
    default void close() {
        // 默认空实现
    }
} 