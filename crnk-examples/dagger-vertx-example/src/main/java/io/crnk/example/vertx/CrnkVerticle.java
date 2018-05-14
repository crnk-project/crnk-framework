package io.crnk.example.vertx;

import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.home.HomeModule;
import io.crnk.setup.vertx.CrnkVertxHandler;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;

public class CrnkVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkVerticle.class);

	private int port;

	@Inject
	protected Set<ResourceRepositoryV2> repositories;

	public CrnkVerticle(int port) {
		this.port = port;
	}

	@Override
	public void start() {
		LOGGER.debug("starting");
		HttpServer server = vertx.createHttpServer();

		CrnkVertxHandler handler = new CrnkVertxHandler((boot) -> {


			boot.addModule(HomeModule.create());
		});

		server.requestStream().toFlowable()
				.flatMap(request -> handler.process(request))
				.subscribe((response) -> LOGGER.debug("delivered response {}", response), error -> LOGGER.debug("error occured", error));
		LOGGER.debug("listen on port={}", port);
		server.listen(port);
		LOGGER.debug("started");
	}
}