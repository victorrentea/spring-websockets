package victor.training.websockets.client;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Scanner;

public class StompWebSocketClientMain {

    private static final String URL = "ws://localhost:8080/stock-ticks/websocket";

    public static void main(String[] args) {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompSessionHandler sessionHandler = new StompClientSessionHandler();
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBasicAuth("user","user");
        stompClient.connect(URL,headers, sessionHandler);
        System.out.println("Be patient [ENTER to stop]...");
        new Scanner(System.in).nextLine();
    }
}
