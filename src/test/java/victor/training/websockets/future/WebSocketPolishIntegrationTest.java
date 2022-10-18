package victor.training.websockets.future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

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
        stompClient.connect("ws://localhost:{port}/stock-ticks/websocket", headers, sessionHandler, port);

        // Trigger in urma caruia WS sa emita un data frame catre client (brow)

        //1) new RestTemplate().postForObject("http://localhost:{port}",/......)
        //2) vine mesaj pe un "reply= queue"
        //3) time-based. @Scheduled:  apare un fisier intr-un folder, un rand in DB se modifica, <<< ACI
        //4) pe alt WS echipa de support raspunde la mesajul tau

        CompletableFuture<Map> cf = sessionHandler.getFirstFrameFuture();

        Map map = cf.get(20, TimeUnit.SECONDS); // throw daca a aparut eroare in interact cu ws sau datele primului frame

        assertThat(map).containsKey("HPE");
        assertThat(map.get("HPE")).isInstanceOf(Integer.class);
    }

}

