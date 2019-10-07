package io.github.mavaze.revolut.corebank;

import io.github.mavaze.revolut.centralbank.CentralBankFeature;
import io.github.mavaze.revolut.commons.contract.TransferRequest;
import io.github.mavaze.revolut.commons.contract.TxResponse;
import io.github.mavaze.revolut.commons.domain.BankAccount;
import io.github.mavaze.revolut.commons.domain.Transaction;
import io.github.mavaze.revolut.commons.domain.TransactionStatus;
import io.github.mavaze.revolut.commons.utils.NameIdGenerator;
import io.github.mavaze.revolut.commons.utils.TestUtils;
import io.github.mavaze.revolut.corebank.exceptions.ErrorMessage;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.NavigableSet;

import static io.github.mavaze.revolut.commons.utils.TestUtils.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static org.junit.Assert.assertEquals;

public class TransactionIntegrationTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig()
                .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "WARNING")
                .register(CentralBankFeature.class)
                .register(CoreBankingFeature.class);
    }

    @Test
    public void doIntraBankTransfer_Success() {

        // given
        final String bankCode1 = "GB93", bic1 = "JDJ333XXX";

        createAndGetBank(this, bankCode1, "Revolut UK");
        createAndGetBranch(this, bankCode1, bic1);

        final BankAccount sender = createAndGetBankAccount(this, "Sam", bankCode1, bic1,1000L);
        final BankAccount receiver = createAndGetBankAccount(this, "Paul", bankCode1, bic1, 200L);

        final TransferRequest.TransferRequestBuilder transaction = TransferRequest.builder()
                .description("intra bank transfer")
                .sender(sender.getIban())
                .amount(200L);

        // when
        final Response response = target(String.format(TRANSACTIONS_BASE_URL, bankCode1, sender.getAccountId()))
                .request(APPLICATION_JSON).accept(APPLICATION_JSON)
                .post(Entity.json(transaction.receiver(receiver.getIban()).build()));

        final TxResponse txResponseSender = TestUtils.getAccountTransactions(this, bankCode1, sender.getAccountId());
        final TxResponse txResponseReceiver = TestUtils.getAccountTransactions(this, bankCode1, receiver.getAccountId());

        assertEquals(800d, txResponseSender.getBalance(), 0.0);
        assertEquals(2, txResponseSender.getTransactions().size());
        assertEquals(TransactionStatus.DEBITED, txResponseSender.getTransactions().pollFirst().getStatus());

        assertEquals(400d, txResponseReceiver.getBalance(), 0.0);
        assertEquals(2, txResponseReceiver.getTransactions().size());
        assertEquals(TransactionStatus.CREDITED, txResponseReceiver.getTransactions().pollFirst().getStatus());

        // then
        assertEquals(ACCEPTED.getStatusCode(), response.getStatus());
    }

    @Test
    public void doIntraBankTransferBothSenderReceiverSame_Failure() {
        // given
        final String bankCode1 = "GB99", bic1 = "JDJ333XXX";

        createAndGetBank(this, bankCode1, "ICICI UK");
        createAndGetBranch(this, bankCode1, bic1);

        final BankAccount sender = createAndGetBankAccount(this, "Sam", bankCode1, bic1,1000L);

        final TransferRequest.TransferRequestBuilder transaction = TransferRequest.builder()
                .description("intra bank transfer")
                .sender(sender.getIban())
                .amount(200L);

        // when
        final Response response = target(String.format(TRANSACTIONS_BASE_URL, bankCode1, sender.getAccountId()))
                .request(APPLICATION_JSON).accept(APPLICATION_JSON)
                .post(Entity.json(transaction.receiver(sender.getIban()).build()));

        // then
        final TxResponse txResponseSender = TestUtils.getAccountTransactions(this, bankCode1, sender.getAccountId());
        assertEquals(1, txResponseSender.getTransactions().size());

        assertEquals(CONFLICT.getStatusCode(), response.getStatus());
        assertEquals("You cannot transfer amount to your own account.", response.readEntity(ErrorMessage.class).getMessage());
    }

    @Test
    public void doIntraBankTransfer_Failure() {

        // given
        final String bankCode1 = "GB94", bic1 = "JDJ333XXX";
        final String bic2 = "HD72SSXXX";

        createAndGetBank(this, bankCode1, "Monzo UK");
        createAndGetBranch(this, bankCode1, bic1);

        String ibanWithoutAccount = NameIdGenerator.iban(bankCode1, bic2);

        final BankAccount sender = createAndGetBankAccount(this, "Sam", bankCode1, bic1,1000L);

        final TransferRequest.TransferRequestBuilder transaction = TransferRequest.builder()
                .description("intra bank transfer")
                .sender(sender.getIban())
                .amount(200L);

        // when
        final Response response = target(String.format(TRANSACTIONS_BASE_URL, bankCode1, sender.getAccountId()))
                .request(APPLICATION_JSON).accept(APPLICATION_JSON)
                .post(Entity.json(transaction.receiver(ibanWithoutAccount).build()));

        final TxResponse txResponseSender = TestUtils.getAccountTransactions(this, bankCode1, sender.getAccountId());
        NavigableSet<Transaction> transactions = txResponseSender.getTransactions();
        assertEquals(3, transactions.size());
        assertEquals(TransactionStatus.REVERTED, transactions.pollFirst().getStatus());
        assertEquals(TransactionStatus.DEBITED, transactions.pollFirst().getStatus());
        assertEquals(1000d, txResponseSender.getBalance(), 0.0);

        // then
        assertEquals(ACCEPTED.getStatusCode(), response.getStatus());
    }

    @Test
    public void doInterBankTransfer_Success() {
        final String bankCode1 = "GB95", bic1 = "JDJ333XXX";
        final String bankCode2 = "DE25", bic2 = "HD72SSXXX";

        createAndGetBank(this, bankCode1, "Credit Suisse UK");
        createAndGetBranch(this, bankCode1, bic1);

        createAndGetBank(this, bankCode2, "N26 Germany");
        createAndGetBranch(this, bankCode2, bic2);

        final BankAccount sender = createAndGetBankAccount(this, "Sam", bankCode1, bic1,1000L);
        final BankAccount receiver = createAndGetBankAccount(this, "John", bankCode2, bic2, 500L);

        final TransferRequest.TransferRequestBuilder transaction = TransferRequest.builder()
                .description("inter bank transfer")
                .sender(sender.getIban())
                .amount(200L);

        // when
        final Response response = target(String.format(TRANSACTIONS_BASE_URL, bankCode1, sender.getAccountId()))
                .request(APPLICATION_JSON).accept(APPLICATION_JSON)
                .post(Entity.json(transaction.receiver(receiver.getIban()).build()));

        // then
        final TxResponse txResponseSender = TestUtils.getAccountTransactions(this, bankCode1, sender.getAccountId());
        assertEquals(800d, txResponseSender.getBalance(), 0.0);
        assertEquals(2, txResponseSender.getTransactions().size());
        assertEquals(TransactionStatus.DEBITED, txResponseSender.getTransactions().pollFirst().getStatus());

        final TxResponse txResponseReceiver = TestUtils.getAccountTransactions(this, bankCode2, receiver.getAccountId());
        assertEquals(700d, txResponseReceiver.getBalance(), 0.0);
        assertEquals(2, txResponseReceiver.getTransactions().size());
        assertEquals(TransactionStatus.CREDITED, txResponseReceiver.getTransactions().pollFirst().getStatus());

        assertEquals(ACCEPTED.getStatusCode(), response.getStatus());
    }

    @Test
    public void doInterBankTransfer_Failure() {
        final String bankCode1 = "GB96", bic1 = "JDJ333XXX";
        final String bankCode2 = "DE26", bic2 = "HD72SSXXX";

        createAndGetBank(this, bankCode1, "HSBC UK");
        createAndGetBranch(this, bankCode1, bic1);

        String ibanWithoutBank = NameIdGenerator.iban(bankCode2, bic2);

        final BankAccount sender = createAndGetBankAccount(this, "Sam", bankCode1, bic1,1000L);

        final TransferRequest.TransferRequestBuilder transaction = TransferRequest.builder()
                .description("inter bank transfer")
                .sender(sender.getIban())
                .amount(200L);

        final Response response = target(String.format(TRANSACTIONS_BASE_URL, bankCode1, sender.getAccountId()))
                .request(APPLICATION_JSON).accept(APPLICATION_JSON)
                .post(Entity.json(transaction.receiver(ibanWithoutBank).build()));

        final TxResponse txResponseSender = TestUtils.getAccountTransactions(this, bankCode1, sender.getAccountId());
        NavigableSet<Transaction> transactions = txResponseSender.getTransactions();
        assertEquals(3, transactions.size());
        assertEquals(TransactionStatus.REVERTED, transactions.pollFirst().getStatus());
        assertEquals(TransactionStatus.DEBITED, transactions.pollFirst().getStatus());
        assertEquals(1000d, txResponseSender.getBalance(), 0.0);
        assertEquals(ACCEPTED.getStatusCode(), response.getStatus());
    }
}
