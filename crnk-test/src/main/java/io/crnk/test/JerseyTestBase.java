package io.crnk.test;

import java.util.Random;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;

public class JerseyTestBase extends JerseyTest {

	@BeforeClass
	public static void selectPort() {
		Random random = new Random();
		random.setSeed(System.nanoTime());
		int port = 40000 + random.nextInt(10000);
		System.setProperty("jersey.config.test.container.port", Integer.toString(port));
	}
}
