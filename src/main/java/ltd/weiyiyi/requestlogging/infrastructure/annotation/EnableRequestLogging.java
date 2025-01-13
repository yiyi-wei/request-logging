package ltd.weiyiyi.requestlogging.infrastructure.annotation;

import ltd.weiyiyi.requestlogging.infrastructure.config.RequestLoggingAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用请求日志记录功能的注解
 *
 * @author weihan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RequestLoggingAutoConfiguration.class)
public @interface EnableRequestLogging {
} 