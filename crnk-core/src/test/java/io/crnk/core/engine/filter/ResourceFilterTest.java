package io.crnk.core.engine.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.MockConstants;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.UserRepository;
import io.crnk.core.mock.repository.UserToProjectRepository;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import io.crnk.core.utils.Nullable;
import org.junit.After;
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

	private ResourceRegistry resourceRegistry;

	private CrnkBoot boot;

	@Before
	@After
	public void cleanup() {
		UserRepository.clear();
		UserToProjectRepository.clear();
	}

	@Before
	public void prepare() {
		boot = new CrnkBoot();
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery(MockConstants.TEST_MODELS_PACKAGE));
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));

		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addResourceFilter(filter);
		boot.addModule(filterModule);
		boot.boot();
		resourceRegistry = boot.getResourceRegistry();
	}


	@Test
	public void testFilterResource() {
		ResourceInformation resourceInformation = resourceRegistry.getEntry(Task.class).getResourceInformation();

		String path = "/tasks/";
		String method = HttpMethod.GET.toString();
		Map<String, Set<String>> parameters = Collections.emptyMap();

		Document requestBody = null;

		// forbid resource
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
		Response response = boot.getRequestDispatcher().dispatchRequest(path, method, parameters, null, requestBody);
		Assert.assertEquals(HttpStatus.FORBIDDEN_403, response.getHttpStatus().intValue());

		// allow resource
		Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
		response = boot.getRequestDispatcher().dispatchRequest(path, method, parameters, null, requestBody);
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
	}

	@Test
	public void checkFilterGetOnResourceField() {
		// setup test data
		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
		ResourceRepositoryAdapter resourceRepository = entry.getResourceRepository();
		Project project = new Project();
		project.setId(13L);
		project.setName("myProject");
		Task task = new Task();
		task.setId(12L);
		task.setName("myTask");
		task.setProject(project);
		resourceRepository.create(task, new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry));

		// get information
		ResourceInformation resourceInformation = entry.getResourceInformation();
		ResourceField projectField = resourceInformation.findFieldByUnderlyingName("project");
		ResourceField nameField = resourceInformation.findFieldByUnderlyingName("name");

		String path = "/tasks/";
		String method = HttpMethod.GET.toString();
		Map<String, Set<String>> parameters = Collections.emptyMap();
		Document requestBody = null;

		// forbid field
		Mockito.when(filter.filterField(Mockito.eq(projectField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
		Response response = boot.getRequestDispatcher().dispatchRequest(path, method, parameters, null, requestBody);
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		Resource taskResource = response.getDocument().getCollectionData().get().get(0);
		Assert.assertTrue(taskResource.getRelationships().containsKey("projects"));
		Assert.assertFalse(taskResource.getRelationships().containsKey("project"));
		Assert.assertTrue(taskResource.getAttributes().containsKey("name"));

		// allow resource
		Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
		response = boot.getRequestDispatcher().dispatchRequest(path, method, parameters, null, requestBody);
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
		taskResource = response.getDocument().getCollectionData().get().get(0);
		Assert.assertTrue(taskResource.getRelationships().containsKey("projects"));
		Assert.assertFalse(taskResource.getRelationships().containsKey("project"));
		Assert.assertFalse(taskResource.getAttributes().containsKey("name"));
	}

	@Test
	public void checkMutationsOnForbiddenField() throws IOException {
		ObjectMapper objectMapper = boot.getObjectMapper();
		RegistryEntry entry = resourceRegistry.getEntry(Task.class);
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
		requestBody.setData(Nullable.of((Object) task));

		// try save while forbidden
		Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
		Response response = boot.getRequestDispatcher().dispatchRequest(path, method, parameters, null, requestBody);
		Assert.assertEquals(HttpStatus.FORBIDDEN_403, response.getHttpStatus().intValue());

		// try save with ok
		Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
		response = boot.getRequestDispatcher().dispatchRequest(path, method, parameters, null, requestBody);
		Assert.assertEquals(HttpStatus.CREATED_201, response.getHttpStatus().intValue());

		// try update while forbidden
		path = "/tasks/" + task.getId();
		method = HttpMethod.PATCH.toString();
		Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.FORBIDDEN);
		response = boot.getRequestDispatcher().dispatchRequest(path, method, parameters, null, requestBody);
		Assert.assertEquals(HttpStatus.FORBIDDEN_403, response.getHttpStatus().intValue());

		Mockito.when(filter.filterField(Mockito.eq(nameField), Mockito.eq(HttpMethod.PATCH))).thenReturn(FilterBehavior.NONE);
		response = boot.getRequestDispatcher().dispatchRequest(path, method, parameters, null, requestBody);
		Assert.assertEquals(HttpStatus.OK_200, response.getHttpStatus().intValue());
	}
}
