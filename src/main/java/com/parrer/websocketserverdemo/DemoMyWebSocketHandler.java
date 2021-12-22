package com.parrer.websocketserverdemo;

import com.parrer.util.LogUtil;
import com.parrer.websocket.server.MyWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;

@Component
public class DemoMyWebSocketHandler extends MyWebSocketHandler {
    @Override
    public void dealTextMessage(TextMessage message) {
        LogUtil.info("接受到消息-{}", message.getPayload());
        sendMessage("服务端回复:" + message.getPayload());
    }

    @Override
    protected WebSocketSession getSessionByMessage(Object message) {
        Set<Map.Entry<String, WebSocketSession>> entries = sessionMap.entrySet();
        for (Map.Entry<String, WebSocketSession> entry : entries) {
            return entry.getValue();
        }
        return null;
    }
}
