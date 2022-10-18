package victor.training.websockets.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate webSocket;

    @GetMapping("post-message")
    public String method(HttpServletRequest request) {
        webSocket.convertAndSend("/topic/chat", new OutputMessage("fictiv", "frumos"));
        HttpSession session = request.getSession();
        return session.getId();
    }


    @MessageMapping("/chat")// asta inregistreaza functia asta ca handler pentru cand BROW scrie serverului
    // limitarea lui @SendTo este ca cine cheama functia asta trebuie sa vina tot din STOMP : adica tre sa aiba @MessageMapping
    @SendTo("/topic/chat") // trimite pe acest topic mesajul returnat de aceasta metoda
    public OutputMessage send(MessageRequest messageRequest, Principal principal) throws Exception {
        // TODO word filter

        return new OutputMessage(principal.getName(), messageRequest.getText().toUpperCase(Locale.ROOT));
//        return new OutputMessage(messageRequest.getFrom(), messageRequest.getText().toUpperCase(Locale.ROOT));
    }

}
