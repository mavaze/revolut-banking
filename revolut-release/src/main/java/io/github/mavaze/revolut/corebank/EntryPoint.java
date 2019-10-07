package io.github.mavaze.revolut.corebank;

import io.github.mavaze.revolut.centralbank.CentralBankFeature;
import io.github.mavaze.revolut.corebank.config.ServerConfig;
import org.glassfish.jersey.server.ResourceConfig;

public class EntryPoint {

    public static void main(String[] args) {
        final ResourceConfig resourceConfig = new ResourceConfig()
                .register(CentralBankFeature.class)
                .register(CoreBankingFeature.class);

        final ServerConfig config = ServerConfig.builder()
                .resourceConfig(resourceConfig)
                .hostname("http://localhost/")
                .shutdownPath("/exit")
                .port(9998)
                .build();

        Server.start(config);
    }
}
