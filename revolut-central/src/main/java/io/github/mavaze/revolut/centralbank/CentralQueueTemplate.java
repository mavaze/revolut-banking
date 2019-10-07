package io.github.mavaze.revolut.centralbank;

import io.github.mavaze.revolut.centralbank.broker.MessageBroker;
import io.github.mavaze.revolut.centralbank.broker.exchange.TopicExchange;
import io.github.mavaze.revolut.centralbank.broker.handlers.TopicMessageHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.github.mavaze.revolut.centralbank.broker.exchange.Topology.TOPIC;

@Slf4j
@Singleton
public class CentralQueueTemplate {

    @Inject
    public CentralQueueTemplate(@NonNull final MessageBroker messageBroker) {
        log.info("Registering central exchange for inter banking operations");
        TopicExchange topicExchange = (TopicExchange) messageBroker.exchange(TopicExchange.QUEUE_NAME, TOPIC);
        TopicMessageHandler handler = new TopicMessageHandler(topicExchange);
        topicExchange.setMessageHandler(handler);
        topicExchange.setReplyHandler(handler);
    }
}
