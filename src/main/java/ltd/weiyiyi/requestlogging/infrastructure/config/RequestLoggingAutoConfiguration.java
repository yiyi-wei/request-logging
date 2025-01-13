package ltd.weiyiyi.requestlogging.infrastructure.config;

import ltd.weiyiyi.requestlogging.application.service.RequestLoggingService;
import ltd.weiyiyi.requestlogging.infrastructure.filter.RequestLoggingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 请求日志自动配置类
 * 
 * @author weihan
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(RequestLoggingProperties.class)
@ConditionalOnProperty(prefix = "request-logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RequestLoggingService requestLoggingService(RequestLoggingProperties properties) {
        return new RequestLoggingService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public RequestLoggingFilter requestLoggingFilter(RequestLoggingService loggingService) {
        return new RequestLoggingFilter(loggingService);
    }
} 