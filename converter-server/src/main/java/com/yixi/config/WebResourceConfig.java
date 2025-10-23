package com.yixi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 静态资源映射配置
 */
@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    // 从配置文件读取本地视频路径
    @Value("${video.local-path}")
    private String localVideoPath;

    // 从配置文件读取HTTP访问前缀
    @Value("${video.http-prefix}")
    private String httpVideoPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 关键：将本地视频目录映射为HTTP可访问路径
        // 注意：localVideoPath 必须以 "file:" 开头，否则会被当作类路径（classpath）处理
        String resourceLocation = "file:" + localVideoPath;

        registry.addResourceHandler(httpVideoPrefix + "**")  // 匹配前端访问路径（如 /api/videos/xxx.mp4）
                .addResourceLocations(resourceLocation)     // 映射到服务器本地目录
                .setCachePeriod(0);  // 缓存周期（秒）：0表示禁用缓存（开发环境推荐），生产环境可设为3600（1小时）

        // 可选：保留Spring Boot默认的静态资源映射（如classpath:/static/、/public/等）
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}