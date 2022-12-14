package victor.training.websockets.future;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.*;
import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class TestStompSessionHandler<T> implements StompSessionHandler {
    private final String destinationToSubscribe;
    private final Class<T> frameClass;
    private final CompletableFuture<T> firstFrameFuture = new CompletableFuture<>();
    private final CompletableFuture<Void> connectedFuture = new CompletableFuture<>();

    public TestStompSessionHandler(String destinationToSubscribe, Class<T> frameClass) {
        this.destinationToSubscribe = destinationToSubscribe;
        this.frameClass = frameClass;
    }

    public CompletableFuture<T> getFirstFrameFuture() {
        return firstFrameFuture;
    }

    public CompletableFuture<Void> getConnectedFuture() {
        return connectedFuture;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return null;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        log.info("Connected to the WebSocket {} ...", connectedHeaders);
        session.subscribe(destinationToSubscribe, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return frameClass;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    System.out.println("Received payload: " + payload);
                    assertThat(payload).isNotNull();
                    assertThat(payload).isInstanceOf(frameClass);

                    T map = (T) payload;
                    firstFrameFuture.complete(map);

                } catch (Throwable t) {
                    log.error("There is an exception ", t);
                    firstFrameFuture.completeExceptionally(t);
                } finally {
                    session.disconnect();
                    firstFrameFuture.completeExceptionally(new IllegalArgumentException("Impossible to get here:)"));
                }

            }
        });

        runAsync(() -> connectedFuture.complete(null), delayedExecutor(100, TimeUnit.MILLISECONDS));
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        throw new IllegalArgumentException("Unexpected error", exception);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        throw new IllegalArgumentException("Unexpected error", exception);
    }
}
