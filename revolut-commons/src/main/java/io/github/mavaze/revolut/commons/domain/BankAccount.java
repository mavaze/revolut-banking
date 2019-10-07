package io.github.mavaze.revolut.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@EqualsAndHashCode(of = "iban")
@NoArgsConstructor
@ToString(exclude = {"balanceMinorUnits", "branch", "transactions"})
public class BankAccount implements Serializable {

    private Integer accountId;
    private String name;

    @Setter
    private String iban;

    @Setter(AccessLevel.NONE)
    private String bic;

    @JsonIgnore
    private Bank.Branch branch;

    // This property is added to represent amounts in decimals, otherwise all
    // computations are intended to be done with in minor units represented in Long.
    // A viable alternative to this combination is using BigDecimal throughout.
    @Setter(AccessLevel.NONE)
    private double balance;

    @JsonIgnore
    private boolean active = false;

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private AtomicLong balanceMinorUnits;

    @JsonIgnore
    private NavigableSet<Transaction> transactions = new ConcurrentSkipListSet<>();

    public BankAccount(Integer accountId, String name, Bank.Branch branch, Long initialBalance) {
        this.accountId = accountId;
        this.name = name;
        this.branch = branch;
        balanceMinorUnits = new AtomicLong(initialBalance);
        this.active = true;
    }

    public BankAccount(Integer accountId, String name, Bank.Branch branch) {
        this(accountId, name, branch, 0L);
    }

    @JsonIgnore
    public Bank getBank() {
        return this.branch.getBank();
    }

    public String getBic() {
        return this.branch.getBic();
    }

    public double getBalance() {
        return balanceMinorUnits.doubleValue();
    }
}
