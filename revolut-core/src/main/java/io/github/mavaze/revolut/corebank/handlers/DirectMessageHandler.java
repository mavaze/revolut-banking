package io.github.mavaze.revolut.corebank.handlers;

import io.github.mavaze.revolut.centralbank.broker.Acknowledge;
import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.centralbank.broker.message.MessageHandler;
import io.github.mavaze.revolut.centralbank.broker.message.MessageUtils;
import io.github.mavaze.revolut.commons.domain.Transaction;
import io.github.mavaze.revolut.commons.domain.TxMessage;
import io.github.mavaze.revolut.commons.utils.NameIdGenerator;
import io.github.mavaze.revolut.corebank.routing.Router;
import io.github.mavaze.revolut.corebank.services.TxAccountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;

import static io.github.mavaze.revolut.centralbank.broker.message.MessageUtils.nack;
import static io.github.mavaze.revolut.commons.domain.TransactionStatus.CREDITED;
import static io.github.mavaze.revolut.commons.domain.TransactionStatus.FAILED;

@Slf4j
@RequiredArgsConstructor
public class DirectMessageHandler implements MessageHandler<TxMessage> {

    private final Router<String> router;
    private final TxAccountService accountService;

    @Override
    public Optional<Acknowledge> onMessage(@NonNull final Message<TxMessage> message) {
        log.debug("Received {}", message);
        try {
            return handleMessageGracefully(message);
        } catch (Exception ex) {
            log.error("Transfer failed due to {}", ex.getMessage());
            final Transaction tx = (Transaction) message.getPayload().getTransaction().clone();
            tx.setStatus(FAILED);
            tx.setDescription(ex.getMessage());
            return Optional.of(nack(message, new TxMessage(MessageUtils.getSender(message), tx)));
        }
    }

    public Optional<Acknowledge> handleMessageGracefully(@NonNull final Message<TxMessage> message) throws Exception {
        final TxMessage request = message.getPayload();
        final Transaction transaction = request.getTransaction();

        final Transaction creditTx = Transaction.builder()
                .transactionId(NameIdGenerator.generateId())
                .referenceId(MessageUtils.getGuid(message))
                .involvedAccount(request.getSender())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(new Date())
                .status(CREDITED)
                .build();

        final Long balance = accountService.addAmountToAccount(request.getReceiver(), creditTx);
        log.debug("Account {} credited with {} amount. New Balance: {}", request.getReceiver(), transaction.getAmount(), balance);
        return Optional.of(MessageUtils.ack(message, new TxMessage(request.getReceiver(), creditTx)));
    }

    @Override
    public void onAcknowledge(Acknowledge<TxMessage> acknowledge) throws Exception {
        log.info("Routing acknowledgement to recipient bank exchange.");
        router.route(acknowledge);
    }
}
