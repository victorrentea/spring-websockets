package victor.training.websockets.future;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import victor.training.websockets.TimeUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

//@Testcontainers
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskIntegrationTest {
    WebSocketClient client = new StandardWebSocketClient();
    WebSocketStompClient stompClient = new WebSocketStompClient(client);
    @Value("${local.server.port}")
    private int port;

//
//    @Container
//    static public RabbitMQContainer rabbitMQContainer =
//            new RabbitMQContainer("rabbitmq:3-management");
//
//    @DynamicPropertySource
//    public static void registerPgProperties(DynamicPropertyRegistry registry) {
////        registry.add("spring.datasource.username",  rabbitMQContainer.getAmqpUrl());
//    }


    @BeforeEach
    public void setup() {
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setMessageConverter(new StringMessageConverter());
    }


    @Autowired
    Consumer<Message<String>> handleTaskResponse;

    @Test
    void givenWebSocket_whenMessage_thenVerifyMessage() throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
//        TestStompSessionHandler<String> sessionHandler = new TestStompSessionHandler<>("/user/user/queue/task-status", String.class);
        TestStompSessionHandler<String> sessionHandler = new TestStompSessionHandler<>("/topic/task-status", String.class);
        log.info("Connecting to port: " + port);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBasicAuth("user", "user");
        StompHeaders stompHeaders = new StompHeaders();
        stompClient.connect("ws://localhost:{port}/task/websocket", headers,stompHeaders, sessionHandler, port);

        sessionHandler.getConnectedFuture().get(5, TimeUnit.SECONDS); //wait for connection to websockets to be established


        log.info("Send pretend message");
        TimeUtils.sleepq(1); // victor a fost slab: nu a gasit un mod in care sa nu fie nevoie de sleep.

        // m-am legat. Acum trebuie sa triggerez un semnal in urma caruia BE sa emita date pe WS

        MessageHeaders messageHeaders = new MessageHeaders(Map.of("REQUESTER_USERNAME","user"));
        Message<String> pretendMessage = MessageBuilder.createMessage("task15 is done", messageHeaders);
        handleTaskResponse.accept(pretendMessage); // execu handlerul de mesaj ca si cum ar fi venit ACKul pe taskul trimis spre executie

        String responseMessage = sessionHandler.getFirstFrameFuture().get(10, TimeUnit.SECONDS); // block the test until the first frame arrives back
        assertThat(responseMessage).contains("task15 is done");
    }
}

