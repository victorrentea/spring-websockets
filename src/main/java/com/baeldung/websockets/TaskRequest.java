package com.baeldung.websockets;

import lombok.Data;

@Data
public class TaskRequest {
    String uuid;
    String task;
}
