package victor.training.websockets;

public class TimeUtils {
    public static void sleepq(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
