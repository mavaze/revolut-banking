package io.github.mavaze.revolut.corebank.services;

import io.github.mavaze.revolut.commons.contract.TransferRequest;
import io.github.mavaze.revolut.commons.contract.TxResponse;
import io.github.mavaze.revolut.commons.domain.BankAccount;
import io.github.mavaze.revolut.commons.domain.Transaction;
import io.github.mavaze.revolut.commons.domain.TxMessage;
import io.github.mavaze.revolut.commons.utils.NameIdGenerator;
import io.github.mavaze.revolut.corebank.exceptions.BusinessException;
import io.github.mavaze.revolut.corebank.routing.Router;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

import static io.github.mavaze.revolut.commons.domain.TransactionStatus.DEBITED;
import static io.github.mavaze.revolut.commons.domain.TransactionStatus.REVERTED;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor=@__({@Inject}))
public class TransactionService {

    private final Router<String> router;

    private final TxAccountService accountService;

    /**
     * Amount transfer request from one account to another.
     * 1. Both sender and receiver accounts belong to same bank. (intra-bank transfer)
     * 2. Sender and receiver belong to 2 different banks. (inter-bank transfer)
     *    - validate sender iban belonging to sender bank
     *    - deduct the amount from sender account if he has sufficient balance
     *    - Enqueue the request to appropriate queue for asynchronous processing
     *       - internal bank queue (direct) for intra bank transfer
     *       - global central queue (topic) for inter bank transfer
     *           - all internal bank queues subscribe to global queue with their respective bankcode as routing key
     *    - If unable to enqueue the request, revert the transaction
     * @param bankCode
     * @param request
     * @return remaining balance post amount transfer
     */
    public Long doTransaction(@NonNull final String bankCode, @NonNull final TransferRequest request) {
        validateRequest(bankCode, request);
        final Transaction debitTx = Transaction.builder()
                .transactionId(NameIdGenerator.generateId())
                .involvedAccount(request.getReceiver())
                .description(request.getDescription())
                .transactionDate(new Date())
                .amount(request.getAmount())
                .status(DEBITED)
                .build();
        final Long balance = accountService.deductAmountFromAccount(request.getSender(), debitTx);
        log.debug("Account {} debited with {} amount. Remaining Balance: {}", request.getSender(), request.getAmount(), balance);
        try {
            router.route(new TxMessage(request.getSender(), debitTx));
        } catch(Exception ex){
            log.error("Failed to route the transaction request. Reverting the transaction.");
            final Transaction reverseTx = Transaction.builder()
                    .transactionId(NameIdGenerator.generateId())
                    .referenceId(debitTx.getTransactionId().toString())
                    .involvedAccount(request.getReceiver())
                    .amount(request.getAmount())
                    .description(ex.getMessage())
                    .transactionDate(new Date())
                    .status(REVERTED)
                    .build();
            accountService.addAmountToAccount(request.getSender(), reverseTx);
            throw new BusinessException("Failed to route message", ex);
        }
        return balance;
    }

    public TxResponse getTransactions(String bankCode, Integer accountId) {
        BankAccount bankAccount = accountService.findByBankCodeAndAccountId(bankCode, accountId);
        return new TxResponse(bankAccount.getBalance(), bankAccount.getTransactions());
    }

    /**
     * Preliminary validation to validate the sender belongs to same bank the request is made against.
     * @param bankCode
     * @param request
     * @throws BusinessException
     */
    private void validateRequest(@NonNull final String bankCode, @NonNull final TransferRequest request) throws BusinessException {
        final String sender = request.getSender();
        final String receiver = request.getReceiver();
        if(sender.equals(receiver)) {
            throw new BusinessException("You cannot transfer amount to your own account.");
        }
        if(NameIdGenerator.extractBankCode(sender).equals(bankCode)) {
            accountService.findByIban(sender);
            return;
        }
        throw new BusinessException("Sender doesn't belong to the bank the request is made against.");
    }
}
