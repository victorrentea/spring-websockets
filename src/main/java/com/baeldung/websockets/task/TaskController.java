package com.baeldung.websockets.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;
import java.util.function.Consumer;

import static org.springframework.messaging.support.MessageBuilder.withPayload;

@RequiredArgsConstructor
@Slf4j
@RestController
public class TaskController {
    private final StreamBridge streamBridge;
    private final SimpMessagingTemplate webSocket;

    @MessageMapping("/task")
    public void submitTaskOverWebsockets(TaskRequest taskRequest) throws Exception {
        Message<String> requestMessage = withPayload(taskRequest.task())
                .setHeader("CORRELATION_ID", taskRequest.uuid())
                .build();
        log.info("Sending message over queue: " + taskRequest.task());
        if (true) {
            throw new IllegalArgumentException();
        }
        streamBridge.send("task-request-out", requestMessage);
    }

//    @MessageExceptionHandler
//    @SendToUser("/queue/errors")
//    public String handleExceptionAutoReturn(Throwable exception) {
//        log.error("Caught exception " + exception, exception);
//        return exception.toString();
//    }

    @MessageExceptionHandler
    public void handleExceptionManualRoute(Throwable exception, Principal principal) {
        String username  = principal.getName();
//        String username  = SecurityContextHolder.getContext().getAuthentication().getName();
        log.error("Caught exception " + exception + " Sending to " + username, exception);
//        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
//        headerAccessor.setSessionId(username);
//        headerAccessor.setLeaveMutable(true);
//        webSocket.convertAndSendToUser(username, "/queue/errors", "ERROR: " + exception, headerAccessor.getMessageHeaders());

        webSocket.convertAndSendToUser(username, "/queue/errors", exception.toString());
//        webSocket.convertAndSend("/user/admin/queue/errors", "ERROR:" + exception.getMessage());
    }

    @PostMapping("/submit-task")
    public String submitTaskOverRest(@RequestBody String task) throws Exception {
        String uuid = UUID.randomUUID().toString();
        Message<String> requestMessage = withPayload(task)
                .setHeader("CORRELATION_ID", uuid)
                .build();
        log.info("Sending message over queue: " + task);
        streamBridge.send("task-request-out", requestMessage);
        return uuid;
    }


    @Bean
    public Consumer<Message<String>> handleTaskResponse() {
        return taskResponseMessage -> {
            log.info("Got task status: " + taskResponseMessage);
            String correlationId = taskResponseMessage.getHeaders().get("CORRELATION_ID", String.class);
            TaskStatus status = TaskStatus.valueOf(taskResponseMessage.getPayload());
            TaskStatusResponse taskStatusResponse = new TaskStatusResponse(correlationId, status);
            webSocket.convertAndSend("/topic/task-status", taskStatusResponse);
        };
    }
}
