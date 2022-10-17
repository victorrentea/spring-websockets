package com.baeldung.websockets.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
        streamBridge.send("task-request-out", requestMessage);
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
