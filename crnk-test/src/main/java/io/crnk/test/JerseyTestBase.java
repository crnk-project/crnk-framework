package io.crnk.test;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.ServerSocket;

public class JerseyTestBase extends JerseyTest {

	@BeforeClass
	public static void selectPort() {
		try {
			ServerSocket s = new ServerSocket(0);
			int port = s.getLocalPort();
			s.close();
			System.setProperty("jersey.config.test.container.port", Integer.toString(port));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
