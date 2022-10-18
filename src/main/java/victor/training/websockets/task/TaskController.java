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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import victor.training.websockets.TimeUtils;
import victor.training.websockets.chat.OutputMessage;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.springframework.messaging.support.MessageBuilder.withPayload;

@RequiredArgsConstructor
@Slf4j
@RestController
public class TaskController {
    private final StreamBridge streamBridge;
    private final SimpMessagingTemplate webSocket;


    @MessageMapping("/task") // SEND peste WS din browser
    public void submitTaskOverWebsockets(TaskRequest taskRequest, Principal principal) throws Exception {
        // prefer sa iau requestul de la browser peste rest

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

    @Async // cere lui spring sa execute munca asicnron, returnad instant 200 la client.
    @PostMapping("/submit-task")
    public void submitTaskOverRest(@RequestBody String task, Principal principal) throws Exception {
//        if (Math.random() < 0.3) throw new RuntimeException("Life is not perfect. Exceptions while sending the message.");
        //Cap1: requestul dureaza mult in mem mea
        log.info("Starting long running task for user " +principal.getName());
        TimeUtils.sleepq(3); // 10 min inchipuieti
        log.info("Ending long running task");

        // aici dupa ce am transpirat facand tasku, tre' sa anunt browserul
        webSocket.convertAndSend("/topic/task-done", task + " GATA");
    }




        //Cap2: trimit mesaj pe o coada catre alt sistem
        //        Message<String> requestMessage = withPayload(taskRequest.task())
        //                .setHeader("REQUESTER_USERNAME", principal.getName())
        //                .build();
        //        log.info("Sending message over queue: " + taskRequest.task());
        //        streamBridge.send("taskRequest-out-0", requestMessage);

        //        sendMessageSink.tryEmitNext(requestMessage); // Reactive way of sending
        // TODO debate: when should I give a UUID back to the browser?
        // TODO debate: when should I send a UUID in the message forward?

    //    public static final Sinks.Many<Message<String>> sendMessageSink = Sinks.many().unicast().onBackpressureBuffer();
    //    @Bean
    //    public Supplier<Flux<Message<String>>> taskRequest() {
    //        return sendMessageSink::asFlux;
    //    }

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
            if (requesterUsername == null) {
                log.warn("No REQUESTER_USERNAME header found in incoming message!");
                return;
            }
            //            webSocket.convertAndSendToUser(requesterUsername, "/queue/task-status", responseMessageFromQueue);
            webSocket.convertAndSend("/topic/task-status", responseMessageFromQueue);
        };
    }

    @Scheduled(fixedRate = 20000)
    public void onExternalSignal_orPollingStatus() {
        webSocket.convertAndSend("/topic/mainframe_down",
                "Mainframe outage at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
