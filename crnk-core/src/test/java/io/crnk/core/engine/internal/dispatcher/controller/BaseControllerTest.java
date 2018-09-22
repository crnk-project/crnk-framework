package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.CoreTestContainer;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.filter.ResourceModificationFilterBase;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.mock.models.ComplexPojo;
import io.crnk.core.mock.models.Memorandum;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.User;
import io.crnk.core.mock.repository.MockRepositoryUtil;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseControllerTest {

	protected static final long TASK_ID = 1;

	protected static final long PROJECT_ID = 2;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	protected ObjectMapper objectMapper;

	protected PathBuilder pathBuilder;

	protected ResourceRegistry resourceRegistry;

	protected TypeParser typeParser;

	protected DocumentMapper documentMapper;

	protected ModuleRegistry moduleRegistry;

	protected QuerySpecAdapter emptyTaskQuery;

	protected QuerySpecAdapter emptyProjectQuery;

	protected QuerySpecAdapter emptyUserQuery;

	protected QuerySpecAdapter emptyComplexPojoQuery;

	protected QuerySpecAdapter emptyMemorandumQuery;

	protected ResourceModificationFilter modificationFilter;

	protected List<ResourceModificationFilter> modificationFilters;

	protected ControllerContext controllerContext;

	protected PropertiesProvider propertiesProvider;

	protected CoreTestContainer container;
	protected CrnkBoot boot;


	@Before
	public void prepare() {
		propertiesProvider = Mockito.mock(PropertiesProvider.class);

		modificationFilter = Mockito.spy(new ResourceModificationFilterBase());
		modificationFilters = Arrays.asList(modificationFilter);

		SimpleModule testModule = new SimpleModule("test");
		testModule.addResourceModificationFilter(modificationFilter);

		container = new CoreTestContainer();
		container.setDefaultPackage();
		container.addModule(testModule);
		container.getBoot().setPropertiesProvider(propertiesProvider);
		setup(container.getBoot());
		container.boot();

		boot = container.getBoot();
		objectMapper = boot.getObjectMapper();
		resourceRegistry = boot.getResourceRegistry();
		moduleRegistry = boot.getModuleRegistry();
		typeParser = moduleRegistry.getTypeParser();
		pathBuilder = new PathBuilder(resourceRegistry, typeParser);
		documentMapper = boot.getDocumentMapper();

		controllerContext = new ControllerContext(moduleRegistry, () -> documentMapper);

		MockRepositoryUtil.clear();

		emptyTaskQuery = container.toQueryAdapter(new QuerySpec(Task.class));
		emptyProjectQuery = container.toQueryAdapter(new QuerySpec(Project.class));
		emptyUserQuery = container.toQueryAdapter(new QuerySpec(User.class));
		emptyComplexPojoQuery = container.toQueryAdapter(new QuerySpec(ComplexPojo.class));
		emptyMemorandumQuery = container.toQueryAdapter(new QuerySpec(Memorandum.class));
	}

	protected void setup(CrnkBoot boot) {
	}


	protected io.crnk.core.engine.document.Document toDocument(Object resource) {
		DocumentMappingConfig config = new DocumentMappingConfig();
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(resource);
		Result<io.crnk.core.engine.document.Document> document = boot.getDocumentMapper().toDocument(response, null, config);
		return document.get();
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

	protected JsonNode toJson(String value) {
		try {
			return objectMapper.readTree("\"" + value + "\"");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
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
		return createProject(Long.toString(PROJECT_ID), "projects");
	}

	public Resource createProject(String id) {
		return createProject(id, "projects");
	}

	public Resource createProject(String id, String type) {
		Resource data = new Resource();
		data.setType(type);
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
