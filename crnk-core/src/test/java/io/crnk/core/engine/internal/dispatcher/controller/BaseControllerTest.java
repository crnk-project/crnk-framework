package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformationBuilder;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.jackson.JsonApiModuleBuilder;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.EmptyPropertiesProvider;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.mock.repository.*;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.resource.registry.ResourceRegistryBuilderTest;
import io.crnk.core.resource.registry.ResourceRegistryTest;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.legacy.registry.ResourceRegistryBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BaseControllerTest {

	protected static final long TASK_ID = 1;

	protected static final long PROJECT_ID = 2;

	protected static final QueryParams REQUEST_PARAMS = new QueryParams();

	protected static final PropertiesProvider PROPERTIES_PROVIDER = new EmptyPropertiesProvider();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	protected ObjectMapper objectMapper;
	protected PathBuilder pathBuilder;
	protected ResourceRegistry resourceRegistry;
	protected TypeParser typeParser;
	protected DocumentMapper documentMapper;
	protected QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder(new DefaultQueryParamsParser());
	protected ModuleRegistry moduleRegistry;

	@Before
	public void prepare() {
		moduleRegistry = new ModuleRegistry();
		ResourceInformationBuilder resourceInformationBuilder =
				new AnnotationResourceInformationBuilder(new ResourceFieldNameTransformer());
		ResourceRegistryBuilder registryBuilder =
				new ResourceRegistryBuilder(moduleRegistry, new SampleJsonServiceLocator(), resourceInformationBuilder);
		resourceRegistry = registryBuilder.build(ResourceRegistryBuilderTest.TEST_MODELS_PACKAGE, moduleRegistry,
				new ConstantServiceUrlProvider(ResourceRegistryTest.TEST_MODELS_URL));
		pathBuilder = new PathBuilder(resourceRegistry);
		typeParser = moduleRegistry.getTypeParser();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JsonApiModuleBuilder().build());
		documentMapper = new DocumentMapper(resourceRegistry, objectMapper, new EmptyPropertiesProvider());
		UserRepository.clear();
		ProjectRepository.clear();
		TaskRepository.clear();
		UserToProjectRepository.clear();
		TaskToProjectRepository.clear();
		ProjectToTaskRepository.clear();
	}

	public Resource createTask() {
		Resource data = new Resource();
		data.setType("tasks");
		data.setId("1");

		try {
			data.setAttribute("name", objectMapper.readTree("\"sample task\""));
			data.setAttribute("data", objectMapper.readTree("\"asd\""));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return data;
	}

	public Resource createUser() {
		Resource data = new Resource();
		data.setType("users");
		data.setId("3");

		try {
			data.setAttribute("name", objectMapper.readTree("\"sample user\""));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return data;
	}

	public Resource createProject() {
		return createProject(Long.toString(PROJECT_ID));
	}

	public Resource createProject(String id) {
		Resource data = new Resource();
		data.setType("projects");
		data.setId(id);

		try {
			data.setAttribute("name", objectMapper.readTree("\"sample project\""));
			data.setAttribute("data", objectMapper.readTree("{\"data\" : \"asd\"}"));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return data;
	}

	protected void addParams(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}
}
