package io.github.mavaze.revolut.commons.utils;

import io.github.mavaze.revolut.commons.domain.Bank;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class NameIdGenerator {

    private static AtomicInteger counter = new AtomicInteger(new Random().nextInt(999999999));

    public static String iban(String bankCode, String bic) {
        String suffix = String.format("%08d", new Random().nextInt(9999999));
        return bankCode + bic + suffix;
    }

    public static String iban(String bankCode, Bank.Branch branch) {
        return iban(bankCode, branch.getBic());
    }

    public static String extractBankCode(String iban) {
        return iban.substring(0, 4);
    }

    // Main usage is to generate incrementing transaction Ids. This should be part of a separate class meant for transactions
    // Having it here as static method make anyone can call at will. Anyways idea is to get unique incrementing numbers,
    // not necessarily be consecutive transaction ids.
    public static Integer generateId() {
        return counter.incrementAndGet();
    }
}
