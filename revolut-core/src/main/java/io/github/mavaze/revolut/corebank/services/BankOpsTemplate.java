package io.github.mavaze.revolut.corebank.services;

import io.github.mavaze.revolut.centralbank.broker.MessageBroker;
import io.github.mavaze.revolut.centralbank.broker.exchange.DirectExchange;
import io.github.mavaze.revolut.centralbank.broker.exchange.TopicExchange;
import io.github.mavaze.revolut.corebank.eventing.BankCreatedEvent;
import io.github.mavaze.revolut.corebank.eventing.BankingEvent;
import io.github.mavaze.revolut.corebank.eventing.EventPublisher;
import io.github.mavaze.revolut.corebank.eventing.EventSubscriber;
import io.github.mavaze.revolut.corebank.handlers.DirectAcknowledgeHandler;
import io.github.mavaze.revolut.corebank.handlers.DirectMessageHandler;
import io.github.mavaze.revolut.corebank.routing.Router;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.github.mavaze.revolut.centralbank.broker.exchange.Topology.DIRECT;
import static io.github.mavaze.revolut.centralbank.broker.exchange.Topology.TOPIC;

@Slf4j
@Singleton
public class BankOpsTemplate implements EventSubscriber {

    @Inject
    private Router router;

    @Inject
    private MessageBroker messageBroker;

    @Inject
    private TxAccountService accountService;

    @Inject
    public BankOpsTemplate(@NonNull final EventPublisher publisher) {
        // TODO: move subscription call from constructor to some postConstruct
        publisher.subscribe(this);
    }

    @Override
    public boolean subscribedFor(@NonNull final BankingEvent event) {
        return event.getClass().isAssignableFrom(BankCreatedEvent.class);
    }

    @Override
    public void submit(@NonNull final BankingEvent event) {
        if (event instanceof BankCreatedEvent) {
            BankCreatedEvent e = (BankCreatedEvent) event;
            registerBankOperations(e.getBankCode(), e.getQueueName());
        }
    }

    private void registerBankOperations(@NonNull String bankCode, String queueName) {
        // Setup its own internal queue
        log.info("Creating exchange for bank with code {} and registering it with central exchange.", queueName);
        final DirectExchange directExchange = (DirectExchange) messageBroker.exchange(queueName, DIRECT);
        directExchange.setMessageHandler(new DirectMessageHandler(router, accountService));
        directExchange.setReplyHandler(new DirectAcknowledgeHandler(accountService));

        // Subscribe to global queue
        TopicExchange topicExchange = (TopicExchange) messageBroker.exchange(TopicExchange.QUEUE_NAME, TOPIC);
        topicExchange.subscribe(bankCode, directExchange);
    }
}
