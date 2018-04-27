package io.crnk.core.queryspec.mapper;

import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.CustomOffsetLimitPagingBehavior;
import io.crnk.core.queryspec.pagingspec.PagingBehavior;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class EnforcedDotPathSeparatorQuerySpecUrlMapperDeserializerTest extends DefaultQuerySpecUrlMapperDeserializerTestBase {

	@Override
	protected List<PagingBehavior> additionalPagingBehaviors() {
		return new ArrayList<>(Arrays.asList(new CustomOffsetLimitPagingBehavior()));
	}

	@Before
	public void setup() {
		super.setup();
		urlMapper.setEnforceDotPathSeparator(true);
		Assert.assertTrue(urlMapper.getEnforceDotPathSeparator());
	}

	@Test(expected = ParametersDeserializationException.class)
	public void testDotNotationDisallowsBrackets() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[projects][tasks][name]", "test");
		urlMapper.deserialize(taskInformation, params);
	}

	@Test
	public void testNoAmbiguityForType() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<>();
		// note that there is both a type and an attribute on tasks called
		// projects
		add(params, "filter[projects][name]", "test");
		QuerySpec querySpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(Task.class, querySpec.getResourceClass());
		Assert.assertEquals(0, querySpec.getFilters().size());
		QuerySpec projectQuerySpec = querySpec.getRelatedSpecs().get(Project.class);
		Assert.assertEquals(1, projectQuerySpec.getFilters().size());
		Assert.assertEquals(Arrays.asList("name"), projectQuerySpec.getFilters().get(0).getAttributePath());
	}

	@Test
	public void testNoAmbiguityForAttribute() throws InstantiationException, IllegalAccessException {
		// note that there is both a type and an attribute on tasks called
		// projects, here the attribute should match
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[projects]", "someValue");
		urlMapper.setIgnoreParseExceptions(true);
		QuerySpec querySpec = urlMapper.deserialize(taskInformation, params);
		Assert.assertEquals(Task.class, querySpec.getResourceClass());
		Assert.assertEquals(Arrays.asList("projects"), querySpec.getFilters().get(0).getAttributePath());
		Assert.assertNull(querySpec.getRelatedSpecs().get(Project.class));
	}
}
