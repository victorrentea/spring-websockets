package com.baeldung;

public class TimeUtils {
    public static void sleepq() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
