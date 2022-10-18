package victor.training.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import victor.training.websockets.TimeUtils;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
@Slf4j
@SpringBootApplication
public class TaskExecutorApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TaskExecutorApp.class)
                .properties("server.port=8081")
                .run(args);
    }

    @Autowired
    private StreamBridge streamBridge;

    @Bean
    public Function<Message<String>, Message<String>> executeTask() {
        return inputMessage -> {
                log.trace("Datele pe care voiam sa le vad in memorie x y z" );
                log.info("Start working on " + inputMessage.getPayload());
                TimeUtils.sleepq(3);
                log.info("Finish working on " + inputMessage.getPayload());
                MessageHeaders replyMessageHeaders = inputMessage.getHeaders();
                Message<String> replyMessage = MessageBuilder.createMessage(
                        "Gata AM TERMINAT " + inputMessage.getPayload(), replyMessageHeaders);
                // daca iesi din met de handler fara ex, mesajul e considerat autom ACK (consumat)
                streamBridge.send("taskRequest-out-0", replyMessage);
            return replyMessage;
        };
    }


}
