package io.crnk.core.engine.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.engine.result.Result;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.TaskLinks;
import io.crnk.core.mock.repository.TaskRepository;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.response.JsonApiResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

public class JsonApiRequestProcessorTest {

    private JsonApiRequestProcessor processor;

    private Module.ModuleContext moduleContext;

    private CoreTestContainer container;

    private HttpRequestContextBase requestContextBase;

    private HttpRequestContextBaseAdapter requestContext;


    @Before
    public void setup() {
        TaskRepository.clear();

        container = new CoreTestContainer();
        container.setDefaultPackage();
        container.addModule(new Module() {
            @Override
            public String getModuleName() {
                return "test";
            }

            @Override
            public void setupModule(ModuleContext context) {
                moduleContext = context;
            }
        });
        container.boot();


        Task task = new Task();
        task.setId(1L);
        task.setName("SomeTask");
        task.setLinksInformation(new TaskLinks());
        TaskRepository tasks = new TaskRepository();
        tasks.save(task);

        processor = new JsonApiRequestProcessor(moduleContext);

        requestContextBase = container.getRequestContextBase();
        requestContext = container.getRequestContext();
    }

    @After
    public void teardown() {
        TaskRepository.clear();
    }

    @Test
    public void ignoreRequestForContentTypeMismatch() {
        Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
        Mockito.when(requestContextBase.getRequestHeader("Accept"))
                .thenReturn("*");
        Assert.assertTrue(JsonApiRequestProcessor.isJsonApiRequest(requestContext, false));
        Mockito.when(requestContextBase.getRequestHeader("Accept"))
                .thenReturn("something");
        Assert.assertFalse(JsonApiRequestProcessor.isJsonApiRequest(requestContext, false));
    }


    @Test
    public void acceptPlainJsonDependingOnFlag() {
        Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
        Mockito.when(requestContextBase.getRequestHeader("Accept"))
                .thenReturn("application/json");
        Assert.assertTrue(JsonApiRequestProcessor.isJsonApiRequest(requestContext, true));
        Mockito.when(requestContextBase.getRequestHeader("Accept"))
                .thenReturn("application/json");
        Assert.assertFalse(JsonApiRequestProcessor.isJsonApiRequest(requestContext, false));
    }

    @Test
    public void handleRequestForGetAndWildcardContentType() {
        Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
        Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
        Assert.assertTrue(JsonApiRequestProcessor.isJsonApiRequest(requestContext, false));
    }

    @Test
    public void ignoreRequestForPatchAndWildcardContentType() throws IOException {
        Mockito.when(requestContextBase.getMethod()).thenReturn("PATCH");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/12");
        Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
        Assert.assertFalse(JsonApiRequestProcessor.isJsonApiRequest(requestContext, false));

        processor.process(requestContext);
        Mockito.verify(requestContextBase, Mockito.times(0)).setResponse(Mockito.any(HttpResponse.class));
    }

    @Test
    public void return405ForInvalidMethod() {
        Mockito.when(requestContextBase.getMethod()).thenReturn("PUT");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/12");
        Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

        Result<HttpResponse> result = processor.processAsync(requestContext);
        Assert.assertEquals(HttpStatus.METHOD_NOT_ALLOWED_405, result.get().getStatusCode());
    }

    @Test
    public void ignoreRequestForPostAndWildcardContentType() throws IOException {
        Mockito.when(requestContextBase.getMethod()).thenReturn("POST");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
        Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
        Assert.assertFalse(JsonApiRequestProcessor.isJsonApiRequest(requestContext, false));

        processor.process(requestContext);
        Mockito.verify(requestContextBase, Mockito.times(0)).setResponse(Mockito.any(HttpResponse.class));
    }

    @Test
    public void getTasks() throws IOException {
        Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
        Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

        processor.process(requestContext);

        ArgumentCaptor<HttpResponse> contentCaptor = ArgumentCaptor.forClass(HttpResponse.class);
        Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(contentCaptor.capture());

        String json = new String(contentCaptor.getValue().getBody());
        Assert.assertEquals(200, contentCaptor.getValue().getStatusCode());
        Document document = container.getBoot().getObjectMapper().readerFor(Document.class).readValue(json);
        Assert.assertTrue(document.getData().isPresent());

        List<Resource> resources = document.getCollectionData().get();
        Assert.assertEquals(1, resources.size());
        Resource resource = resources.get(0);
        Assert.assertEquals("http://127.0.0.1/tasks/1", resource.getLinks().get("self").asText());
        Assert.assertNotNull(resource.getLinks().get("value"));
    }

    @Test
    public void getTasksWithCompactHeader() throws IOException {
        Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
        Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");
        Mockito.when(requestContextBase.getRequestHeader(HttpHeaders.HTTP_HEADER_CRNK_COMPACT)).thenReturn("true");

        processor.process(requestContext);

        ArgumentCaptor<HttpResponse> contentCaptor = ArgumentCaptor.forClass(HttpResponse.class);
        Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(contentCaptor.capture());

        Assert.assertEquals(200, contentCaptor.getValue().getStatusCode());
        String json = new String(contentCaptor.getValue().getBody());

        Document document = container.getObjectMapper().readerFor(Document.class).readValue(json);
        Assert.assertTrue(document.getData().isPresent());
        List<Resource> resources = document.getCollectionData().get();
        Assert.assertEquals(1, resources.size());
        Resource resource = resources.get(0);
        Assert.assertNull(resource.getLinks().get("self"));
        Assert.assertNotNull(resource.getLinks().get("value"));
    }


    private String createRequestBody(String name) throws JsonProcessingException {
        Task task = new Task();
        task.setId(1L);
        task.setName(name);
        task.setCategory("testCategory");

        JsonApiResponse request = new JsonApiResponse();
        request.setEntity(task);
        DocumentMappingConfig mappingConfig = new DocumentMappingConfig();
        Document requestDocument = container.getDocumentMapper()
                .toDocument(request, container.toQueryAdapter(new QuerySpec(Task.class)), mappingConfig).get();
        return container.getObjectMapper().writeValueAsString(requestDocument);
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

        ArgumentCaptor<HttpResponse> contentCaptor = ArgumentCaptor.forClass(HttpResponse.class);
        Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(contentCaptor.capture());

        String json = new String(contentCaptor.getValue().getBody());
        Assert.assertEquals(HttpStatus.CREATED_201, contentCaptor.getValue().getStatusCode());

        Document document = container.getObjectMapper().readerFor(Document.class).readValue(json);
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

        ArgumentCaptor<HttpResponse> contentCaptor = ArgumentCaptor.forClass(HttpResponse.class);
        Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(contentCaptor.capture());

        Assert.assertEquals(HttpStatus.BAD_REQUEST_400, contentCaptor.getValue().getStatusCode());
        String json = new String(contentCaptor.getValue().getBody());

        Document document = container.getObjectMapper().readerFor(Document.class).readValue(json);
        Assert.assertFalse(document.getData().isPresent());
        Assert.assertEquals(1, document.getErrors().size());
        ErrorData errorData = document.getErrors().get(0);
        Assert.assertEquals("400", errorData.getStatus());
    }

    @Test
    public void requestWithInvalidJson() throws IOException {
        Mockito.when(requestContextBase.getMethod()).thenReturn("POST");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
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

        Document document = container.getObjectMapper().readerFor(Document.class).readValue(json);
        Assert.assertFalse(document.getData().isPresent());
        Assert.assertEquals(1, document.getErrors().size());
        ErrorData errorData = document.getErrors().get(0);
        Assert.assertEquals("400", errorData.getStatus());
        Assert.assertEquals("Json Parsing failed", errorData.getTitle());
        Assert.assertNotNull(errorData.getDetail());
    }
}
