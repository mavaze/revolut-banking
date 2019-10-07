package io.github.mavaze.revolut.corebank.routing;

import io.github.mavaze.revolut.centralbank.broker.MessageBroker;
import io.github.mavaze.revolut.centralbank.broker.exchange.Exchange;
import io.github.mavaze.revolut.centralbank.broker.exchange.TopicExchange;
import io.github.mavaze.revolut.centralbank.broker.message.Message;
import io.github.mavaze.revolut.centralbank.broker.message.MessageUtils;
import io.github.mavaze.revolut.commons.domain.Bank;
import io.github.mavaze.revolut.commons.domain.Routable;
import io.github.mavaze.revolut.commons.utils.NameIdGenerator;
import io.github.mavaze.revolut.corebank.services.BankService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.github.mavaze.revolut.centralbank.broker.exchange.Topology.DIRECT;
import static io.github.mavaze.revolut.centralbank.broker.exchange.Topology.TOPIC;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor=@__({@Inject}))
public class BankNameRouter implements Router<String> {

    private final BankService bankService;

    private final MessageBroker messageBroker;

    @Override
    public void route(Message<? extends Routable<String>> message) throws Exception {
        String routingKey = MessageUtils.getRoutingKey(message);
        if(routingKey == null) {
            routingKey = NameIdGenerator.extractBankCode(message.getPayload().getReceiver());
        }
        final Exchange exchange = findExchange(message.getPayload(), routingKey);
        exchange.send(message);
    }

    @Override
    public void route(@NonNull final Routable<String> request) throws Exception {
        final String replyKey = NameIdGenerator.extractBankCode(request.getSender());
        final String routingKey = NameIdGenerator.extractBankCode(request.getReceiver());
        final Exchange exchange = findExchange(request, routingKey);

        final Message<Routable<String>> message = MessageUtils.build(request, routingKey, replyKey);
        exchange.send(message);
    }

    private Exchange findExchange(@NonNull final Routable<String> request, String receiverBank) {
        final String senderBank = NameIdGenerator.extractBankCode(request.getSender());

        final Exchange exchange;

        if (senderBank.equals(receiverBank)) {
            final Bank bank = bankService.findByCode(receiverBank);
            String queueName = bank.getBankOperations().getQueueName();
            exchange = messageBroker.exchange(queueName, DIRECT);
            log.info("Intra bank transfer. Sending {} to '{}' queue", request.getClass().getSimpleName(), queueName);
        } else {
            exchange = messageBroker.exchange(TopicExchange.QUEUE_NAME, TOPIC);
            log.info("Inter bank transfer. Sending {} to central queue", request.getClass().getSimpleName());
        }
        return exchange;
    }
}
