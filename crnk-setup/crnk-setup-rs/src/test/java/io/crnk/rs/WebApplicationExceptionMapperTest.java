package io.crnk.rs;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.rs.internal.WebApplicationExceptionMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.util.Iterator;

public class WebApplicationExceptionMapperTest {

	@Test
	public void test() {
		WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();
		WebApplicationException exception = new WebApplicationException("hi");
		ErrorResponse response = mapper.toErrorResponse(exception);
		Iterable<ErrorData> errors = response.getErrors();
		Iterator<ErrorData> iterator = errors.iterator();
		ErrorData data = iterator.next();
		Assert.assertFalse(iterator.hasNext());
		Assert.assertEquals("500", data.getStatus());
		Assert.assertEquals("hi", data.getCode());
		Assert.assertTrue(mapper.accepts(response));
		WebApplicationException fromErrorResponse = mapper.fromErrorResponse(response);
		Assert.assertEquals("hi", fromErrorResponse.getMessage());
	}
}
