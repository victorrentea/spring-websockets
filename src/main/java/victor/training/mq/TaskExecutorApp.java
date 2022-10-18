package victor.training.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import victor.training.websockets.TimeUtils;

import java.util.function.Function;
@Slf4j
@SpringBootApplication
public class TaskExecutorApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TaskExecutorApp.class)
                .properties("server.port=8081")
                .run(args);
    }

    @Bean
    public Function<Message<String>,Message<String>> executeTask() {
        return taskRequestMessage-> {
            log.info("Processing task in a separate app: Start...");
            TimeUtils.sleepq(1);
            log.info("Processing task: END");
            return MessageBuilder.createMessage(taskRequestMessage.getPayload() + " ... OK", taskRequestMessage.getHeaders());
        };
    }
}
