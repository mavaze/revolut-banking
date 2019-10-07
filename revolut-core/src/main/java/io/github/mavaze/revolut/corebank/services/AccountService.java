package io.github.mavaze.revolut.corebank.services;

import io.github.mavaze.revolut.commons.domain.Bank;
import io.github.mavaze.revolut.commons.domain.BankAccount;
import io.github.mavaze.revolut.commons.utils.NameIdGenerator;
import io.github.mavaze.revolut.corebank.exceptions.BusinessException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final BankService bankService;

    public BankAccount addBankAccount(@NonNull final String bankCode,
                                      @NonNull final String bic,
                                      @NonNull final String name) {
        final Bank bank = bankService.findByCode(bankCode);
        Bank.Branch branch = bank.getBranches().stream()
                .filter(br -> bic.equals(br.getBic()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("No branch with bic " + bic + " exists."));

        final BankAccount account = new BankAccount(NameIdGenerator.generateId(), name, branch);
        account.setIban(NameIdGenerator.iban(bankCode, branch));
        bank.getAccounts().add(account);
        return account;
    }

    public BankAccount findByBankCodeAndAccountId(@NonNull final String bankCode,
                                                  @NonNull final Integer accountId) throws NotFoundException {
        final Bank bank = bankService.findByCode(bankCode);
        return Optional.ofNullable(bank)
                .map(Bank::getAccounts)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Bank Account with this IBAN doesn't exist."));
    }

    public BankAccount findByIban(@NonNull final String iban) throws NotFoundException {
        final String bankCode = NameIdGenerator.extractBankCode(iban);
        final Bank bank = bankService.findByCode(bankCode);
        return Optional.ofNullable(bank)
                .map(Bank::getAccounts)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(account -> account.getIban().equals(iban))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Bank Account with this IBAN doesn't exist."));
    }

    Long addAmountToAccount(@NonNull final BankAccount account, @NonNull final Long amount) {
        return account.getBalanceMinorUnits().addAndGet(amount);
    }

    Long deductAmountFromAccount(@NonNull final BankAccount account, @NonNull final Long amount) {
        // TODO: better locking mechanism for account level lock, likely maintaining a map of iban vs locks in BankService
        //  For simplicity, as we believe the account being passed is singleton, we are locking on method parameter
        synchronized (account) {
            if(account.getBalanceMinorUnits().longValue() >= amount) {
                return account.getBalanceMinorUnits().addAndGet( - amount);
            }
        }
        throw new BusinessException("Not sufficient balance");
    }
}
