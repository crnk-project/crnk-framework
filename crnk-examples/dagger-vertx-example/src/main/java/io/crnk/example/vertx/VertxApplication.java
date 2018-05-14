package io.crnk.example.vertx;

import io.reactivex.subjects.SingleSubject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(VertxApplication.class);

	private CrnkVerticle vehicle;

	private Vertx vertx;

	private int port = 8080;

	public static void main(String[] args) {
		VertxApplication app = new VertxApplication();
		app.start();
		LOGGER.warn("server started, visit " + app.getBaseUrl());
		System.out.println("server started, visit " + app.getBaseUrl());
	}

	public void start() {


		/*
		mNetComponent = DaggerNetComponent.builder()
				// list of modules that are part of this component need to be created here too
				.appModule(new AppModule(this)) // This also corresponds to the name of your module: %component_name%Module
				.netModule(new NetModule("https://api.github.com"))
				.build();
				*/

		VertxOptions options = new VertxOptions();
		options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);

		SingleSubject waitSubject = SingleSubject.create();
		Handler<AsyncResult<String>> completionHandler = event -> {

			System.out.println(event);
			if(event.succeeded()) {
				waitSubject.onSuccess(event.result());
			}else{
				event.cause().printStackTrace();
				System.exit(0);
			}
		};

		AppComponent appComponent = DaggerAppComponent.builder().build();

		vehicle = new CrnkVerticle(port);
		appComponent.inject(vehicle);

		vertx = Vertx.vertx(options);
		vertx.deployVerticle(vehicle, completionHandler);
		System.out.println("deploying...");
		waitSubject.blockingGet();
	}

	public void stop() {
		SingleSubject waitSubject = SingleSubject.create();
		Handler<AsyncResult<Void>> completionHandler = event -> waitSubject.onSuccess("test");
		vertx.close(completionHandler);
		waitSubject.blockingGet();
	}

	public String getBaseUrl() {
		return "http://127.0.0.1:" + port;
	}
}
