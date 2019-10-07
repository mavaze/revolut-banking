package io.github.mavaze.revolut.corebank.services;

import io.github.mavaze.revolut.commons.domain.Bank;
import io.github.mavaze.revolut.corebank.eventing.BankCreatedEvent;
import io.github.mavaze.revolut.corebank.eventing.EventPublisher;
import io.github.mavaze.revolut.corebank.exceptions.BusinessException;
import lombok.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class BankService {

    private static Map<String, Bank> registeredBanks = new ConcurrentHashMap<>();

    @Inject
    private EventPublisher publisher;

    public Bank findByCode(@NonNull final String bankCode) {
        return registeredBanks.get(bankCode);
    }

    /**
     * Registers a bank with give bankCode and name in system. With eventing backend queues are generated
     * to facilitate amount transfers within (intra-bank) and across banks (inter-bank)
     * @param bankCode
     * @param name
     * @return
     */
    public Bank addByCodeAndName(@NonNull final String bankCode, @NonNull final String name) {

        if(registeredBanks.get(bankCode) != null) {
            throw new BusinessException(String.format("Bank with requested code %s already exists.", bankCode));
        }

        final Bank bank = new Bank(bankCode, name);
        final String queueName = bank.getBankOperations().getQueueName();
        registeredBanks.put(bankCode, bank);

        publisher.publish(new BankCreatedEvent(bankCode, queueName));

        return bank;
    }

    /**
     * Associates a branch with bank. This step was added mostly to generate BIC assuming BIC corresponds to a branch.
     * Later this BIC is used to associate various bank account and to generate random IBAN numbers using combination
     * of bank code and bic.
     * @param bic
     * @param bankCode
     * @return
     */
    public Bank.Branch addBranchToBank(@NonNull final String bic, @NonNull final String bankCode) {
        final Bank bank;
        if((bank = registeredBanks.get(bankCode)) == null) {
            throw new BusinessException(String.format("Bank with code %s doesn't exist to add a branch for.", bankCode));
        }
        final Bank.Branch branch = new Bank.Branch(bic, bank);
        bank.getBranches().add(branch);
        return branch;
    }
}
