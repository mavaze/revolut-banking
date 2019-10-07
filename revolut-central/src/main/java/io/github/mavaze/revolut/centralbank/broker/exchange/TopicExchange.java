package io.github.mavaze.revolut.centralbank.broker.exchange;

import io.github.mavaze.revolut.centralbank.broker.Consumer;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TopicExchange extends Exchange {

    private Map<String, Consumer> consumers = new ConcurrentHashMap<>();

    // Name of global queue can be parameterized
    public static String QUEUE_NAME = "global-queue";
    public static String UNPROCESSED_ROUTING_KEY = "unprocessed";

    public TopicExchange(@NonNull final String queueName) {
        super(queueName);
    }

    public void subscribe(@NonNull final String routingKey, @NonNull final Consumer agent) {
        consumers.putIfAbsent(routingKey, agent);
    }

    public Optional<Consumer> getConsumer(@NonNull final String routingKey) {
        return Optional.ofNullable(consumers.get(routingKey));
    }
}
