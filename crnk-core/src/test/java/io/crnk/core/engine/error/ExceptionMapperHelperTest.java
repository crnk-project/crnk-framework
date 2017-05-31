package io.crnk.core.engine.error;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class ExceptionMapperHelperTest {

	@Test
	public void hasPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(ExceptionMapperHelper.class);
	}

	@Test
	public void test() {
		IllegalStateException exception = new IllegalStateException("test");
		ErrorResponse response = ExceptionMapperHelper.toErrorResponse(exception, 499, "illegal");

		Assert.assertEquals(1, response.getErrors().size());
		ErrorData errorData = response.getErrors().iterator().next();

		Assert.assertEquals("test", errorData.getCode());
		Assert.assertEquals("test", errorData.getTitle());
		Assert.assertEquals("499", errorData.getStatus());
		Assert.assertEquals("illegal", errorData.getMeta().get("type"));

		Assert.assertEquals(499, response.getHttpStatus());

		Assert.assertEquals("test", ExceptionMapperHelper.createErrorMessage(response));

		Assert.assertTrue(ExceptionMapperHelper.accepts(response, 499, "illegal"));
		Assert.assertFalse(ExceptionMapperHelper.accepts(response, 1, "illegal"));
		Assert.assertFalse(ExceptionMapperHelper.accepts(response, 499, "test"));
		Assert.assertFalse(ExceptionMapperHelper.accepts(new ErrorResponseBuilder().setStatus(499).build(), 499, "illegal"));
	}
}
