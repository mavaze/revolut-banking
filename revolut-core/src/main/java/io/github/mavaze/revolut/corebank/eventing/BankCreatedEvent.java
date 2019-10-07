package io.github.mavaze.revolut.corebank.eventing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BankCreatedEvent implements BankingEvent {
    private final String bankCode;
    private final String queueName;
}
