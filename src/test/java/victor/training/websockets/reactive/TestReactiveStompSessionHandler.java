package victor.training.websockets.reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.lang.reflect.Type;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class TestReactiveStompSessionHandler<T> implements StompSessionHandler {
    private final String destinationToSubscribe;
    private final Class<T> frameClass;
    private final Sinks.Many<T> sink = Sinks.many().unicast().onBackpressureBuffer();
    private StompSession session;
    private Flux<T> frameFlux = sink.asFlux().doOnTerminate(() -> {
        if (session != null) {
            session.disconnect();
        }
    });

    public TestReactiveStompSessionHandler(String destinationToSubscribe, Class<T> frameClass) {
        this.destinationToSubscribe = destinationToSubscribe;
        this.frameClass = frameClass;
    }

    public Flux<T> getFrameFlux() {
        return frameFlux;
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
        log.info("Connected to the WebSocket ...");
        this.session = session; // for later disconnection

        session.subscribe(destinationToSubscribe, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    System.out.println("Received payload: " + payload);
                    assertThat(payload).isNotNull();
                    assertThat(payload).isInstanceOf(frameClass);

                    T frame = (T) payload;
                    sink.tryEmitNext(frame);

                } catch (Throwable t) {
                    log.error("There is an exception ", t);
                    sink.tryEmitError(t);
                } finally {
//                    session.disconnect();
//                    firstFrameFuture.completeExceptionally(new IllegalArgumentException("Impossible to get here:)"));
                }

            }
        });
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
