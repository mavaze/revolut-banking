package io.github.mavaze.revolut.centralbank;

import io.github.mavaze.revolut.centralbank.broker.MessageBroker;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class CentralBankFeature implements Feature {

    @Override
    public boolean configure(final FeatureContext context) {
        context.register(CentralResource.class);
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                // Components
                bindAsContract(MessageBroker.class).in(Singleton.class);
                bindAsContract(CentralQueueTemplate.class).in(Singleton.class);
            }
        });
        return true;
    }
}
