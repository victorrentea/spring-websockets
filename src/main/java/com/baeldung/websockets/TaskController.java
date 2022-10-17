package com.baeldung.websockets;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import static org.springframework.messaging.support.MessageBuilder.withPayload;

@RequiredArgsConstructor
@Slf4j
@Controller
public class TaskController {
    private final StreamBridge streamBridge;

    @MessageMapping("/task")
    public void send(TaskRequest taskRequest) throws Exception {
        Message<String> requestMessage = withPayload(taskRequest.task)
                .setHeader("CORRELATION_ID", taskRequest.uuid)
                .build();
        log.info("Sending message over queue: " + taskRequest);
        streamBridge.send("task-request-out", requestMessage);
    }
}
