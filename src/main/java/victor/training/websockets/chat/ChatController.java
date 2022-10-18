package victor.training.websockets.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate webSocket;

    @GetMapping("post-message")
    public void method() {
        webSocket.convertAndSend("/topic/chat", new OutputMessage("fictiv", "frumos"));
    }


    @MessageMapping("/chat")// asta inregistreaza functia asta ca handler pentru cand BROW scrie serverului
    // limitarea lui @SendTo este ca cine cheama functia asta trebuie sa vina tot din STOMP : adica tre sa aiba @MessageMapping
    @SendTo("/topic/chat") // trimite pe acest topic mesajul returnat de aceasta metoda
    public OutputMessage send(MessageRequest messageRequest) throws Exception {
        // TODO word filter
        return new OutputMessage(messageRequest.getFrom(), messageRequest.getText().toUpperCase(Locale.ROOT));
    }

}
