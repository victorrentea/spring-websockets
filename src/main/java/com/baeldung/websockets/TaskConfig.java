package com.baeldung.websockets;

import com.baeldung.TimeUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Configuration
public class TaskConfig {
    @SneakyThrows
    @Bean
    public Function<Message<String>,Message<String>> executeTask() {
        return taskRequestMessage-> {
            log.info("Processing task: Start...");
            TimeUtils.sleepq();
            log.info("Processing task: END");
            return MessageBuilder.createMessage(TaskStatus.OK.name(), taskRequestMessage.getHeaders());
        };
    }


    @Autowired
    private SimpMessagingTemplate webSocket;
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
