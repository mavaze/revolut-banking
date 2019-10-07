package io.github.mavaze.revolut.corebank.routing;

import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.commons.domain.Routable;

public interface Router<T> {

    void route(Routable<T> request) throws Exception;
    void route(Message<? extends Routable<T>> message) throws Exception;
}
