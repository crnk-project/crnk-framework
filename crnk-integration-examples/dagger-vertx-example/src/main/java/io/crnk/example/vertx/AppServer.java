package io.crnk.example.vertx;

import io.reactivex.subjects.SingleSubject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import javax.inject.Inject;

public class AppServer {

	private CrnkVerticle vehicle;

	private Vertx vertx;

	@Inject
	public AppServer(CrnkVerticle verticle) {
		this.vehicle = verticle;
	}

	public void start() {
		VertxOptions options = new VertxOptions();
		options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);

		SingleSubject waitSubject = SingleSubject.create();
		Handler<AsyncResult<String>> completionHandler = event -> {
			if (event.succeeded()) {
				waitSubject.onSuccess(event.result());
			} else {
				event.cause().printStackTrace();
				System.exit(0);
			}
		};


		vertx = Vertx.vertx(options);
		vertx.deployVerticle(vehicle, completionHandler);
		waitSubject.blockingGet();
	}

	public void stop() {
		SingleSubject waitSubject = SingleSubject.create();
		Handler<AsyncResult<Void>> completionHandler = event -> waitSubject.onSuccess("test");
		vertx.close(completionHandler);
		waitSubject.blockingGet();
	}

	public String getBaseUrl() {
		return "http://127.0.0.1:" + vehicle.getPort();
	}
}
