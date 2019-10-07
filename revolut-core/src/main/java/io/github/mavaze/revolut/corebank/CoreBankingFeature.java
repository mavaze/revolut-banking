package io.github.mavaze.revolut.corebank;

import io.github.mavaze.revolut.centralbank.broker.MessageBroker;
import io.github.mavaze.revolut.corebank.eventing.EventPublisher;
import io.github.mavaze.revolut.corebank.exceptions.BusinessException;
import io.github.mavaze.revolut.corebank.exceptions.UncaughtException;
import io.github.mavaze.revolut.corebank.resources.BankResource;
import io.github.mavaze.revolut.corebank.routing.BankNameRouter;
import io.github.mavaze.revolut.corebank.routing.Router;
import io.github.mavaze.revolut.corebank.services.*;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class CoreBankingFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(BankResource.class);
        context.register(UncaughtException.class);
        context.register(BusinessException.class);
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                // Components
                bindAsContract(MessageBroker.class).in(Singleton.class);
                bindAsContract(EventPublisher.class).in(Singleton.class);
                bindAsContract(BankOpsTemplate.class).in(Singleton.class);
                bind(BankNameRouter.class).to(new TypeLiteral<Router<String>>() {}).in(Singleton.class);

                // Services
                bindAsContract(BankService.class).in(Singleton.class);
                bindAsContract(TxAccountService.class).in(Singleton.class);
                bind(TxAccountService.class).to(AccountService.class).in(Singleton.class);
                bindAsContract(TransactionService.class).in(Singleton.class);
            }
        });
        return true;
    }
}
