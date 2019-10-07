package io.github.mavaze.revolut.centralbank.broker;

import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.commons.domain.Routable;
import lombok.NonNull;

public interface Consumer {
    <T extends Routable> void consume(@NonNull Message<T> message) throws Exception;
}
