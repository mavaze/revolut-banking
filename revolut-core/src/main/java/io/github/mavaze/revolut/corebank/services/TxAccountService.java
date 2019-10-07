package io.github.mavaze.revolut.corebank.services;

import io.github.mavaze.revolut.commons.domain.BankAccount;
import io.github.mavaze.revolut.commons.domain.Transaction;
import io.github.mavaze.revolut.commons.utils.NameIdGenerator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.Optional;

import static io.github.mavaze.revolut.commons.domain.TransactionStatus.CREDITED;

@Slf4j
@Singleton
public class TxAccountService extends AccountService {

    @Inject
    public TxAccountService(final BankService bankService) {
        super(bankService);
    }

    public BankAccount addBankAccount(@NonNull String bankCode, @NonNull String bic, @NonNull String name, @NonNull Long initialBalance) {
        final BankAccount account = super.addBankAccount(bankCode, bic, name);
        final Transaction firstTx = Transaction.builder()
                .transactionId(NameIdGenerator.generateId())
                .amount(initialBalance)
                .status(CREDITED)
                .transactionDate(new Date())
                .description("Opening Balance").build();
        addAmountToAccount(account, firstTx);
        return account;
    }

    public Optional<Transaction> findAccountTransactionById(@NonNull final String iban, @NonNull final Integer transactionId) {
        return super.findByIban(iban).getTransactions().stream()
                .filter(tx -> tx.getTransactionId().equals(transactionId))
                .findFirst();
    }

    public Long addAmountToAccount(@NonNull final BankAccount toAccount, @NonNull final Transaction transaction) {
        toAccount.getTransactions().add(transaction);
        log.debug("Saving {} to {}", transaction, toAccount);
        return super.addAmountToAccount(toAccount, transaction.getAmount());
    }

    public Long addAmountToAccount(@NonNull final String toAccount, @NonNull final Transaction transaction) {
        final BankAccount account = findByIban(toAccount);
        return addAmountToAccount(account, transaction);
    }

    public Long deductAmountFromAccount(@NonNull final BankAccount fromAccount, @NonNull final Transaction transaction) {
        fromAccount.getTransactions().add(transaction);
        return super.deductAmountFromAccount(fromAccount, transaction.getAmount());
    }

    public Long deductAmountFromAccount(@NonNull final String fromAccount, @NonNull final Transaction transaction) {
        final BankAccount account = findByIban(fromAccount);
        return deductAmountFromAccount(account, transaction);
    }
}
