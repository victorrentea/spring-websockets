//package victor.training.websockets.task;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.cloud.stream.function.StreamBridge;
//import org.springframework.context.annotation.Bean;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageHeaders;
//import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.messaging.simp.annotation.SendToUser;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
////import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Sinks;
//import victor.training.websockets.TimeUtils;
//import victor.training.websockets.chat.OutputMessage;
//
//import javax.annotation.PostConstruct;
//import java.security.Principal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Map;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//
//import static org.springframework.messaging.support.MessageBuilder.withPayload;
//
//@RequiredArgsConstructor
//@Slf4j
//@RestController
//public class TaskController {
//    private final StreamBridge streamBridge; // spring cloud "stream" de mesaj
//    private final SimpMessagingTemplate webSocket;
//
//    // ----- de evitat: BROW trimite catre Server -----
//    @MessageMapping("/task") // SEND peste WS din browser
//    public void submitTaskOverWebsockets(TaskRequest taskRequest, Principal principal) throws Exception {
//        // prefer sa iau requestul de la browser peste rest
//
//    }
//
//    // handle exceptions in a @MessageMapping method in this class, by returning errors
//    // to the same user that sent the original message, on /user/{username}/queue/errors
//    @MessageExceptionHandler
//    @SendToUser("/queue/errors")
//    public String handleExceptionsInWebsocketRequest(Throwable exception) {
//        log.error("Caught exception " + exception, exception);
//        return exception.toString();
//
//        // alternatives to @SendToUser, that work from any code (not only from a @MessageMapping or @MessageExceptionHandler):
//        // webSocket.convertAndSendToUser(principal.getName(), "/queue/errors", exception.toString());
//        // webSocket.convertAndSend("/user/"+principal.getName()+"/queue/errors",exception.toString());
//    }
//    // -----END de evitat: BROW trimite catre Server -----
//
//
//    @PostConstruct
//    public void enableSecurityContextPropagationOverAsyncCalls() {
//        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
//    }
//
//    @Async // cere lui spring sa execute munca asicnron, returnad instant 200 la client.
//    @PostMapping("/submit-task")
//    public void submitTaskOverRest(@RequestBody String task) throws Exception {
//        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        try {
//            bizLogic();
//            webSocket.convertAndSend("/user/" + currentUsername + "/queue/task-done", task + " GATA");
//        } catch (Exception e) {
//            webSocket.convertAndSend("/user/" + currentUsername + "/queue/errors", e.toString());
//        }
//    }
//
//    private void bizLogic() {
//        if (Math.random() < 0.3)
//            throw new RuntimeException("Life is not perfect. Exceptions while sending the message.");
//        //Cap1: requestul dureaza mult in mem mea
//        log.info("Starting long running task for user ");
//        TimeUtils.sleepq(3); // 10 min inchipuieti
//        log.info("Ending long running task");
//    }
//
//
//    @PostMapping("/submit-task-q")
//    public void submitTaskOverRestQ(@RequestBody String task, Principal principal) {
//        Map<String, Object> headers = Map.of("REQUESTER_USERNAME", principal.getName());
//        Message<String> message = MessageBuilder.createMessage(task, new MessageHeaders(headers));
//        streamBridge.send("taskRequest-out-0", message);
//        // trimit pe channelul 'taskRequest-out-0' - un nume intern dat cozii/topicului extern din MQ
//    }
//
//
//    //Cap2: trimit mesaj pe o coada catre alt sistem
//    //        Message<String> requestMessage = withPayload(taskRequest.task())
//    //                .setHeader("REQUESTER_USERNAME", principal.getName())
//    //                .build();
//    //        log.info("Sending message over queue: " + taskRequest.task());
//    //        streamBridge.send("taskRequest-out-0", requestMessage);
//
//
//    // REST
//    @ResponseBody
//    @ResponseStatus
//    @ExceptionHandler(Exception.class) // usually in a global @RestControllerAdvice
//    public String handleExceptionInRest(Exception e) {
//        return e.toString();
//    }
//
//
//    @Bean
//    public Consumer<Message<String>> handleTaskResponse() {
//        // handler de mesaje venite pe o coada de reply de la alta app.
//        return taskResponseMessage -> {
//            // cine cheama acest cod? Rabbit. threadul pe care esti nu vine din WEB, deci nu are SecurityContextHolder
//            // Cum puii mei obtin eu aici userul care a initiat fluxul? ca sa-l pot notifica in Brow?
//            String username = (String) taskResponseMessage.getHeaders().get("REQUESTER_USERNAME");
//            log.info("Got task status: " + taskResponseMessage + " from user: " + username);
//            String responseMessageFromQueue = taskResponseMessage.getPayload();
//            webSocket.convertAndSend("/user/" + username + "/queue/task-done", responseMessageFromQueue);
//            log.info("STOMP message sent to browsers");
//        };
//    }
//
//    @Scheduled(fixedRate = 20000)
//    public void onExternalSignal_orPollingStatus() {
//        webSocket.convertAndSend("/topic/mainframe_down",
//                "Mainframe outage at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
//    }
//}
