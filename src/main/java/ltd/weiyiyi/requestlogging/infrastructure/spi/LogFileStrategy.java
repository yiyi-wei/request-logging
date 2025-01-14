package ltd.weiyiyi.requestlogging.infrastructure.spi;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import java.io.File;

/**
 * 日志文件策略接口
 * 用于定义日志文件的存储策略
 *
 * @author weihan
 */
public interface LogFileStrategy {
    /**
     * 获取日志文件
     *
     * @param log 请求日志对象
     * @return 日志文件
     */
    File getLogFile(RequestLog log);

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();

    /**
     * 初始化策略
     *
     * @param baseDir 日志文件基础目录
     */
    void init(String baseDir);

    /**
     * 清理资源
     */
    void cleanup();
} 