package victor.training.websockets.task;

import victor.training.websockets.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

@Slf4j
@Configuration
public class TaskExecutorBean {
    @Bean
    public Function<Message<String>,Message<String>> executeTask() {
        return taskRequestMessage-> {
            log.info("Processing task (in the same app: fake!): Start...");
            TimeUtils.sleepq(1);
            log.info("Processing task: END");
            return MessageBuilder.createMessage(taskRequestMessage.getPayload() + " ... OK", taskRequestMessage.getHeaders());
        };
    }



}
