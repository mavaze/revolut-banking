package io.github.mavaze.revolut.corebank.resources;

import io.github.mavaze.revolut.commons.contract.TransferRequest;
import io.github.mavaze.revolut.commons.contract.TxResponse;
import io.github.mavaze.revolut.corebank.services.TransactionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class TransactionResource {

    private final TransactionService transactionService;

    @GET
    public Response getTransactions(@PathParam("bankCode") String bankCode,
                                    @PathParam("accountId") Integer accountId) {
        final TxResponse response = transactionService.getTransactions(bankCode, accountId);
        return Response.ok().entity(response).build();
    }

    @POST
    public Response doTransaction(@PathParam("bankCode") String bankCode,
                                  @PathParam("accountId") Integer accountId,
                                  @NonNull final TransferRequest transaction)  {
        final Long balance = transactionService.doTransaction(bankCode, transaction);
        return Response.accepted().entity(singletonMap("balance", balance)).build();
    }
}
