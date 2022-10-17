package victor.training.reactive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

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
        TestReactiveStompSessionHandler<Map> sessionHandler = new TestReactiveStompSessionHandler<>("/topic/ticks", Map.class);
        System.out.println("Connecting to port: " + port);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBasicAuth("user", "user");
        stompClient.connect("ws://localhost:{port}/stock-ticks/websocket", headers, sessionHandler, this.port);

        List<Map> two = sessionHandler.getFrameFlux().take(2).collectList().block(Duration.ofSeconds(20));
        assertThat(two.get(0)).containsKey("HPE");
        assertThat(two.get(0).get("HPE")).isInstanceOf(Integer.class);
        assertThat(two.get(1)).containsKey("HPE");
        assertThat(two.get(1).get("HPE")).isInstanceOf(Integer.class);
    }

}

