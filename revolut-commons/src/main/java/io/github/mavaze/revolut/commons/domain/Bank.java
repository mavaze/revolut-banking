package io.github.mavaze.revolut.commons.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "code")
@ToString(of = {"code", "name"})
public class Bank implements Serializable{

    private String code;

    private String name;

    private Set<Branch> branches;

    private Set<BankAccount> accounts;

    @JsonIgnore
    private BankOperations bankOperations;

    public Bank(String code, String name) {
        this.code = code;
        this.name = name;
        this.branches = new CopyOnWriteArraySet<>();
        this.accounts = new CopyOnWriteArraySet<>();
        this.bankOperations = () -> Bank.this.code;
    }

    public Bank(String code, String name, BankOperations bankOperations) {
        this(code, name);
        this.bankOperations = bankOperations;
    }

    @Getter
    @NoArgsConstructor
    @RequiredArgsConstructor
    @ToString(of = {"bic", "bankCode"})
    public static class Branch implements Serializable {

        @NonNull
        private String bic;

        @Setter(AccessLevel.NONE)
        private String bankCode;

        @NonNull
        @JsonIgnore
        private Bank bank;

        private String getBankCode() {
            return bank.code;
        }
    }
}
