package io.github.mavaze.revolut.commons.utils;

import io.github.mavaze.revolut.commons.contract.TxResponse;
import io.github.mavaze.revolut.commons.domain.Bank;
import io.github.mavaze.revolut.commons.domain.BankAccount;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class TestUtils {

    public final static String TRANSACTIONS_BASE_URL = "banks/%s/accounts/%s/transactions";

    public static Bank createAndGetBank(JerseyTest test, String code, String name) {
        return test.target("banks")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(new HashMap<String, String>() {{
                    put("code", code);
                    put("name", name);
                }}))
                .readEntity(Bank.class);
    }

    public static Bank.Branch createAndGetBranch(JerseyTest test, String bankCode, String bic) {
        return test.target("banks/" + bankCode + "/branches")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(singletonMap("bic", bic)))
                .readEntity(Bank.Branch.class);
    }

    public static BankAccount createAndGetBankAccount(JerseyTest test, String name, String bankCode, String bic, Long initialBalance) {
        return test.target("banks/" + bankCode + "/accounts")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                .post(Entity.json(new HashMap<String, String>() {{
                    put("bic", bic);
                    put("name", name);
                    put("initialBalance", initialBalance.toString());
                }}))
                .readEntity(BankAccount.class);
    }

    public static TxResponse getAccountTransactions(JerseyTest test, String bankCode, Integer accountId) {
        return test.target(String.format(TRANSACTIONS_BASE_URL, bankCode, accountId))
                .request().accept(APPLICATION_JSON)
                .get(TxResponse.class);
    }
}
