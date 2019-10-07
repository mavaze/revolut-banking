package io.github.mavaze.revolut.centralbank.broker.message;

import java.util.Map;

public interface Message<T> {

    Map<String, Object> getHeaders();

    T getPayload();
}
