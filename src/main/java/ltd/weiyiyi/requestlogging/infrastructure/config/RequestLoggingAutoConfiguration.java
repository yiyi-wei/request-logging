package ltd.weiyiyi.requestlogging.infrastructure.config;

import ltd.weiyiyi.requestlogging.application.service.AsyncLogProcessingService;
import ltd.weiyiyi.requestlogging.application.service.RequestLoggingService;
import ltd.weiyiyi.requestlogging.domain.model.RequestLog;
import ltd.weiyiyi.requestlogging.infrastructure.filter.RequestLoggingFilter;
import ltd.weiyiyi.requestlogging.infrastructure.util.SystemMetricsCollector;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 请求日志自动配置类
 * 
 * @author weihan
 */
@EnableAsync
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(RequestLoggingProperties.class)
@ConditionalOnProperty(prefix = "request-logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SystemMetricsCollector systemMetricsCollector() {
        return new SystemMetricsCollector();
    }

    @Bean
    @ConditionalOnMissingBean
    public AsyncLogProcessingService asyncLogProcessingService() {
        return new AsyncLogProcessingService();
    }

    @Bean
    @ConditionalOnMissingBean
    public GenericObjectPool<RequestLog> requestLogPool(RequestLoggingProperties properties) {
        if (!properties.isEnableObjectPool()) {
            return null;
        }

        GenericObjectPoolConfig<RequestLog> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(properties.getObjectPoolMaxSize());
        config.setMaxIdle(properties.getObjectPoolMaxSize());
        config.setMinIdle(properties.getAsyncCorePoolSize());
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        config.setJmxEnabled(false);

        return new GenericObjectPool<>(new RequestLogPooledObjectFactory(), config);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestLoggingService requestLoggingService(
            RequestLoggingProperties properties,
            GenericObjectPool<RequestLog> requestLogPool,
            SystemMetricsCollector systemMetricsCollector,
            AsyncLogProcessingService asyncLogProcessingService) {
        return new RequestLoggingService(properties, requestLogPool, systemMetricsCollector, asyncLogProcessingService);
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public RequestLoggingFilter requestLoggingFilter(RequestLoggingService loggingService) {
        return new RequestLoggingFilter(loggingService);
    }

    /**
     * RequestLog对象池工厂
     */
    private static class RequestLogPooledObjectFactory extends BasePooledObjectFactory<RequestLog> {
        @Override
        public RequestLog create() {
            return new RequestLog();
        }

        @Override
        public PooledObject<RequestLog> wrap(RequestLog requestLog) {
            return new DefaultPooledObject<>(requestLog);
        }

        @Override
        public void passivateObject(org.apache.commons.pool2.PooledObject<RequestLog> pooledObject) {
            pooledObject.getObject().reset();
        }
    }

    @Bean("requestLoggingExecutor")
    public Executor requestLoggingExecutor(RequestLoggingProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getAsyncCorePoolSize());
        executor.setMaxPoolSize(properties.getAsyncMaxPoolSize());
        executor.setQueueCapacity(properties.getAsyncQueueCapacity());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());

        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 设置线程池关闭的超时时间
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
} 