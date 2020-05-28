package io.crnk.example.vertx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppMain {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppMain.class);

	public static void main(String[] args) {
		long s = System.currentTimeMillis();
		AppComponent appComponent = DaggerAppComponent.builder().build();
		AppServer server = appComponent.server();

		server.start();
		LOGGER.warn("server started in " + (System.currentTimeMillis() - s) + ", visit " + server.getBaseUrl());
	}
}
