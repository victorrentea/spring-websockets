package victor.training;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketPolishIntegrationTest {
    WebSocketClient client = new StandardWebSocketClient();
    WebSocketStompClient stompClient = new WebSocketStompClient(client);
    @Value("${local.server.port}")
    private int port;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketPolishIntegrationTest.class);

    @BeforeEach
    public void setup() {
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void givenWebSocket_whenMessage_thenVerifyMessage() throws Exception {
        TestStompSessionHandler<Map> sessionHandler = new TestStompSessionHandler<>("/topic/ticks", Map.class);
        System.out.println("Connecting to port: " + port);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBasicAuth("user", "user");
        stompClient.connect("ws://localhost:{port}/stock-ticks/websocket", headers, sessionHandler, this.port);

        Map map = sessionHandler.getFirstFrameFuture().get(20, TimeUnit.SECONDS);
        assertThat(map).containsKey("HPE");
        assertThat(map.get("HPE")).isInstanceOf(Integer.class);
    }

}

