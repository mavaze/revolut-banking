package io.github.mavaze.revolut.commons.domain;

public interface Routable<T> {

    T getSender();

    T getReceiver();
}
