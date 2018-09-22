package io.crnk.core.engine.http;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseTest {

	private HttpResponse response;

	@Before
	public void setup() {
		response = new HttpResponse();
	}

	@Test
	public void checkHeaderAccess() {
		Assert.assertNotNull(response.getHeaders());

		Map<String, String> headers = new HashMap<>();
		response.setHeaders(headers);
		Assert.assertSame(headers, response.getHeaders());

		response.setContentType("test");
		Assert.assertEquals("test", headers.get(HttpHeaders.HTTP_CONTENT_TYPE));
	}

	@Test
	public void checkBodyUtf8Encoding() throws UnsupportedEncodingException {
		response.setBody("aäöü");
		Assert.assertNotEquals(4, response.getBody().length);
		Assert.assertEquals("aäöü", new String(response.getBody(), StandardCharsets.UTF_8));
	}
}
