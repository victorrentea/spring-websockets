package com.baeldung.websockets.task;

import com.baeldung.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

@Slf4j
@Configuration
public class TaskExecutor {
    @Bean
    public Function<Message<String>,Message<String>> executeTask() {
        return taskRequestMessage-> {
            log.info("Processing task: Start...");
            TimeUtils.sleepq();
            log.info("Processing task: END");
            return MessageBuilder.createMessage(TaskStatus.OK.name(), taskRequestMessage.getHeaders());
        };
    }



}
