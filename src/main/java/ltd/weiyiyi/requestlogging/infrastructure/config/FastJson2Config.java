package ltd.weiyiyi.requestlogging.infrastructure.config;

import com.alibaba.fastjson2.support.spring6.http.converter.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * FastJson2 配置类
 *
 * @author weihan
 */
@Configuration
@ConditionalOnClass(name = {"com.alibaba.fastjson2.JSON"})
@ConditionalOnMissingBean(name = "fastJsonHttpMessageConverter")
@ConditionalOnProperty(prefix = "request-logging", name = "use-fastjson", havingValue = "true", matchIfMissing = true)
public class FastJson2Config implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, new FastJsonHttpMessageConverter());
    }
} 