package io.crnk.core.queryspec.mapper;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.TaskWithLookup;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.*;
import io.crnk.core.queryspec.pagingspec.CustomOffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.queryspec.repository.TaskWithPagingBehavior;
import io.crnk.core.queryspec.repository.TaskWithPagingBehaviorQuerySpecRepository;
import io.crnk.core.queryspec.repository.TaskWithPagingBehaviorToProjectRelationshipRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

public abstract class DefaultQuerySpecUrlMapperDeserializerTestBase extends AbstractQuerySpecTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	protected DefaultQuerySpecUrlMapper urlMapper;

	protected ResourceInformation taskInformation;

	private ResourceInformation taskWithPagingBehaviorInformation;

	private QuerySpecDeserializerContext deserializerContext;

	@Before
	public void setup() {
		super.setup();

		deserializerContext = new QuerySpecDeserializerContext() {

			@Override
			public ResourceRegistry getResourceRegistry() {
				return resourceRegistry;
			}

			@Override
			public TypeParser getTypeParser() {
				return moduleRegistry.getTypeParser();
			}
		};

		urlMapper = new DefaultQuerySpecUrlMapper();
		urlMapper.init(deserializerContext);

		RegistryEntry taskEntry = resourceRegistry.getEntry(Task.class);
		taskInformation = taskEntry.getResourceInformation();
		taskWithPagingBehaviorInformation = resourceRegistry.getEntry(TaskWithPagingBehavior.class).getResourceInformation();
	}


	@Override
	protected void setup(CoreTestContainer container) {
		container.addModule(new CoreTestModule());

		SimpleModule customPagingModule = new SimpleModule("customPaging");
		customPagingModule.addRepository(new TaskWithPagingBehaviorQuerySpecRepository());
		customPagingModule.addRepository(new TaskWithPagingBehaviorToProjectRelationshipRepository());
		customPagingModule.addPagingBehavior(new CustomOffsetLimitPagingBehavior());
		container.addModule(customPagingModule);
	}

	@Test
	public void operations() {
		Assert.assertFalse(urlMapper.getAllowUnknownAttributes());
		urlMapper.setAllowUnknownAttributes(true);
		Assert.assertTrue(urlMapper.getAllowUnknownAttributes());
		urlMapper.getSupportedOperators().clear();
		urlMapper.setDefaultOperator(FilterOperator.LIKE);
		urlMapper.addSupportedOperator(FilterOperator.LIKE);
		Assert.assertEquals(FilterOperator.LIKE, urlMapper.getDefaultOperator());
		Assert.assertEquals(1, urlMapper.getSupportedOperators().size());
	}

	@Test
	public void checkIgnoreParseExceptions() {
		Assert.assertFalse(urlMapper.isIgnoreParseExceptions());
		urlMapper.setIgnoreParseExceptions(true);
		Assert.assertTrue(urlMapper.isIgnoreParseExceptions());

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[id]", "notAnInteger");
		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, "notAnInteger"));
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test(expected = ParametersDeserializationException.class)
	public void throwParseExceptionsByDefault() {
		Assert.assertFalse(urlMapper.isIgnoreParseExceptions());

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[id]", "notAnInteger");
		urlMapper.deserialize(taskInformation, params);
	}

	@Test(expected = ParametersDeserializationException.class)
	public void throwParseExceptionsForMultiValuedOffset() {
		Map<String, Set<String>> params = new HashMap<>();
		params.put("page[offset]", new HashSet<>(Arrays.asList("1", "2")));
		urlMapper.deserialize(taskInformation, params);
	}

	@Test(expected = ParametersDeserializationException.class)
	public void throwParseExceptionsWhenBracketsNotClosed() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[", "someValue");
		urlMapper.deserialize(taskInformation, params);
	}

	@Test
	public void testFindAll() {
		Map<String, Set<String>> params = new HashMap<>();

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void defaultPaginationOnRoot() {
		Map<String, Set<String>> params = new HashMap<>();
		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(0L, actualSpec.getOffset());
		Assert.assertNull(actualSpec.getLimit());
	}

	@Test
	public void defaultPaginationOnRelation() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[projects]", "name");
		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(0L, actualSpec.getOffset());
		Assert.assertNull(actualSpec.getLimit());
		QuerySpec projectQuerySpec = actualSpec.getQuerySpec(Project.class);
		Assert.assertNotNull(projectQuerySpec);
		Assert.assertEquals(0L, projectQuerySpec.getOffset());
		Assert.assertNull(projectQuerySpec.getLimit());
	}

	@Test
	public void mapJsonToJavaNames() {
		ResourceInformation scheduleInformation = resourceRegistry.getEntry(Schedule.class).getResourceInformation();
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort", "description");
		add(params, "filter[description]", "test");
		add(params, "fields", "description");
		add(params, "include", "followup");
		QuerySpec actualSpec = urlMapper.deserialize(scheduleInformation, params);

		Assert.assertEquals(1, actualSpec.getSort().size());
		Assert.assertEquals(1, actualSpec.getFilters().size());
		Assert.assertEquals(1, actualSpec.getIncludedFields().size());
		Assert.assertEquals(1, actualSpec.getIncludedRelations().size());

		SortSpec sortSpec = actualSpec.getSort().get(0);
		FilterSpec filterSpec = actualSpec.getFilters().get(0);
		IncludeRelationSpec includeRelationSpec = actualSpec.getIncludedRelations().get(0);
		IncludeFieldSpec includeFieldSpec = actualSpec.getIncludedFields().get(0);

		Assert.assertEquals(Arrays.asList("desc"), sortSpec.getAttributePath());
		Assert.assertEquals(Arrays.asList("desc"), filterSpec.getAttributePath());
		Assert.assertEquals(Arrays.asList("followupProject"), includeRelationSpec.getAttributePath());
		Assert.assertEquals(Arrays.asList("desc"), includeFieldSpec.getAttributePath());
	}

	@Test
	public void customPaginationOnRoot() {
		Map<String, Set<String>> params = new HashMap<>();
		QuerySpec actualSpec = urlMapper.deserialize(taskWithPagingBehaviorInformation, params);
		Assert.assertEquals(1L, actualSpec.getOffset());
		Assert.assertEquals(10L, actualSpec.getLimit().longValue());
	}

	@Test
	public void customPaginationOnRelation() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[projects]", "name");
		QuerySpec actualSpec = urlMapper.deserialize(taskWithPagingBehaviorInformation, params);
		Assert.assertEquals(1L, actualSpec.getOffset());
		Assert.assertEquals(10L, actualSpec.getLimit().longValue());
		QuerySpec projectQuerySpec = actualSpec.getQuerySpec(Project.class);
		Assert.assertNotNull(projectQuerySpec);
		Assert.assertEquals(0L, projectQuerySpec.getOffset());
		Assert.assertNull(projectQuerySpec.getLimit());
	}

	@Test
	public void testFindAllOrderByAsc() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[tasks]", "name");
		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFollowNestedObjectWithinResource() {
		// follow ProjectData.data
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("data", "data"), Direction.ASC));

		ResourceInformation projectInformation = resourceRegistry.getEntry(Project.class).getResourceInformation();

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort", "data.data");
		QuerySpec actualSpec = urlMapper.deserialize(projectInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testOrderByMultipleAttributes() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		expectedSpec.addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[tasks]", "name,id");
		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFindAllOrderByDesc() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[tasks]", "-name");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterWithDefaultOp() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][name]", "value");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterWithComputedAttribute() {
		// if computeAttribte is not found, module order is wrong
		// tests setup with information builder must be hardened
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("computedAttribute"), FilterOperator.EQ, 13));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][computedAttribute]", "13");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterWithDotNotation() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("project", "name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[project.name]", "value");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterWithDotNotationMultipleElements() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("project", "task", "name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[project.task.name]", "value");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testUnknownPropertyAllowed() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("doesNotExists"), FilterOperator.EQ, "value"));

		urlMapper.setAllowUnknownAttributes(true);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][doesNotExists]", "value");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test(expected = BadRequestException.class)
	public void testUnknownPropertyNotAllowed() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("doesNotExists"), FilterOperator.EQ, "value"));

		urlMapper.setAllowUnknownAttributes(false);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][doesNotExists]", "value");

		urlMapper.deserialize(taskInformation, params);
	}

	@Test
	public void testUnknownParameterAllowed() {
		urlMapper.setAllowUnknownParameters(true);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "doesNotExists[tasks]", "value");

		Assert.assertNotNull(urlMapper.deserialize(taskInformation, params));
	}

	@Test(expected = ParametersDeserializationException.class)
	public void testUnknownParameterNotAllowed() {
		urlMapper.setAllowUnknownParameters(false);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "doesNotExists[tasks]", "value");

		urlMapper.deserialize(taskInformation, params);
	}

	@Test
	public void testFilterByOne() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][name][EQ]", "value");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterByMany() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(
				new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, new HashSet<>(Arrays.asList("value1", "value2"))));

		Map<String, Set<String>> params = new HashMap<>();
		params.put("filter[tasks][name][EQ]", new HashSet<>(Arrays.asList("value1", "value2")));

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterEquals() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 1L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][id][EQ]", "1");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterGreater() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[tasks][id][LE]", "1");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testFilterGreaterOnRoot() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[id][LE]", "1");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testPaging() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.setPagingSpec(new OffsetLimitPagingSpec(1L, 2L));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "1");
		add(params, "page[limit]", "2");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void deserializeUnknownParameter() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "doesNotExist[something]", "someValue");

		final boolean[] deserialized = new boolean[1];
		urlMapper = new DefaultQuerySpecUrlMapper() {

			@Override
			protected void deserializeUnknown(final QuerySpec querySpec, final QueryParameter parameter) {
				Assert.assertEquals(QueryParameterType.UNKNOWN, parameter.getType());
				Assert.assertEquals("doesNotExist[something]", parameter.getName());
				Assert.assertNull(parameter.getResourceInformation());
				Assert.assertEquals(FilterOperator.EQ, parameter.getOperator());
				Assert.assertNull(parameter.getAttributePath());
				Assert.assertEquals(1, parameter.getValues().size());
				deserialized[0] = true;
			}
		};
		urlMapper.init(deserializerContext);
		urlMapper.deserialize(taskInformation, params);
		Assert.assertTrue(deserialized[0]);
	}


	@Test(expected = ParametersDeserializationException.class)
	public void testInvalidPagingType() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[doesNotExist]", "1");
		urlMapper.deserialize(taskInformation, params);
	}

	@Test(expected = ParametersDeserializationException.class)
	public void testPagingError() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.setLimit(2L);
		expectedSpec.setOffset(1L);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "notANumber");
		add(params, "page[limit]", "2");

		urlMapper.deserialize(taskInformation, params);
	}

	@Test
	public void testPagingMaxLimitNotAllowed() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "1");
		add(params, "page[limit]", "30");

		expectedException.expect(BadRequestException.class);

		urlMapper.deserialize(taskWithPagingBehaviorInformation, params);
	}

	@Test
	public void testPagingMaxLimitAllowed() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.setOffset(1L);
		expectedSpec.setLimit(5L);

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "page[offset]", "1");
		add(params, "page[limit]", "5");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeRelations() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeRelation(Arrays.asList("project"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "include[tasks]", "project");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeRelationsOnRoot() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeRelation(Arrays.asList("project"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "include", "project");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeAttributes() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeField(Arrays.asList("name"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "fields[tasks]", "name");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIncludeAttributesOnRoot() {
		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.includeField(Arrays.asList("name"));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "fields", "name");

		QuerySpec actualSpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testHyphenIsAllowedInResourceName() {

		QuerySpec expectedSpec = new QuerySpec(Task.class);
		expectedSpec.addSort(new SortSpec(Arrays.asList("id"), Direction.ASC));

		Map<String, Set<String>> params = new HashMap<>();
		add(params, "sort[task-with-lookup]", "id");

		ResourceInformation taskWithLookUpInformation =
				resourceRegistry.getEntryForClass(TaskWithLookup.class).getResourceInformation();
		QuerySpec actualSpec = urlMapper.deserialize(taskWithLookUpInformation, params);
		Assert.assertEquals(expectedSpec, actualSpec);
	}

	@Test
	public void testIngoreParseException() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[id]", "NotAnId");
		urlMapper.setIgnoreParseExceptions(true);
		QuerySpec querySpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(Task.class, querySpec.getResourceClass());
		Assert.assertEquals(Arrays.asList("id"), querySpec.getFilters().get(0).getAttributePath());
		Assert.assertEquals("NotAnId", querySpec.getFilters().get(0).getValue());
		Assert.assertNull(querySpec.getRelatedSpecs().get(Project.class));
	}

	@Test
	public void testGenericCast() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[id]", "12");
		add(params, "filter[name]", "test");
		add(params, "filter[completed]", "true");
		urlMapper.setIgnoreParseExceptions(false);
		QuerySpec querySpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(Task.class, querySpec.getResourceClass());
		Assert.assertEquals(Arrays.asList("id"), querySpec.getFilters().get(2).getAttributePath());
		Long id = querySpec.getFilters().get(2).getValue();
		Assert.assertEquals(Long.valueOf(12), id);
		String name = querySpec.getFilters().get(0).getValue();
		Assert.assertEquals("test", name);
		Boolean completed = querySpec.getFilters().get(1).getValue();
		Assert.assertEquals(Boolean.TRUE, completed);
		Assert.assertNull(querySpec.getRelatedSpecs().get(Project.class));
	}

	@Test(expected = ParametersDeserializationException.class)
	public void testFailOnParseException() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[id]", "NotAnId");
		urlMapper.setIgnoreParseExceptions(false);
		urlMapper.deserialize(taskInformation, params);
	}

	@Test(expected = ParametersDeserializationException.class)
	public void testUnknownProperty() {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "group", "test");
		urlMapper.setIgnoreParseExceptions(false);
		urlMapper.deserialize(taskInformation, params);
	}

	protected void add(Map<String, Set<String>> params, String key, String value) {
		params.put(key, new HashSet<>(Arrays.asList(value)));
	}
}
