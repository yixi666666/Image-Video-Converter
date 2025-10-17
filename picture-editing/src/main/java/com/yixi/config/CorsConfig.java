package com.yixi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类，解决前后端分离架构中的跨域问题
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        // 1.创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许访问的客户端域名，*表示允许所有域名
        // 替换为你的前端实际地址
        config.addAllowedOrigin("http://localhost:63342");
        config.addAllowedOrigin("http://127.0.0.1:5500");;
        // 允许的请求头，*表示允许所有请求头
        config.addAllowedHeader("*");
        // 允许的请求方法，*表示允许所有HTTP方法（GET, POST, PUT, DELETE等）
        config.addAllowedMethod("*");
        // 是否允许发送Cookie
        config.setAllowCredentials(true);
        // 预检请求的有效期，单位为秒
        config.setMaxAge(3600L);

        // 2.创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有URL路径应用上述CORS配置
        source.registerCorsConfiguration("/**", config);

        // 3.返回CORS过滤器
        return new CorsFilter(source);
    }
}
