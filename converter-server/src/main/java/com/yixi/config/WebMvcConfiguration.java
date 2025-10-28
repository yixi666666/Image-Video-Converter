package com.yixi.config;

import com.yixi.interceptor.JwtTokenUserInterceptor;
import com.yixi.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 整合后的Spring MVC配置类，包含资源映射、拦截器、消息转换器等配置
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    // 从配置文件读取本地视频路径
    @Value("${video.local-path}")
    private String localVideoPath;

    // 从配置文件读取HTTP访问前缀
    @Value("${video.http-prefix}")
    private String httpVideoPrefix;

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login");
    }

    /**
     * 合并静态资源映射配置：包含视频路径映射、Swagger资源映射等
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 视频资源映射（原WebResourceConfig的配置）
        String resourceLocation = "file:" + localVideoPath;
        registry.addResourceHandler(httpVideoPrefix + "**")  // 匹配前端访问路径（如 /api/videos/xxx.mp4）
                .addResourceLocations(resourceLocation)     // 映射到服务器本地目录
                .setCachePeriod(0);  // 缓存周期（秒）

        // 2. Swagger及其他静态资源映射（原WebMvcConfiguration的配置）
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 3. 保留Spring Boot默认的静态资源映射（如classpath:/static/、/public/等）
        // 注意：WebMvcConfigurationSupport会覆盖默认配置，这里显式添加默认映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/META-INF/resources/")
                .addResourceLocations("classpath:/resources/")
                .addResourceLocations("classpath:/static/")
                .addResourceLocations("classpath:/public/");
    }

    /**
     * 扩展Spring MVC框架的消息转换器
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, converter);
    }
}