package io.crnk.setup.vertx;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.home.HomeModule;
import io.crnk.test.mock.reactive.ReactiveTestModule;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// tag::docs[]
public class CrnkVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrnkVerticle.class);

    public ReactiveTestModule testModule = new ReactiveTestModule();

    private int port;

    private CrnkVertxHandler handler;

    public CrnkVerticle(int port) {
        this.port = port;

        handler = new CrnkVertxHandler((boot) -> {
            boot.addModule(HomeModule.create());
            boot.addModule(testModule);
        });
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();

        server.requestStream().toFlowable()
                .flatMap(request -> handler.process(request))
                .subscribe((response) -> LOGGER.debug("delivered response {}", response), error -> LOGGER.debug("error occured", error));
        server.listen(port);
    }

    public CrnkBoot getBoot() {
        return handler.getBoot();
    }
}
// end::docs[]