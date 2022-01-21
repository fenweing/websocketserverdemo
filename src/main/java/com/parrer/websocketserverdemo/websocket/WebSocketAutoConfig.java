package com.parrer.websocketserverdemo.websocket;

import com.parrer.websocket.server.MyWebSocketHandler;
import com.parrer.websocket.server.WebSocketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketAutoConfig implements WebSocketConfigurer {
    @Autowired
    private DemoMyWebSocketHandler myWebSocketHandler;
    @Autowired
    private DemoWebSocketInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // webSocket通道
        // 指定处理器和路径
        registry.addHandler(myWebSocketHandler, "/ws")
                // 指定自定义拦截器
                .addInterceptors(webSocketInterceptor)
                // 允许跨域
                .setAllowedOrigins("*");
        // sockJs通道
        registry.addHandler(new MyWebSocketHandler(), "/sock-js")
                .addInterceptors(new WebSocketInterceptor())
                .setAllowedOrigins("*")
                // 开启sockJs支持
                .withSockJS();
    }
}
