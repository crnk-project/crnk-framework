package io.crnk.example.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertxApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(VertxApplication.class);

	public static void main(String[] args) {
		AppComponent appComponent = DaggerAppComponent.builder().build();
		AppServer server = appComponent.server();

		server.start();
		LOGGER.warn("server started, visit " + server.getBaseUrl());
	}
}
