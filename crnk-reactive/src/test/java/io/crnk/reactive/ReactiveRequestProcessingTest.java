package io.crnk.reactive;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.reactive.model.ReactiveTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Hooks;

public class ReactiveRequestProcessingTest extends ReactiveTestBase {

	private JsonApiRequestProcessor processor;

	private HttpRequestContextBaseAdapter requestContext;

	private HttpRequestContextBase requestContextBase;

	@Before
	public void setup() {
		super.setup();

		ReactiveTask task = new ReactiveTask();
		task.setId(1L);
		task.setName("SomeTask");
		task.setLinksInformation(new ReactiveTask.TaskLinks());
		taskRepository.getMap().put(task.getId(), task);

		processor = new JsonApiRequestProcessor(boot.getModuleRegistry().getContext());
		requestContextBase = Mockito.mock(HttpRequestContextBase.class);
		Mockito.when(requestContextBase.getPath()).thenReturn("/reactive/task/");
		requestContext = new HttpRequestContextBaseAdapter(requestContextBase);
		requestContext.getQueryContext().setBaseUrl(queryContext.getBaseUrl());

		HttpRequestContextProvider requestContextProvider = boot.getModuleRegistry().getHttpRequestContextProvider();
		requestContextProvider.onRequestStarted(requestContext);
	}

	@Test
	public void handleRequestForGetAndWildcardContentType() {
		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/reactive/task/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
		Assert.assertTrue(JsonApiRequestProcessor.isJsonApiRequest(requestContext, false));
	}

	@Test
	public void getTasks() throws IOException {
		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/reactive/task/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
		Hooks.onOperatorDebug();
		HttpResponse response = processor.processAsync(requestContext).get();

		String json = new String(response.getBody());
		Assert.assertEquals(200, response.getStatusCode());
		Document document = boot.getObjectMapper().readerFor(Document.class).readValue(json);
		Assert.assertTrue(document.getData().isPresent());

		List<Resource> resources = document.getCollectionData().get();
		Assert.assertEquals(1, resources.size());
		Resource resource = resources.get(0);
		Assert.assertEquals("http://localhost:8080/reactive/task/1", resource.getLinks().get("self").asText());
		Assert.assertNotNull(resource.getLinks().get("value"));
	}

	private String createRequestBody(String name) throws JsonProcessingException {
		ReactiveTask task = new ReactiveTask();
		task.setId(1L);
		task.setName(name);

		JsonApiResponse request = new JsonApiResponse();
		request.setEntity(task);
		DocumentMappingConfig mappingConfig = new DocumentMappingConfig();
		Document requestDocument = boot.getDocumentMapper().toDocument(request, new QuerySpecAdapter(new QuerySpec(ReactiveTask.class),
				boot.getResourceRegistry(), queryContext), mappingConfig).get();
		return boot.getObjectMapper().writeValueAsString(requestDocument);
	}


	@Test
	public void postTasks() throws IOException {
		String requestBody = createRequestBody("test");
		Mockito.when(requestContextBase.getMethod()).thenReturn("POST");
		Mockito.when(requestContextBase.getPath()).thenReturn("/reactive/task/");
		Mockito.when(requestContextBase.getRequestBody()).thenReturn(requestBody.getBytes());
		Mockito.when(requestContextBase.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE))
				.thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);

		processor.process(requestContext);

		ArgumentCaptor<HttpResponse> contentCaptor = ArgumentCaptor.forClass(HttpResponse.class);
		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(contentCaptor.capture());

		String json = new String(contentCaptor.getValue().getBody());
		Assert.assertEquals(HttpStatus.CREATED_201, contentCaptor.getValue().getStatusCode());

		Document document = boot.getObjectMapper().readerFor(Document.class).readValue(json);
		Assert.assertTrue(document.getData().isPresent());
		Resource updatedTask = (Resource) document.getData().get();
		Assert.assertEquals("1", updatedTask.getId());
		Assert.assertEquals("reactive/task", updatedTask.getType());
	}


	@Test
	public void postTasksWithBadRequestException() throws IOException {
		String requestBody = createRequestBody("badName"); // badName triggers an error in repository

		Mockito.when(requestContextBase.getMethod()).thenReturn("POST");
		Mockito.when(requestContextBase.getPath()).thenReturn("/reactive/task/");
		Mockito.when(requestContextBase.getRequestBody()).thenReturn(requestBody.getBytes());
		Mockito.when(requestContextBase.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE))
				.thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);

		processor.process(requestContext);

		ArgumentCaptor<HttpResponse> contentCaptor = ArgumentCaptor.forClass(HttpResponse.class);
		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(contentCaptor.capture());

		Assert.assertEquals(HttpStatus.BAD_REQUEST_400, contentCaptor.getValue().getStatusCode());
		String json = new String(contentCaptor.getValue().getBody());

		Document document = boot.getObjectMapper().readerFor(Document.class).readValue(json);
		Assert.assertFalse(document.getData().isPresent());
		Assert.assertEquals(1, document.getErrors().size());
		ErrorData errorData = document.getErrors().get(0);
		Assert.assertEquals("400", errorData.getStatus());
	}

	@Test
	public void requestWithInvalidJson() throws IOException {
		Mockito.when(requestContextBase.getMethod()).thenReturn("POST");
		Mockito.when(requestContextBase.getPath()).thenReturn("/reactive/task/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
		Mockito.when(requestContextBase.getRequestHeader(HttpHeaders.HTTP_CONTENT_TYPE))
				.thenReturn(HttpHeaders.JSONAPI_CONTENT_TYPE);
		Mockito.when(requestContext.getRequestBody()).thenReturn("{ INVALID }".getBytes());

		Assert.assertTrue(JsonApiRequestProcessor.isJsonApiRequest(requestContext, false));

		processor.process(requestContext);

		ArgumentCaptor<HttpResponse> contentCaptor = ArgumentCaptor.forClass(HttpResponse.class);
		Mockito.verify(requestContextBase, Mockito.times(1))
				.setResponse(contentCaptor.capture());

		Assert.assertEquals(HttpStatus.BAD_REQUEST_400, contentCaptor.getValue().getStatusCode());

		String json = new String(contentCaptor.getValue().getBody());

		Document document = boot.getObjectMapper().readerFor(Document.class).readValue(json);
		Assert.assertFalse(document.getData().isPresent());
		Assert.assertEquals(1, document.getErrors().size());
		ErrorData errorData = document.getErrors().get(0);
		Assert.assertEquals("400", errorData.getStatus());
		Assert.assertEquals("Json Parsing failed", errorData.getTitle());
		Assert.assertNotNull(errorData.getDetail());
	}
}
