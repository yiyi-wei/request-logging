package ltd.weiyiyi.requestlogging.application.service;

import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.spi.RequestLogProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步日志处理服务
 * 负责异步处理日志
 *
 * @author weihan
 */
@Service
public class AsyncLogProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncLogProcessingService.class);

    /**
     * 异步处理普通日志
     *
     * @param logProcessors 日志处理器列表
     * @param log 日志对象
     */
    @Async("requestLoggingExecutor")
    public void processLogAsync(List<RequestLogProcessor> logProcessors, RequestLog log) {
        logProcessors.forEach(processor -> {
            try {
                processor.process(log);
            } catch (Exception e) {
                logger.error("Error in async log processor: {}", processor.getClass().getName(), e);
            }
        });
    }

    /**
     * 异步处理错误日志
     *
     * @param logProcessors 日志处理器列表
     * @param log 日志对象
     */
    @Async("requestLoggingExecutor")
    public void processErrorLogAsync(List<RequestLogProcessor> logProcessors, RequestLog log) {
        logProcessors.forEach(processor -> {
            try {
                processor.processRequestError(log);
            } catch (Exception e) {
                logger.error("Error in async error log processor: {}", processor.getClass().getName(), e);
            }
        });
    }
} 