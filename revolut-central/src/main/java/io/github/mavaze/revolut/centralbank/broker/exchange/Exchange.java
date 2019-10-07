package io.github.mavaze.revolut.centralbank.broker.exchange;

import io.github.mavaze.revolut.centralbank.broker.Acknowledge;
import io.github.mavaze.revolut.centralbank.broker.Consumer;
import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.centralbank.broker.message.MessageHandler;
import io.github.mavaze.revolut.commons.domain.Identifiable;
import io.github.mavaze.revolut.commons.domain.Routable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

@Slf4j
public class Exchange implements Consumer, Closeable {

    private final String name;

    private QueueResolver queueResolver = new QueueResolver();

    private Queue<? extends Routable> messageQueue;
    private Queue<? extends Routable> replyQueue;

    @Getter
    private volatile boolean running = false;

    public Exchange(String name) {
        this.name = name;
    }

    public <T extends Routable & Identifiable> void setMessageHandler(MessageHandler<T> handler) {
        messageQueue = new Queue<>(handler);
    }

    public <T extends Routable & Identifiable> void setReplyHandler(MessageHandler<T> handler) {
        replyQueue = new Queue<>(handler);
    }

    public <T extends Routable> void send(Message<T> message) throws Exception {
        Queue resolvedQueue = queueResolver.resolve(message);
        resolvedQueue.send(message);
    }

    @Override
    public <T extends Routable> void consume(Message<T> message) throws Exception {
        this.send(message);
    }

    @Override
    public void close() {
        messageQueue.close();
        replyQueue.close();
    }

    private class QueueResolver {

        private Queue<? extends Routable> resolve(Message message) {
            if(message instanceof Acknowledge) {
                return Exchange.this.replyQueue;
            }
            return Exchange.this.messageQueue;
        }
    }
}
