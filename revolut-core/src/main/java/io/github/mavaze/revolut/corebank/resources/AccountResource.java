package io.github.mavaze.revolut.corebank.resources;

import io.github.mavaze.revolut.commons.domain.BankAccount;
import io.github.mavaze.revolut.corebank.services.TransactionService;
import io.github.mavaze.revolut.corebank.services.TxAccountService;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class AccountResource {

    private final TxAccountService accountService;

    private final TransactionService transactionService;

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public BankAccount createAccount(@PathParam("bankCode") String bankCode, Map<String, String> properties) {
        final String name = properties.get("name");
        final String bic = properties.get("bic");
        final Long initialBalance = Long.parseLong(properties.get("initialBalance"));
        return accountService.addBankAccount(bankCode, bic, name, initialBalance);
    }

    @GET
    @Path("/{accountId}")
    @Produces(APPLICATION_JSON)
    public BankAccount getAccount(@PathParam("bankCode") String bankCode,
                                  @PathParam("accountId") Integer accountId) {
        return accountService.findByBankCodeAndAccountId(bankCode, accountId);
    }

    @Path("/{accountId}/transactions")
    public TransactionResource getTransactionResource() {
        return new TransactionResource(transactionService);
    }
}
