package io.crnk.example.jersey;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public class App {

	private static final URI BASE_URI = URI.create(JerseyApplication.APPLICATION_URL);

	public static void main(String[] args) throws IOException {
		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, new JerseyApplication());

		System.out.println("Press any key to close");
		System.in.read();
		server.shutdownNow();
	}
}
