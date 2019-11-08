package io.crnk.core.queryspec.mapper;

import io.crnk.core.exception.BadRequestException;
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

public class DefaultQuerySpecUrlMapperDeserializerTest extends DefaultQuerySpecUrlMapperDeserializerTestBase {


    @Before
    public void setup() {
        super.setup();
        urlMapper.setEnforceDotPathSeparator(true);
        Assert.assertTrue(urlMapper.getEnforceDotPathSeparator());
    }

    @Test(expected = ParametersDeserializationException.class)
    public void testDotNotationDisallowsBrackets() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[projects][tasks][name]", "test");
        urlMapper.deserialize(taskInformation, params);
    }

    @Test
    public void testCannotFilterNonFilterableAttribute() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[deleted]", "true");
        try {
            urlMapper.deserialize(taskInformation, params);
            Assert.fail();
        } catch (BadRequestException e) {
            Assert.assertEquals("path [deleted] is not filterable", e.getMessage());
        }
    }

    @Test
    public void testCannotSortNonSortableAttribute() {
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "sort", "deleted");
        try {
            urlMapper.deserialize(taskInformation, params);
            Assert.fail();
        } catch (BadRequestException e) {
            Assert.assertEquals("path [deleted] is not sortable", e.getMessage());
        }
    }


    @Test
    public void testNoAmbiguityForType() {
        Map<String, Set<String>> params = new HashMap<>();
        // note that there is both a type and an attribute on tasks called
        // projects
        add(params, "filter[projects][name]", "test");
        QuerySpec querySpec = urlMapper.deserialize(taskInformation, params);
        Assert.assertEquals(Task.class, querySpec.getResourceClass());
        Assert.assertEquals(0, querySpec.getFilters().size());
        QuerySpec projectQuerySpec = querySpec.getQuerySpec(Project.class);
        Assert.assertEquals(1, projectQuerySpec.getFilters().size());
        Assert.assertEquals(Arrays.asList("name"), projectQuerySpec.getFilters().get(0).getAttributePath());
    }

    @Test
    public void testNoAmbiguityForAttribute() {
        // note that there is both a type and an attribute on tasks called
        // projects, here the attribute should match
        Map<String, Set<String>> params = new HashMap<>();
        add(params, "filter[projects]", "someValue");
        urlMapper.setIgnoreParseExceptions(true);
        QuerySpec querySpec = urlMapper.deserialize(taskInformation, params);
        Assert.assertEquals(Task.class, querySpec.getResourceClass());
        Assert.assertEquals(Arrays.asList("projects"), querySpec.getFilters().get(0).getAttributePath());
        Assert.assertNull(querySpec.getQuerySpec(Project.class));
    }
}
