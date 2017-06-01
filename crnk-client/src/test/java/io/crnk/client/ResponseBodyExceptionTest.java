package io.crnk.client;

import org.junit.Assert;
import org.junit.Test;

public class ResponseBodyExceptionTest {

	@Test
	public void test() {
		ResponseBodyException exception = new ResponseBodyException("test");
		Assert.assertEquals("test", exception.getMessage());
	}
}
