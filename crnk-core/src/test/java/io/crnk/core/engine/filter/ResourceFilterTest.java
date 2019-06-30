package io.crnk.core.engine.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.utils.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ResourceFilterTest {

    private ResourceFilter filter = Mockito.spy(new ResourceFilterBase());

    private CoreTestContainer container;

    @Before
    public void prepare() {
        SimpleModule filterModule = new SimpleModule("filter");
        filterModule.addResourceFilter(filter);

        container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.addModule(filterModule);
        container.boot();
    }


    @Test
    public void testForbidResource() {
        ResourceInformation resourceInformation = container.getEntry(Task.class).getResourceInformation();

        String path = "/tasks/";
        String method = HttpMethod.GET.toString();
        Map<String, Set<String>> parameters = Collections.emptyMap();

        Document requestBody = null;

        // forbid resource
        Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
        HttpRequestDispatcherImpl requestDispatcher = container.getBoot().getRequestDispatcher();
        Response response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.FORBIDDEN_403, response.getHttpStatus().intValue());

        // allow resource but cache prevents access
        Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.FORBIDDEN_403, response.getHttpStatus().intValue());

        // clear cache
        container.getQueryContext().getAttributes().clear();
        response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
    }

    @Test
    public void testUnauthorizedResource() {
        ResourceInformation resourceInformation = container.getEntry(Task.class).getResourceInformation();

        String path = "/tasks/";
        String method = HttpMethod.GET.toString();
        Map<String, Set<String>> parameters = Collections.emptyMap();

        Document requestBody = null;

        // forbid resource
        Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.UNAUTHORIZED);
        HttpRequestDispatcherImpl requestDispatcher = container.getBoot().getRequestDispatcher();
        Response response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, response.getHttpStatus().intValue());

        // allow resource but cache prevents access
        Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED_401, response.getHttpStatus().intValue());

        // clear cache
        container.getQueryContext().getAttributes().clear();
        response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
    }

    @Test
    public void checkFilterGetOnResourceField() {
        // setup test data
        RegistryEntry entry = container.getEntry(Task.class);
        ResourceRepositoryAdapter resourceRepository = entry.getResourceRepository();
        Project project = new Project();
        project.setId(13L);
        project.setName("myProject");
        Task task = new Task();
        task.setId(12L);
        task.setName("myTask");
        task.setProject(project);
        resourceRepository.create(task, container.toQueryAdapter(new QuerySpec(Task.class)));

        // get information
        ResourceInformation resourceInformation = entry.getResourceInformation();
        ResourceField projectField = resourceInformation.findFieldByUnderlyingName("project");
        ResourceField nameField = resourceInformation.findFieldByUnderlyingName("name");

        String path = "/tasks/";
        String method = HttpMethod.GET.toString();
        Map<String, Set<String>> parameters = Collections.emptyMap();
        Document requestBody = null;

        // forbid field
        HttpRequestDispatcherImpl requestDispatcher = container.getBoot().getRequestDispatcher();
        Mockito.when(filter.filterField(Mockito.eq(projectField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
        Response response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
        Resource taskResource = response.getDocument().getCollectionData().get().get(0);
        Assert.assertTrue(taskResource.getRelationships().containsKey("projects"));
        Assert.assertFalse(taskResource.getRelationships().containsKey("project"));
        Assert.assertTrue(taskResource.getAttributes().containsKey("name"));

        // allow resource
        container.getQueryContext().getAttributes().clear();
        Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
        response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
        taskResource = response.getDocument().getCollectionData().get().get(0);
        Assert.assertTrue(taskResource.getRelationships().containsKey("projects"));
        Assert.assertFalse(taskResource.getRelationships().containsKey("project"));
        Assert.assertFalse(taskResource.getAttributes().containsKey("name"));
    }

    @Test
    public void checkMutationsOnForbiddenField() throws IOException {
        ObjectMapper objectMapper = container.getBoot().getObjectMapper();
        RegistryEntry entry = container.getEntry(Task.class);
        ResourceInformation resourceInformation = entry.getResourceInformation();
        ResourceField nameField = resourceInformation.findFieldByUnderlyingName("name");

        // prepare test data
        ResourceRepositoryAdapter resourceRepository = entry.getResourceRepository();
        Resource task = new Resource();
        task.setType("tasks");
        task.setId("12");
        task.setAttribute("name", objectMapper.readTree("\"Doe\""));

        String path = "/tasks/";
        String method = HttpMethod.POST.toString();
        Map<String, Set<String>> parameters = Collections.emptyMap();
        Document requestBody = new Document();
        requestBody.setData(Nullable.of(task));

        // try save while forbidden
        QueryContext queryContext = container.getQueryContext();
        HttpRequestDispatcherImpl requestDispatcher = container.getBoot().getRequestDispatcher();
        queryContext.getAttributes().clear();
        Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
        Response response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.FORBIDDEN_403, response.getHttpStatus().intValue());

        // try save with ok
        queryContext.getAttributes().clear();
        Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

        // try update while forbidden
        queryContext.getAttributes().clear();
        path = "/tasks/" + task.getId();
        method = HttpMethod.PATCH.toString();
        Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.FORBIDDEN);
        response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.FORBIDDEN_403, response.getHttpStatus().intValue());

        queryContext.getAttributes().clear();
        Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.NONE);
        response = requestDispatcher.dispatchRequest(path, method, parameters, requestBody);
        Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
    }
}
