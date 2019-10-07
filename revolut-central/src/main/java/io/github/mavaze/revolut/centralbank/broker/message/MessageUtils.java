package io.github.mavaze.revolut.centralbank.broker.message;

import io.github.mavaze.revolut.centralbank.broker.Acknowledge;
import io.github.mavaze.revolut.commons.domain.Identifiable;
import io.github.mavaze.revolut.commons.domain.Routable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageUtils {

    public final static String SENDER = "X-reply-to";
    public final static String REPLY_KEY = "X-reply-key";

    public final static String RECIPIENT = "X-message-to";
    public final static String ROUTING_KEY = "X-routing-key";

    public final static String MESSAGE_ID = "X-message-id";
    public final static String REFERENCE_ID = "X-reference-id";
    public final static String MESSAGE_DATE = "X-message-date";

    private static <T> Message<T> build(String guid, T payload, Map<String, Object> properties) {

        return new Message<T>() {

            @Override
            public Map<String, Object> getHeaders() {
                final Map<String, Object> headers = new HashMap<>();
                headers.put(MESSAGE_ID, guid);
                headers.put(MESSAGE_DATE, new Date());
                if(properties != null) {
                    headers.putAll(properties);
                }
                return headers;
            }

            @Override
            public T getPayload() {
                return payload;
            }

            @Override
            public String toString() {
                return String.format("Message(payload=%s,headers=%s)",
                        getPayload().getClass().getSimpleName() , getHeaders());
            }
        };
    }

    private static <T> Message<T> build(String guid, T payload, String sender, String receiver, String routingKey, String replyKey) {
        return build(guid, payload, new HashMap<String, Object>() {
            private static final long serialVersionUID = -807086164157594423L;
            {
                put(SENDER, sender);
                put(RECIPIENT, receiver);
                put(ROUTING_KEY, routingKey);
                put(REPLY_KEY, replyKey);
            }
        });
    }

    public static <T extends Routable<String>> Message<T> build(T payload, String routingKey, String replyKey) {
        String id;
        if(payload instanceof Identifiable) {
            id = ((Identifiable) payload).getId().toString();
        } else {
            id = UUID.randomUUID().toString();
        }
        return build(id, payload, payload.getSender(), payload.getReceiver(), routingKey, replyKey);
    }

    public static <T> String getGuid(Message<T> message) {
        return (String) message.getHeaders().get(MESSAGE_ID);
    }

    @SuppressWarnings("unchecked")
    public static <S, T extends Routable<S>> S getSender(Message<T> message) {
        return (S) message.getHeaders().get(SENDER);
    }

    @SuppressWarnings("unchecked")
    public static <S, T extends Routable<S>> S getReceiver(Message<T> message) {
        return (S) message.getHeaders().get(RECIPIENT);
    }

    public static <T> String getRoutingKey(Message<T> message) {
        return (String) message.getHeaders().get(ROUTING_KEY);
    }

    public static <T extends Identifiable> Acknowledge.Ack<T> ack(Message<T> message, T payload) {
        return new Acknowledge.Ack<T>(payload) {

            @Override
            protected Map<String, Object> getOrigHeaders() {
                return message.getHeaders();
            }
        };
    }

    public static <T extends Identifiable> Acknowledge.Nack<T> nack(Message<T> message, T payload) {
        return new Acknowledge.Nack<T>(payload) {

            @Override
            protected Map<String, Object> getOrigHeaders() {
                return message.getHeaders();
            }
        };
    }

    public static void swapHeaders(String prop1, String prop2, Map<String, Object> headers) {
        Object prevProp1 = headers.get(prop1);
        headers.put(prop1, headers.get(prop2));
        headers.put(prop2, prevProp1);
    }
}
