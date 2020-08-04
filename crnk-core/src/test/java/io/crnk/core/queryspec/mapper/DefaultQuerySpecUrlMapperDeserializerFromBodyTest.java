package io.crnk.core.queryspec.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.TestHttpRequestContext;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.TaskWithPagingBehavior;
import io.crnk.core.mock.repository.TaskWithPagingBehaviorRepository;
import io.crnk.core.mock.repository.TaskWithPagingBehaviorToProjectRepository;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.*;
import io.crnk.core.queryspec.pagingspec.CustomOffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultQuerySpecUrlMapperDeserializerFromBodyTest extends AbstractQuerySpecTest {
	protected DefaultQuerySpecUrlMapper urlMapper;

	protected ResourceInformation taskInformation;

	private ResourceInformation taskWithPagingBehaviorInformation;

	private QuerySpecUrlContext deserializerContext;
	private TestHttpRequestContext httpRequestContext;

	@Before
	public void setup() {
		super.setup();

		deserializerContext = new QuerySpecUrlContext() {

			@Override
			public ResourceRegistry getResourceRegistry() {
				return resourceRegistry;
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}

			@Override
			public ObjectMapper getObjectMapper() {
				return container.getObjectMapper();
			}

			@Override
			public UrlBuilder getUrlBuilder() {
				return container.getModuleRegistry().getUrlBuilder();
			}
		};

		urlMapper = new DefaultQuerySpecUrlMapper();
		urlMapper.setFilterCriteriaInRequestBody(true);
		urlMapper.init(deserializerContext);

		RegistryEntry taskEntry = resourceRegistry.getEntry(Task.class);
		taskInformation = taskEntry.getResourceInformation();
		taskWithPagingBehaviorInformation = resourceRegistry.getEntry(TaskWithPagingBehavior.class).getResourceInformation();

		httpRequestContext = new TestHttpRequestContext();
		queryContext.setRequestContext(httpRequestContext);
	}

	@Override
	protected void setup(CoreTestContainer container) {
		super.setup(container);
		SimpleModule customPagingModule = new SimpleModule("customPaging");
		customPagingModule.addRepository(new TaskWithPagingBehaviorRepository());
		customPagingModule.addRepository(new TaskWithPagingBehaviorToProjectRepository());
		customPagingModule.addPagingBehavior(new OffsetLimitPagingBehavior());
		customPagingModule.addPagingBehavior(new CustomOffsetLimitPagingBehavior());
		container.addModule(customPagingModule);
	}

	@Test
	public void testEmptyFilter() {
		QuerySpec querySpec = urlMapper.deserialize(taskInformation, Collections.emptyMap(), queryContext);
		assertNotNull(querySpec.getFilters());
		assertTrue(querySpec.getFilters().isEmpty());
	}

	@Test
	public void testSimpleEqualsFilter() {
		httpRequestContext.setRequestBody("{\"name\": \"test\"}");
		QuerySpec querySpec = urlMapper.deserialize(taskInformation, Collections.emptyMap(), queryContext);
		List<FilterSpec> filters = querySpec.getFilters();
		assertNotNull(filters);
		assertEquals(1, filters.size());
		FilterSpec filter = filters.get(0);
		assertEquals(FilterOperator.EQ, filter.getOperator());
		assertEquals(Collections.singletonList("name"), filter.getAttributePath());
		assertEquals("test", filter.getValue());
	}

	@Test
	public void testCompoundFilter() {
		httpRequestContext.setRequestBody("{ \"OR\": [ {\"id\": [12, 13, 14]}, {\"name\": \"test\"} ] }");
		QuerySpec querySpec = urlMapper.deserialize(taskInformation, Collections.emptyMap(), queryContext);
		List<FilterSpec> filters = querySpec.getFilters();
		assertNotNull(filters);
		assertEquals(1, filters.size());
		FilterSpec filter = filters.get(0);
		assertEquals(FilterOperator.OR, filter.getOperator());
		List<FilterSpec> expression = Arrays.asList(
				PathSpec.of("id").filter(FilterOperator.EQ, Arrays.asList(12L, 13L, 14L)),
				PathSpec.of("name").filter(FilterOperator.EQ, "test")
		);
		assertEquals(expression, filter.getExpression());
	}

	@Test(expected = IllegalStateException.class)
	public void testFilterInUrlResultsInException() {
		urlMapper.deserialize(taskInformation, Collections.singletonMap("filter[name]", Collections.singleton("test")), queryContext);
	}
}
