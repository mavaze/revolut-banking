package io.github.mavaze.revolut.corebank.handlers;

import io.github.mavaze.revolut.centralbank.broker.Acknowledge;
import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.centralbank.broker.message.MessageHandler;
import io.github.mavaze.revolut.commons.domain.Transaction;
import io.github.mavaze.revolut.commons.domain.TxMessage;
import io.github.mavaze.revolut.commons.utils.NameIdGenerator;
import io.github.mavaze.revolut.corebank.services.TxAccountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;

import static io.github.mavaze.revolut.commons.domain.TransactionStatus.REVERTED;
import static java.lang.Integer.parseInt;

@Slf4j
@RequiredArgsConstructor
public class DirectAcknowledgeHandler implements MessageHandler<TxMessage> {

    private final TxAccountService accountService;

    @Override
    public Optional<Acknowledge> onMessage(@NonNull final Message<TxMessage> message) {

        log.debug("Received {}", message);

        final TxMessage payload = message.getPayload();
        final Transaction transaction = payload.getTransaction();

        if (message instanceof Acknowledge.Ack) {
            final String involvedAccount = transaction.getInvolvedAccount();
            log.info("Updating original transaction of sender {} with reference id from receiver", involvedAccount);
            accountService.findAccountTransactionById(involvedAccount, parseInt(transaction.getReferenceId()))
                    .ifPresent(tx -> tx.setReferenceId(transaction.getTransactionId().toString()));
        }

        if (message instanceof Acknowledge.Nack) {
            final Transaction reverseTx = Transaction.builder()
                    .transactionId(NameIdGenerator.generateId())
                    .referenceId(transaction.getTransactionId().toString())
                    .involvedAccount(transaction.getInvolvedAccount())
                    .description(transaction.getDescription())
                    .amount(transaction.getAmount())
                    .transactionDate(new Date())
                    .status(REVERTED)
                    .build();
            accountService.addAmountToAccount(message.getPayload().getSender(), reverseTx);
        }

        return Optional.empty();
    }

    @Override
    public void onAcknowledge(Acknowledge<TxMessage> acknowledge) {
        log.warn("OOPS!!! As long as onMessage() returns empty, this line should never be printed.");
    }
}
