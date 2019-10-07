package io.github.mavaze.revolut.corebank;

import com.sun.net.httpserver.HttpServer;
import io.github.mavaze.revolut.corebank.config.ServerConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.ExecutorService;

@Slf4j
public class Server {

    private static HttpServer httpServer;

    private static ExecutorService executor;

    public static void start(@NonNull final ServerConfig config) {

        if(httpServer == null) {
            synchronized (Server.class) {
                if (httpServer == null) {
                    final URI baseUri = UriBuilder.fromUri(config.getHostname())
                            .port(config.getPort())
                            .build();
                    httpServer = JdkHttpServerFactory.createHttpServer(baseUri, config.getResourceConfig(), true);
                    executor = ((ExecutorService) httpServer.getExecutor());
                    handleGracefulTermination(config);
                    return;
                }
            }
        }
        log.warn("server already started");
    }

    private static void handleGracefulTermination(@NonNull ServerConfig config) {
        if(config.getShutdownPath() != null) {
            registerShutdownHandler(config.getShutdownPath());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final boolean terminated = executor.isTerminated();
            if(!terminated) {
                log.info("Exiting server gracefully.");
                httpServer.stop(0);
            }
        }));
    }

    private static void registerShutdownHandler(@NonNull final String path) {
        httpServer.createContext(path, httpExchange -> {
            log.info("Endpoint " + path + " invoked. Stopping server.");
            stop();
        });
    }

    private static synchronized void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            executor.shutdown();
            httpServer = null;
        }
    }
}
