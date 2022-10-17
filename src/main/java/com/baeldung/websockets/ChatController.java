package com.baeldung.websockets;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public OutputMessage send(MessageRequest messageRequest) throws Exception {
        return new OutputMessage(messageRequest.getFrom(), messageRequest.getText());
    }

}
