package io.github.mavaze.revolut.commons.domain;

public interface BankOperations {

    String getBankCode();

    default String getQueueName() {
        return "queue-int-" + getBankCode();
    }

}
