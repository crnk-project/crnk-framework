package io.crnk.client.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import io.crnk.client.ClientException;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientStubBaseTest {


	private CrnkClient client;

	private JsonApiUrlBuilder urlBuilder;

	private ClientStubBase stub;

	@Before
	public void setup() {
		client = new CrnkClient("http://x");

		SimpleModule module = new SimpleModule("test");
		module.addExceptionMapper(new CheckedExceptionMapper());
		client.addModule(module);

		urlBuilder = Mockito.mock(JsonApiUrlBuilder.class);

		stub = new ClientStubBase(client, urlBuilder, Task.class);
	}

	@Test
	public void check404AndNoBodyGivesNotFoundException() throws IOException {

		HttpAdapterResponse response = Mockito.mock(HttpAdapterResponse.class);
		Mockito.when(response.body()).thenReturn("");
		Mockito.when(response.code()).thenReturn(404);

		RuntimeException exception = stub.handleError(null, response);
		Assert.assertTrue(exception instanceof ResourceNotFoundException);
	}

	@Test
	public void check500AndNoBodyGivesInternalServerErrorException() throws IOException {
		HttpAdapterResponse response = Mockito.mock(HttpAdapterResponse.class);
		Mockito.when(response.body()).thenReturn("");
		Mockito.when(response.code()).thenReturn(500);

		RuntimeException exception = stub.handleError(null, response);
		Assert.assertTrue(exception instanceof InternalServerErrorException);
	}

	@Test
	public void checkCheckedException() throws IOException {
		HttpAdapterResponse response = Mockito.mock(HttpAdapterResponse.class);
		Mockito.when(response.body()).thenReturn("");
		Mockito.when(response.code()).thenReturn(599);

		RuntimeException exception = stub.handleError(null, response);
		Assert.assertTrue(exception instanceof ClientException);
		Assert.assertTrue(exception.getCause() instanceof IOException);
	}

	@Test
	public void checkBodyWithNoErrorsAnd500Status() throws IOException {
		Document document = new Document();
		document.setErrors(new ArrayList<ErrorData>());
		String body = client.getObjectMapper().writeValueAsString(document);


		HttpAdapterResponse response = Mockito.mock(HttpAdapterResponse.class);
		Mockito.when(response.body()).thenReturn(body);
		Mockito.when(response.code()).thenReturn(500);

		RuntimeException exception = stub.handleError(null, response);
		Assert.assertTrue(exception instanceof InternalServerErrorException);
	}

	@Test
	public void checkBodyWithErrors() throws IOException {
		Document document = new Document();
		ErrorData errorData = new ErrorDataBuilder().setCode("404").setDetail("detail").build();
		document.setErrors(Arrays.asList(errorData));
		String body = client.getObjectMapper().writeValueAsString(document);

		HttpAdapterResponse response = Mockito.mock(HttpAdapterResponse.class);
		Mockito.when(response.body()).thenReturn(body);
		Mockito.when(response.getResponseHeader(HttpHeaders.HTTP_CONTENT_TYPE))
				.thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);
		Mockito.when(response.code()).thenReturn(404);

		RuntimeException exception = stub.handleError(null, response);
		Assert.assertTrue(exception instanceof ResourceNotFoundException);
		Assert.assertEquals("detail", exception.getMessage());
	}

	@Test
	public void checkBodyWithErrorsButInvalidContentType() throws IOException {
		Document document = new Document();
		ErrorData errorData = new ErrorDataBuilder().setCode("404").setDetail("detail").build();
		document.setErrors(Arrays.asList(errorData));
		String body = client.getObjectMapper().writeValueAsString(document);

		HttpAdapterResponse response = Mockito.mock(HttpAdapterResponse.class);
		Mockito.when(response.body()).thenReturn(body);
		Mockito.when(response.getResponseHeader(HttpHeaders.HTTP_CONTENT_TYPE)).thenReturn("not json api");
		Mockito.when(response.code()).thenReturn(404);

		RuntimeException exception = stub.handleError(null, response);
		Assert.assertTrue(exception instanceof ResourceNotFoundException);
		Assert.assertNull(exception.getMessage());
	}


	class CheckedExceptionMapper implements ExceptionMapper<IOException> {

		@Override
		public ErrorResponse toErrorResponse(IOException exception) {
			return null;
		}

		@Override
		public IOException fromErrorResponse(ErrorResponse errorResponse) {
			return new IOException();
		}

		@Override
		public boolean accepts(ErrorResponse errorResponse) {
			return errorResponse.getHttpStatus() == 599;
		}
	}
}
