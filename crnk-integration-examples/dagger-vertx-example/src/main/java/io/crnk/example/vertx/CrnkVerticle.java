package io.crnk.example.vertx;

import io.crnk.core.module.Module;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.setup.vertx.CrnkVertxHandler;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.Set;

public class CrnkVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrnkVerticle.class);

    private final Set<ResourceRepository> repositories;

    private final Set<Module> modules;

    private final VertxSecurityProvider securityProvider;

    private int port = 8080;

    @Inject
    public CrnkVerticle(Set<ResourceRepository> repositories, Set<Module> modules, VertxSecurityProvider securityProvider) {
        this.repositories = repositories;
        this.modules = modules;
        this.securityProvider = securityProvider;
    }

    @Override
    public void start() {
        LOGGER.debug("starting");
        HttpServer server = vertx.createHttpServer();

        CrnkVertxHandler handler = new CrnkVertxHandler((boot) -> {
            SimpleModule daggerModule = new SimpleModule("dagger");
            daggerModule.addSecurityProvider(securityProvider);
            modules.forEach(it -> boot.addModule(it));
            repositories.forEach(it -> daggerModule.addRepository(it));
            boot.addModule(daggerModule);
        });
        handler.addInterceptor(securityProvider);

        server.requestStream().toFlowable()
                .flatMap(request -> handler.process(request))
                .subscribe((response) -> LOGGER.debug("delivered response {}", response), error -> LOGGER.debug("error occured", error));
        LOGGER.debug("listen on port={}", port);
        server.listen(port);
        LOGGER.debug("started");
    }

    public int getPort() {
        return port;
    }
}