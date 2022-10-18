package victor.training.websockets.chat;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Unde pot trimite mesaje in general
        config.setApplicationDestinationPrefixes("/app"); // prefix comun pentru cand BROW imi scrie serverului
    }
    
    @Bean
    public GreetingHandshakeChatInterceptor handshakeInterceptor() {
        return new GreetingHandshakeChatInterceptor();
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // inregistreaza un STOMP/ws la URL-ul /chat
        registry.addEndpoint("/chat").addInterceptors(handshakeInterceptor()).withSockJS();
    }
    @Slf4j
    public static class GreetingHandshakeChatInterceptor implements HandshakeInterceptor{
        @Autowired
        @Lazy
        private SimpMessagingTemplate webSocket;

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            log.info("attr "+ attributes);
            return true;
        }

        @SneakyThrows
        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
            log.info("welcome!");
            webSocket.convertAndSend("/topic/chat", new OutputMessage("sys", "Please welcome "));
        }
    }

}

