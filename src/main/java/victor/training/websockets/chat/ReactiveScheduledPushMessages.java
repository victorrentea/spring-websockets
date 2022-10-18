package victor.training.websockets.chat;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import victor.training.websockets.chat.OutputMessage;

import java.time.Duration;

//@Service
public class ReactiveScheduledPushMessages implements InitializingBean {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public ReactiveScheduledPushMessages(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Flux.interval(Duration.ofSeconds(4L))
            .map((n) -> new OutputMessage("angel", "Be kind in code reviews"))
            .subscribe(message -> simpMessagingTemplate.convertAndSend("/topic/messages", message));
    }
}
