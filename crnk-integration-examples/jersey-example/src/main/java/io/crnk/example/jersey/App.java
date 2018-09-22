package io.crnk.example.jersey;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public class App {

	private static final URI BASE_URI = URI.create(JerseyApplication.APPLICATION_URL);

	public static void main(String[] args) throws IOException, InterruptedException {
		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, new JerseyApplication());
		server.start();

		Thread.sleep(50);
		System.out.println("\n\nopen http://localhost:8080 in your browser\n\n");
	}
}
