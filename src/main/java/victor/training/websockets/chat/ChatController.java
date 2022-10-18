package victor.training.websockets.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Locale;

@Controller
public class ChatController {

    @MessageMapping("/chat")// asta inregistreaza functia asta ca handler pentru cand BROW scrie serverului
    @SendTo("/topic/chat") // trimite pe acest topic mesajul returnat de aceasta metoda
    public OutputMessage send(MessageRequest messageRequest) throws Exception {
        // TODO word filter
        return new OutputMessage(messageRequest.getFrom(), messageRequest.getText().toUpperCase(Locale.ROOT));
    }

}
