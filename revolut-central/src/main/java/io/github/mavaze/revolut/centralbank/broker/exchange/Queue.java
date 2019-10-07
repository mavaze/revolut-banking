package io.github.mavaze.revolut.centralbank.broker.exchange;

import io.github.mavaze.revolut.centralbank.broker.Acknowledge;
import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.centralbank.broker.message.MessageHandler;
import io.github.mavaze.revolut.commons.domain.Identifiable;
import io.github.mavaze.revolut.commons.domain.Routable;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class Queue<T extends Routable & Identifiable> implements Closeable {

    private BlockingQueue<Message<T>> internalQueue = new LinkedBlockingQueue<>();

    private ExecutorService executor;

    private MessageHandler<T> handler;

    @Getter
    private volatile boolean running = true;

    public Queue(MessageHandler<T> handler) {
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = handler;
        final Supplier<Optional<Acknowledge>> task = messageHandlerSupplier();
        recursiveCompletableFuture(task);
    }

    @Override
    @PreDestroy
    public void close() {
        log.info("Shutting services down");
        this.executor.shutdown();
        this.running = false;
    }

    public void send(Message<T> message) throws InterruptedException {
        log.debug("Received {}", message);
        internalQueue.put(message);
    }

    private CompletableFuture<?> recursiveCompletableFuture(final Supplier<Optional<Acknowledge>> task) {
        return CompletableFuture.supplyAsync(task, executor)
                .thenAcceptAsync(acknowledge -> acknowledge.ifPresent(this::sendReply))
                .handleAsync((voidable, throwable) -> {
                    if (running) {
                        return recursiveCompletableFuture(task);
                    }
                    return voidable;
                });
    }

    private Supplier<Optional<Acknowledge>> messageHandlerSupplier() {
        return () -> {
            Message<T> message = null;
            try {
                message = internalQueue.take();
                return handler.onMessage(message);
            } catch (InterruptedException ex) {
                log.error("SEVERE: Process interrupted while retrieving message off queue.", ex);
                return Optional.empty();
            }
        };
    }

    private void sendReply(@NonNull final Acknowledge<T> acknowledge) {
        try {
            handler.onAcknowledge(acknowledge);
        } catch (Exception e) {
            log.error("Failed to reply the processed message. Transactions could be out of sync", e);
            // TODO: Need to have some retry mechanism and/or reporting capability so that this could be handled separately
        }
    }
}
