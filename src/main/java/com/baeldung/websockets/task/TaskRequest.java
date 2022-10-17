package com.baeldung.websockets.task;

public record TaskRequest(
        String uuid,
        String task) {
}
