package victor.training.websockets.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OutputMessage {

    private String from;
    private String text;
    private String time =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

    public OutputMessage(String from, String text) {

        this.from = from;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getFrom() {
        return from;
    }
}
