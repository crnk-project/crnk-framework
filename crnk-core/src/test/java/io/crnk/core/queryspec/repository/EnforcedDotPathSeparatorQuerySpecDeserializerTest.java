package io.crnk.core.queryspec.repository;

import io.crnk.core.exception.ParametersDeserializationException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnforcedDotPathSeparatorQuerySpecDeserializerTest extends DefaultQuerySpecDeserializerTestBase {

	@Before
	public void setup() {
		super.setup();
		deserializer.setEnforceDotPathSeparator(true);
	}

	@Test(expected = ParametersDeserializationException.class)
	public void testDotNotationDisallowsBrackets() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<>();
		add(params, "filter[projects][tasks][name]", "test");
		deserializer.deserialize(taskInformation, params);
	}

	@Test
	public void testNoAmbiguityForType() throws InstantiationException, IllegalAccessException {
		Map<String, Set<String>> params = new HashMap<>();
		// note that there is both a type and an attribute on tasks called
		// projects
		add(params, "filter[projects][name]", "test");
		QuerySpec querySpec = deserializer.deserialize(taskInformation, params);
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
		deserializer.setIgnoreParseExceptions(true);
		QuerySpec querySpec = deserializer.deserialize(taskInformation, params);
		Assert.assertEquals(Task.class, querySpec.getResourceClass());
		Assert.assertEquals(Arrays.asList("projects"), querySpec.getFilters().get(0).getAttributePath());
		Assert.assertNull(querySpec.getRelatedSpecs().get(Project.class));
	}
}
