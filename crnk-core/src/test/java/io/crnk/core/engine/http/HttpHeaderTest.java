package io.crnk.core.engine.http;

import io.crnk.core.utils.CoreClassTestUtils;
import org.junit.Test;

public class HttpHeaderTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(HttpHeaders.class);
	}
}
