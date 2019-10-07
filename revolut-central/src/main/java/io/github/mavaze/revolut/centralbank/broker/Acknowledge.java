package io.github.mavaze.revolut.centralbank.broker;

import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.commons.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.Map;

import static io.github.mavaze.revolut.centralbank.broker.message.MessageUtils.*;

@Getter
@ToString
@AllArgsConstructor
public abstract class Acknowledge<T extends Identifiable> implements Message<T> {

    private T payload;

    @Override
    //@ToString.Include(name = "headers")
    public Map<String, Object> getHeaders() {
        Map<String, Object> headers = getOrigHeaders();
        swapHeaders(SENDER, RECIPIENT, headers);
        swapHeaders(ROUTING_KEY, REPLY_KEY, headers);
        swapHeaders(MESSAGE_ID, REFERENCE_ID, headers);
        headers.put(MESSAGE_ID, payload.getId());
        headers.put(MESSAGE_DATE, new Date());
        return headers;
    }

    protected abstract Map<String, Object> getOrigHeaders();

    public static abstract class Ack<T extends Identifiable> extends Acknowledge<T> {
        public Ack(T payload) {
            super(payload);
        }
    }

    public static abstract class Nack<T extends Identifiable> extends Acknowledge<T> {
        public Nack(T payload) {
            super(payload);
        }
    }
}
