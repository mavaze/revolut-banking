package io.github.mavaze.revolut.centralbank.broker.handlers;

import io.github.mavaze.revolut.centralbank.broker.Acknowledge;
import io.github.mavaze.revolut.centralbank.broker.Consumer;
import io.github.mavaze.revolut.centralbank.broker.exchange.TopicExchange;
import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.centralbank.broker.message.MessageHandler;
import io.github.mavaze.revolut.centralbank.broker.message.MessageUtils;
import io.github.mavaze.revolut.commons.domain.Transaction;
import io.github.mavaze.revolut.commons.domain.TxMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static io.github.mavaze.revolut.centralbank.broker.message.MessageUtils.nack;
import static io.github.mavaze.revolut.commons.domain.TransactionStatus.FAILED;

@Slf4j
public class TopicMessageHandler implements MessageHandler<TxMessage> {

    private TopicExchange exchange;

    public TopicMessageHandler(@NonNull final TopicExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public Optional<Acknowledge> onMessage(@NonNull final Message<TxMessage> message) {
        log.debug("Received {}", message);
        final Optional<Consumer> consumer = exchange.getConsumer(MessageUtils.getRoutingKey(message));

        return consumer.map(c -> {
            try {
                c.consume(message);
            } catch (Exception ex) {
                String description = "Failed to send message to the bank specific exchange.";
                log.error("Transfer failed due to {}", description);
                return buildNackMessage(message, description);
            }
            return Optional.<Acknowledge>empty();
        }).orElseGet(() -> {
            String description = "No bank with the given code subscribed to topic.";
            log.error("Transfer failed due to {}", description);
            return buildNackMessage(message, description);
        });
    }

    @Override
    public void onAcknowledge(@NonNull final Acknowledge<TxMessage> acknowledge) {
        this.onMessage(acknowledge);
    }

    private Optional<Acknowledge> buildNackMessage(Message<TxMessage> message, String description) {
        final Transaction tx = (Transaction) message.getPayload().getTransaction().clone();
        tx.setDescription(description);
        tx.setStatus(FAILED);
        return Optional.of(nack(message, new TxMessage(MessageUtils.getSender(message), tx)));
    }
}
