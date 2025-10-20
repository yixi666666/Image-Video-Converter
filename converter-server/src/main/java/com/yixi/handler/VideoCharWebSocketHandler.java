package com.yixi.handler;

import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VideoCharWebSocketHandler extends TextWebSocketHandler {

    // 使用 Map 存储客户端 ID 与对应的 session
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String clientId = session.getId();
        sessionMap.put(clientId, session);
        System.out.println("客户端已连接: " + clientId);
        // 连接成功时可以通知前端它的 ID
        try {
            session.sendMessage(new TextMessage("{\"clientId\": \"" + clientId + "\"}"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionMap.remove(session.getId());
        System.out.println("客户端已断开: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 打印客户端消息
        System.out.println("收到客户端 " + session.getId() + " 的消息: " + message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println("传输错误: " + session.getId() + " -> " + exception.getMessage());
    }

    /**
     * ✅ 定向推送：只向指定 clientId 发送帧
     */
    public void sendFrameTo(String clientId, String charFrame) {
        WebSocketSession session = sessionMap.get(clientId);
        if (session != null && session.isOpen()) {
            try {
                String json = "{\"frame\": " + JSONObject.quote(charFrame) + "}";
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("客户端 " + clientId + " 不存在或已关闭连接");
        }
    }
}
