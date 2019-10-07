package io.github.mavaze.revolut.corebank.eventing;

public interface EventSubscriber {

    boolean subscribedFor(BankingEvent event);

    void submit(BankingEvent event);
}
