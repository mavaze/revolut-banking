package io.github.mavaze.revolut.centralbank.broker;

import io.github.mavaze.revolut.centralbank.broker.exchange.DirectExchange;
import io.github.mavaze.revolut.centralbank.broker.exchange.Exchange;
import io.github.mavaze.revolut.centralbank.broker.exchange.TopicExchange;
import io.github.mavaze.revolut.centralbank.broker.exchange.Topology;
import lombok.NonNull;

import javax.inject.Singleton;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class MessageBroker implements Closeable {

    private final Object LOCK = new Object();

    private Map<String, Exchange> exchanges = new ConcurrentHashMap<>();

    @Override
    public void close() {
        exchanges.values().forEach(Exchange::close);
    }

    public Exchange exchange(@NonNull final String queueName, Topology type) {
        Exchange exchange = exchanges.get(queueName);

        if(exchange == null) {
            synchronized (LOCK) {
                exchange = exchanges.get(queueName);
                if(exchange == null) {
                    exchange = instantiateExchange(queueName, type);
                    exchanges.put(queueName, exchange);
                }
            }
        }
        return exchange;
    }

    private Exchange instantiateExchange(String queueName, Topology type) {
        switch(type) {
            case DIRECT:
                return new DirectExchange(queueName);
            case TOPIC:
                return new TopicExchange(queueName);
            default:
                throw new RuntimeException("This queue type has no implementation yet");
        }
    }
}
