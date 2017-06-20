package io.crnk.core.engine.http;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class JsonApiRequestProcessorTest {


	private JsonApiRequestProcessor processor;

	private HttpRequestContextBase requestContextBase;

	private HttpRequestContextBaseAdapter requestContext;

	private Module.ModuleContext moduleContext;

	private CrnkBoot boot;

	@Before
	public void setup() {
		boot = new CrnkBoot();
		boot.addModule(new Module() {
			@Override
			public String getModuleName() {
				return "test";
			}

			@Override
			public void setupModule(ModuleContext context) {
				moduleContext = context;
			}
		});
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost:8080"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE));
		boot.boot();

		processor = new JsonApiRequestProcessor(moduleContext);


		requestContextBase = Mockito.mock(HttpRequestContextBase.class);
		requestContext = new HttpRequestContextBaseAdapter(requestContextBase);
	}

	@Test
	public void ignoreRequestForContentTypeMismatch() throws IOException {
		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
		Mockito.when(requestContextBase.getRequestHeader("Accept"))
				.thenReturn("*");
		Assert.assertTrue(JsonApiRequestProcessor.isJsonApiRequest(requestContext));
		Mockito.when(requestContextBase.getRequestHeader("Accept"))
				.thenReturn("something");
		Assert.assertFalse(JsonApiRequestProcessor.isJsonApiRequest(requestContext));
	}

	@Test
	public void handleRequestForGetAndWildcardContentType() throws IOException {
		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
		Assert.assertTrue(JsonApiRequestProcessor.isJsonApiRequest(requestContext));
	}

	@Test
	public void ignoreRequestForPatchAndWildcardContentType() throws IOException {
		Mockito.when(requestContextBase.getMethod()).thenReturn("PATCH");
		Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
		Assert.assertFalse(JsonApiRequestProcessor.isJsonApiRequest(requestContext));

		processor.process(requestContext);
		Mockito.verify(requestContextBase, Mockito.times(0)).setResponse(Mockito.anyInt(), Mockito.any(byte[].class));
	}

	@Test
	public void ignoreRequestForPostAndWildcardContentType() throws IOException {
		Mockito.when(requestContextBase.getMethod()).thenReturn("POST");
		Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
		Assert.assertFalse(JsonApiRequestProcessor.isJsonApiRequest(requestContext));

		processor.process(requestContext);
		Mockito.verify(requestContextBase, Mockito.times(0)).setResponse(Mockito.anyInt(), Mockito.any(byte[].class));
	}


	@Test
	public void getTasks() throws IOException {
		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

		processor.process(requestContext);

		ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(Mockito.eq(200), contentCaptor.capture());

		String json = new String(contentCaptor.getValue());

		Document document = boot.getObjectMapper().readerFor(Document.class).readValue(json);
		Assert.assertTrue(document.getData().isPresent());
	}

	private String createRequestBody(String name) throws JsonProcessingException {
		Task task = new Task();
		task.setId(1L);
		task.setName(name);
		task.setCategory("testCategory");

		JsonApiResponse request = new JsonApiResponse();
		request.setEntity(task);
		Document requestDocument = boot.getDocumentMapper().toDocument(request, new QuerySpecAdapter(new QuerySpec(Task.class),
				boot.getResourceRegistry()));
		return boot.getObjectMapper().writeValueAsString(requestDocument);
	}


	@Test
	public void postTasks() throws IOException {
		String requestBody = createRequestBody("test");
		Mockito.when(requestContextBase.getMethod()).thenReturn("POST");
		Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
		Mockito.when(requestContextBase.getRequestBody()).thenReturn(requestBody.getBytes());
		Mockito.when(requestContextBase.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE))
				.thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);

		processor.process(requestContext);

		ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(Mockito.eq(HttpStatus.CREATED_201), contentCaptor
				.capture
						());

		String json = new String(contentCaptor.getValue());

		Document document = boot.getObjectMapper().readerFor(Document.class).readValue(json);
		Assert.assertTrue(document.getData().isPresent());
		Resource updatedTask = (Resource) document.getData().get();
		Assert.assertEquals("1", updatedTask.getId());
		Assert.assertEquals("tasks", updatedTask.getType());
	}


	@Test
	public void postTasksWithBadRequestException() throws IOException {
		String requestBody = createRequestBody("badName"); // badName triggers an error in repository

		Mockito.when(requestContextBase.getMethod()).thenReturn("POST");
		Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
		Mockito.when(requestContextBase.getRequestBody()).thenReturn(requestBody.getBytes());
		Mockito.when(requestContextBase.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE))
				.thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);

		processor.process(requestContext);

		ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(Mockito.eq(HttpStatus.BAD_REQUEST_400), contentCaptor
				.capture
						());

		String json = new String(contentCaptor.getValue());

		Document document = boot.getObjectMapper().readerFor(Document.class).readValue(json);
		Assert.assertFalse(document.getData().isPresent());
		Assert.assertEquals(1, document.getErrors().size());
		ErrorData errorData = document.getErrors().get(0);
		Assert.assertEquals("400", errorData.getStatus());
	}

	@Test
	public void testHttpRequestProcessorRegistration() throws IOException {
		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

		boot.getRequestDispatcher().process(requestContext);

		ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(Mockito.eq(200), contentCaptor.capture());

		String json = new String(contentCaptor.getValue());

		Document document = boot.getObjectMapper().readerFor(Document.class).readValue(json);
		Assert.assertTrue(document.getData().isPresent());
	}
}
