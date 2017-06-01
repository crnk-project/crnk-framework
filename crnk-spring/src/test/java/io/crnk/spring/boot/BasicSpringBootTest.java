package io.crnk.spring.boot;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.junit.Assert.assertEquals;

import javax.security.auth.message.config.AuthConfigFactory;

import org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class BasicSpringBootTest {

	@Value("${local.server.port}")
	private int port;

	@Before
	public void setup() {
		// NPE fix
		if (AuthConfigFactory.getFactory() == null) {
			AuthConfigFactory.setFactory(new AuthConfigFactoryImpl());
		}
	}

	@Test
	public void testTestEndpointWithQueryParams() throws Exception {
		TestRestTemplate testRestTemplate = new TestRestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/tasks?filter[tasks][name]=John", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertThatJson(response.getBody()).node("data").isPresent();
	}


	@Test
	public void testJpa() throws Exception {
		TestRestTemplate testRestTemplate = new TestRestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/schedule", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertThatJson(response.getBody()).node("data").isPresent();
	}

	@Test
	public void testTestCustomEndpoint() throws Exception {
		TestRestTemplate testRestTemplate = new TestRestTemplate();
		ResponseEntity<String> response = testRestTemplate
				.getForEntity("http://localhost:" + this.port + "/api/custom", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(response.getBody(), "hello");
	}
}