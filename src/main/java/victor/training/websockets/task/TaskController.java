package victor.training.websockets.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.function.Consumer;

import static org.springframework.messaging.support.MessageBuilder.withPayload;

@RequiredArgsConstructor
@Slf4j
@RestController
public class TaskController {
    private final StreamBridge streamBridge;
    private final SimpMessagingTemplate webSocket;

    @MessageMapping("/task")
    public void submitTaskOverWebsockets(TaskRequest taskRequest, Principal principal) throws Exception {
        Message<String> requestMessage = withPayload(taskRequest.task())
                .setHeader("REQUESTER_USERNAME", principal.getName())
                .build();
        log.info("Sending message over queue: " + taskRequest.task());
        if (Math.random() < 0.3) throw new RuntimeException("Life is not perfect. Exceptions while sending the message.");
        streamBridge.send("task-request-out", requestMessage);
    }

    // handle exceptions in a @MessageMapping method in this class, by returning errors
    // to the same user that sent the original message, on /user/{username}/queue/errors
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleExceptionsInWebsocketRequest(Throwable exception) {
        log.error("Caught exception " + exception, exception);
        return exception.toString();

        // alternatives to @SendToUser:
        // webSocket.convertAndSendToUser(principal.getName(), "/queue/errors", exception.toString());
        // webSocket.convertAndSend("/user/"+principal.getName()+"/queue/errors",exception.toString());
    }

    @PostMapping("/submit-task")
    public void submitTaskOverRest(@RequestBody String task, Principal principal) throws Exception {
        if (Math.random() < 0.3) throw new RuntimeException("Life is not perfect. Exceptions while sending the message.");

        Message<String> requestMessage = withPayload(task)
                .setHeader("REQUESTER_USERNAME", principal.getName())
                .build();
        log.info("Sending message over queue: " + task);
        streamBridge.send("task-request-out", requestMessage);
        // TODO debate: when should I give a UUID back to the browser?
    }

    @ResponseBody
    @ResponseStatus
    @ExceptionHandler(Exception.class)
    public String handleExceptionInRest(Exception e) {
        return e.toString();
    }


    @Bean
    public Consumer<Message<String>> handleTaskResponse() {
        return taskResponseMessage -> {
            log.info("Got task status: " + taskResponseMessage);
            String requesterUsername = taskResponseMessage.getHeaders().get("REQUESTER_USERNAME", String.class);
            String responseMessageFromQueue = taskResponseMessage.getPayload();
            webSocket.convertAndSendToUser(requesterUsername, "/queue/task-status", responseMessageFromQueue);
        };
    }
}
