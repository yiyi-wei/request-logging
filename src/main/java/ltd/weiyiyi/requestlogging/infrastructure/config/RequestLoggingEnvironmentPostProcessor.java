package ltd.weiyiyi.requestlogging.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * Environment post processor for request logging
 * Sets up default logging configuration if not present
 *
 * @author weiyiyi
 */
public class RequestLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info("Configuring request logging environment");

        Properties props = new Properties();
        
        // 设置默认的日志级别
        if (!environment.containsProperty("logging.level.ltd.weiyiyi.requestlogging")) {
            props.put("logging.level.ltd.weiyiyi.requestlogging", "DEBUG");
        }

        // 设置默认的请求日志配置
        if (!environment.containsProperty("request-logging.enabled")) {
            props.put("request-logging.enabled", "true");
        }

        if (!environment.containsProperty("request-logging.log-level")) {
            props.put("request-logging.log-level", "INFO");
        }

        // 设置日志文件配置
        if (!environment.containsProperty("request-logging.log-file-dir")) {
            props.put("request-logging.log-file-dir", "logs/request");
        }

        // 设置日志格式配置
        if (!environment.containsProperty("request-logging.log-headers")) {
            props.put("request-logging.log-headers", "true");
        }

        if (!environment.containsProperty("request-logging.log-request-body")) {
            props.put("request-logging.log-request-body", "true");
        }

        if (!environment.containsProperty("request-logging.log-response")) {
            props.put("request-logging.log-response", "true");
        }

        // 设置Filter配置
        if (!environment.containsProperty("request-logging.filter-order")) {
            props.put("request-logging.filter-order", String.valueOf(Integer.MIN_VALUE + 100));
        }

        // 添加属性源
        if (!props.isEmpty()) {
            PropertiesPropertySource propertySource = new PropertiesPropertySource(
                "requestLoggingDefaultProperties", props);
            environment.getPropertySources().addFirst(propertySource);
            log.info("Added default request logging properties: {}", props);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
} 