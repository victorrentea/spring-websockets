package victor.training.websockets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import victor.training.websockets.chat.OutputMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RequiredArgsConstructor
@Component
@Slf4j
public class StompEventListener{
    private final SimpMessagingTemplate websocket;

    @EventListener
    public void onSocketConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        String usernameFromHeader = event.getUser().getName();//sha.getNativeHeader("user");

        CompletableFuture.runAsync(() -> {
            log.info("SEnd welcome to Username" + usernameFromHeader);
            OutputMessage message = new OutputMessage("Sys", "Welcome " + usernameFromHeader);
            websocket.convertAndSend("/topic/chat", message);
        }, delayedExecutor(100, MILLISECONDS));

        boolean isConnect = sha.getCommand() == StompCommand.CONNECT;
        boolean isDisconnect = sha.getCommand() == StompCommand.DISCONNECT;
        log.debug("Connect: " + isConnect + ",disconnect:" + isDisconnect +
                     ",event[sessionId: " + sha.getSessionId() + ";" + event.getUser() + " ,command =" + sha.getCommand());

    }

    @EventListener
    public void onSocketConnected(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        log.info("[Connected] " + sha.getUser());
    }

    @EventListener
    public void onSocketDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        log.info("[Disonnected] " + sha.getUser());
    }


}