package com.yixi.config;

import com.yixi.handler.VideoCharWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    // 这里用 @Bean 明确交给 Spring 管理单例
    @Bean
    public VideoCharWebSocketHandler videoCharWebSocketHandler() {
        return new VideoCharWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(videoCharWebSocketHandler(), "/ws/video-char")
                .setAllowedOrigins("*");
    }
}
