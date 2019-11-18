package io.crnk.core.queryspec.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.internal.DefaultQueryPathResolver;
import io.crnk.core.queryspec.internal.JsonFilterSpecMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class JsonFilterSpecMapperTest {

    private ResourceInformation resourceInformation;

    private JsonFilterSpecMapper mapper;

    private ObjectMapper objectMapper;

    private QueryContext queryContext = new QueryContext().setRequestVersion(0);

    @Before
    public void setup() {
        CoreTestContainer container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.boot();

        QueryPathResolver pathResolver = new DefaultQueryPathResolver();

        resourceInformation = container.getEntry(Task.class).getResourceInformation();

        HashMap<String, FilterOperator> supportedOperators = new HashMap<>();
        supportedOperators.put("GE", FilterOperator.GE);
        supportedOperators.put("LE", FilterOperator.LE);
        supportedOperators.put("EQ", FilterOperator.EQ);
        supportedOperators.put("AND", FilterOperator.AND);
        supportedOperators.put("OR", FilterOperator.OR);
        supportedOperators.put("NOT", FilterOperator.NOT);
        supportedOperators.put("NEQ", FilterOperator.NEQ);

        QuerySpecUrlContext urlContext = Mockito.mock(QuerySpecUrlContext.class);
        Mockito.when(urlContext.getObjectMapper()).thenReturn(container.getObjectMapper());
        Mockito.when(urlContext.getResourceRegistry()).thenReturn(container.getResourceRegistry());

        pathResolver.init(urlContext);

        objectMapper = container.getObjectMapper();
        mapper = new JsonFilterSpecMapper(urlContext, supportedOperators, FilterOperator.EQ, pathResolver);
    }

    @Test
    public void filterByString() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"test\"}");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(1, filterSpecs.size());
        FilterSpec filterSpec = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("name"), filterSpec.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec.getOperator());
        Assert.assertEquals("test", filterSpec.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }

    @Test
    public void filterByBoolean() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"completed\": true}");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(1, filterSpecs.size());
        FilterSpec filterSpec = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("completed"), filterSpec.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec.getOperator());
        Assert.assertEquals(Boolean.TRUE, filterSpec.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }

    @Test
    public void filterByLong() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"id\": 12}");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(1, filterSpecs.size());
        FilterSpec filterSpec = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("id"), filterSpec.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec.getOperator());
        Assert.assertEquals((Long) 12L, filterSpec.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }

    @Test
    public void filterByLongArray() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"id\": [12, 13, 14]}");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(1, filterSpecs.size());
        FilterSpec filterSpec = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("id"), filterSpec.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec.getOperator());
        Assert.assertEquals(Arrays.asList(12L, 13L, 14L), filterSpec.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }

    @Test
    public void filterNotEquals() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"NEQ\": {\"id\": [12, 13, 14]}}");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(1, filterSpecs.size());
        FilterSpec filterSpec = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("id"), filterSpec.getPath());
        Assert.assertEquals(FilterOperator.NEQ, filterSpec.getOperator());
        Assert.assertEquals(Arrays.asList(12L, 13L, 14L), filterSpec.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }

    @Test
    public void filterNEQ() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"NEQ\": {\"id\": [12, 13, 14]}}");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(1, filterSpecs.size());
        FilterSpec filterSpec = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("id"), filterSpec.getPath());
        Assert.assertEquals(FilterOperator.NEQ, filterSpec.getOperator());
        Assert.assertEquals(Arrays.asList(12L, 13L, 14L), filterSpec.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }


    @Test
    public void filterOr() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{ \"OR\": [ {\"id\": [12, 13, 14]}, {\"completed\": true} ] }");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(1, filterSpecs.size());
        FilterSpec andFilter = filterSpecs.get(0);
        Assert.assertEquals(FilterOperator.OR, andFilter.getOperator());
        Assert.assertEquals(2, andFilter.getExpression().size());

        FilterSpec filterSpec1 = andFilter.getExpression().get(0);
        Assert.assertEquals(PathSpec.of("id"), filterSpec1.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec1.getOperator());
        Assert.assertEquals(Arrays.asList(12L, 13L, 14L), filterSpec1.getValue());

        FilterSpec filterSpec2 = andFilter.getExpression().get(1);
        Assert.assertEquals(PathSpec.of("completed"), filterSpec2.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec2.getOperator());
        Assert.assertEquals(Boolean.TRUE, filterSpec2.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertTrue(mapper.isNested(filterSpecs));
    }

    @Test
    public void filterMultipleAttributes() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"id\": [12, 13, 14], \"completed\": true }");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(2, filterSpecs.size());
        FilterSpec filterSpec1 = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("id"), filterSpec1.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec1.getOperator());
        Assert.assertEquals(Arrays.asList(12L, 13L, 14L), filterSpec1.getValue());

        FilterSpec filterSpec2 = filterSpecs.get(1);
        Assert.assertEquals(PathSpec.of("completed"), filterSpec2.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec2.getOperator());
        Assert.assertEquals(Boolean.TRUE, filterSpec2.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }

    @Test
    public void filterByNestedAttributes() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"project\": {\"id\": 1} }");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(1, filterSpecs.size());
        FilterSpec filterSpec1 = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("project", "id"), filterSpec1.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec1.getOperator());
        Assert.assertEquals((Long) 1L, filterSpec1.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }

    @Test
    public void filterMixed() throws IOException {
        JsonNode jsonNode = objectMapper.readTree("{\"name\": \"Great Task\", \"id\": 121, \"NEQ\": {\"id\": 122}}");
        List<FilterSpec> filterSpecs = mapper.deserialize(jsonNode, resourceInformation, queryContext);
        Assert.assertEquals(3, filterSpecs.size());
        FilterSpec filterSpec1 = filterSpecs.get(0);
        Assert.assertEquals(PathSpec.of("name"), filterSpec1.getPath());
        Assert.assertEquals(FilterOperator.EQ, filterSpec1.getOperator());
        Assert.assertEquals("Great Task", filterSpec1.getValue());

        FilterSpec filterSpec2 = filterSpecs.get(2);
        Assert.assertEquals(PathSpec.of("id"), filterSpec2.getPath());
        Assert.assertEquals(FilterOperator.NEQ, filterSpec2.getOperator());
        Assert.assertEquals((Long) 122L, filterSpec2.getValue());

        JsonNode serializedJsonNode = mapper.serialize(filterSpecs);
        checkNodeEquals(jsonNode, serializedJsonNode);
        Assert.assertFalse(mapper.isNested(filterSpecs));
    }

    private void checkNodeEquals(JsonNode expected, JsonNode actual) throws JsonProcessingException {
        String strExpected = objectMapper.writeValueAsString(expected);
        String strActual = objectMapper.writeValueAsString(actual);
        Assert.assertEquals(strExpected, strActual);
    }


}
