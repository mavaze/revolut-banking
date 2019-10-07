package io.github.mavaze.revolut.centralbank.broker.message;

import io.github.mavaze.revolut.centralbank.broker.Acknowledge;
import io.github.mavaze.revolut.commons.domain.Identifiable;
import io.github.mavaze.revolut.commons.domain.Routable;

import java.util.Optional;

public interface MessageHandler<T extends Routable & Identifiable> {

    Optional<Acknowledge> onMessage(Message<T> message);

    void onAcknowledge(Acknowledge<T> acknowledge) throws Exception;
}
