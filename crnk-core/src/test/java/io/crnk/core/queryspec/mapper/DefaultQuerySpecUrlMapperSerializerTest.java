package io.crnk.core.queryspec.mapper;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.internal.utils.JsonApiUrlBuilder;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.mock.models.CustomPagingPojo;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingBehavior;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DefaultQuerySpecUrlMapperSerializerTest {

    private JsonApiUrlBuilder urlBuilder;

    private ResourceRegistry resourceRegistry;

    private DefaultQuerySpecUrlMapper urlMapper;

    @Before
    public void setup() {
        CoreTestContainer container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.getBoot().getModuleRegistry().addPagingBehavior(new OffsetLimitPagingBehavior());
        container.getBoot().getModuleRegistry().addPagingBehavior(new NumberSizePagingBehavior());
        container.boot();

        urlMapper = (DefaultQuerySpecUrlMapper) container.getBoot().getUrlMapper();

        resourceRegistry = container.getResourceRegistry();
        urlBuilder = new JsonApiUrlBuilder(container.getModuleRegistry(), container.getQueryContext());
    }

    @Test
    public void dotSeparatorEnabledByDefault() {
        Assert.assertTrue(urlMapper.getEnforceDotPathSeparator());
    }

    @Test
    public void testHttpsSchema() {
        CoreTestContainer container = new CoreTestContainer();
        container.getBoot().setServiceUrlProvider(new ConstantServiceUrlProvider("https://127.0.0.1"));
        container.addModule(new CoreTestModule());
        container.getBoot().getModuleRegistry().addPagingBehavior(new OffsetLimitPagingBehavior());
        container.boot();

        urlBuilder = new JsonApiUrlBuilder(container.getModuleRegistry(), container.getQueryContext());
        check("https://127.0.0.1/tasks", null, new QuerySpec(Task.class));
    }

    @Test
    public void testPort() {
        CoreTestContainer container = new CoreTestContainer();
        container.getBoot().setServiceUrlProvider(new ConstantServiceUrlProvider("https://127.0.0.1:1234"));
        container.addModule(new CoreTestModule());
        container.getBoot().getModuleRegistry().addPagingBehavior(new OffsetLimitPagingBehavior());
        container.boot();

        urlBuilder = new JsonApiUrlBuilder(container.getModuleRegistry(), container.getQueryContext());
        check("https://127.0.0.1:1234/tasks", null, new QuerySpec(Task.class));
    }

    @Test(expected = RepositoryNotFoundException.class)
    public void unknownResourceShouldThrowException() {
        RegistryEntry entry = resourceRegistry.getEntry(Task.class);
        Class<?> notAResourceClass = String.class;
        urlBuilder.buildUrl(entry.getResourceInformation(), null, new QuerySpec(notAResourceClass));
    }

    @Test
    public void testFindAll() {
        check("http://127.0.0.1/tasks", null, new QuerySpec(Task.class));
    }

    @Test
    public void testFilterNonRootType() {
        QuerySpec projectQuerySpec = new QuerySpec(Project.class);
        projectQuerySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "test"));
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.putRelatedSpec(Project.class, projectQuerySpec);
        check("http://127.0.0.1/tasks?filter[projects][name]=test", null, querySpec);
    }

    @Test
    public void testFindById() {
        check("http://127.0.0.1/tasks/1", 1, new QuerySpec(Task.class));
    }

    @Test
    public void testFindByIds() {
        check("http://127.0.0.1/tasks/1,2,3", Arrays.asList(1, 2, 3), new QuerySpec(Task.class));
    }

    @Test
    public void testFindAllOrderByAsc() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
        check("http://127.0.0.1/tasks?sort=name", null, querySpec);
    }

    @Test
    public void testFindAllOrderMultipleFields() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
        querySpec.addSort(new SortSpec(Arrays.asList("id"), Direction.DESC));
        check("http://127.0.0.1/tasks?sort=name%2C-id", null, querySpec);
    }

    @Test
    public void testFindAllIncludeMultipleFields() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeField(Arrays.asList("name"));
        querySpec.includeField(Arrays.asList("id"));
        check("http://127.0.0.1/tasks?fields=name%2Cid", null, querySpec);
    }

    @Test
    public void testFindAllIncludeMultipleRelations() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeRelation(Arrays.asList("project"));
        querySpec.includeRelation(Arrays.asList("projects"));
        check("http://127.0.0.1/tasks?include=project%2Cprojects", null, querySpec);
    }

    @Test
    public void testFindAllOrderByDesc() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.DESC));
        check("http://127.0.0.1/tasks?sort=-name", null, querySpec);
    }

    @Test
    public void testFilterByOne() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, "value"));
        check("http://127.0.0.1/tasks?filter[name]=value", null, querySpec);
    }

    @Test
    public void testFilterWithLikeUsesStrings() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("deleted"), FilterOperator.LIKE, "value"));
        check("http://127.0.0.1/tasks?filter[deleted][LIKE]=value", null, querySpec);
    }

    @Test
    public void testFilterWithCommaSeparation() {
        Assert.assertTrue(urlMapper.getAllowCommaSeparatedValue());

        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.LIKE, Arrays.asList("john", "jane")));
        check("http://127.0.0.1/tasks?filter[name][LIKE]=jane%2Cjohn", null, querySpec);
    }

    @Test
    public void testFilterWithoutCommaSeparation() {
        urlMapper.setAllowCommaSeparatedValue(false);

        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, Arrays.asList("john", "jane")));
        check("http://127.0.0.1/tasks?filter[name]=john&filter[name]=jane", null, querySpec);
    }

    @Test
    public void testFilterByPath() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("project", "name"), FilterOperator.EQ, "value"));
        check("http://127.0.0.1/tasks?filter[project.name]=value", null, querySpec);
    }

    @Test
    public void testFilterByNull() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("name"), FilterOperator.EQ, null));
        check("http://127.0.0.1/tasks?filter[name]=null", null, querySpec);
    }

    @Test
    public void testFilterEquals() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, 1));
        check("http://127.0.0.1/tasks?filter[id]=1", null, querySpec);
    }

    @Test
    public void testFilterWithJson() throws UnsupportedEncodingException {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec("{id: 1}"));
        check("http://127.0.0.1/tasks?filter=" + URLEncoder.encode("{id: 1}", StandardCharsets.UTF_8.toString()), null, querySpec);
    }

    @Test
    public void testFilterGreater() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.LE, 1));
        check("http://127.0.0.1/tasks?filter[id][LE]=1", null, querySpec);
    }

    //
    @Test
    public void testPaging() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.setLimit(2L);
        querySpec.setOffset(1L);
        check("http://127.0.0.1/tasks?page[limit]=2&page[offset]=1", null, querySpec);
    }

    @Test
    public void testPagingOnRelation() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.setLimit(2L);
        querySpec.setOffset(1L);

        RegistryEntry entry = resourceRegistry.getEntry(Task.class);
        String actualUrl = urlBuilder.buildUrl(entry.getResourceInformation(), 1L, querySpec, "projects");
        assertEquals("http://127.0.0.1/tasks/1/relationships/projects?page[limit]=2&page[offset]=1", actualUrl);
    }

    @Test
    public void testIncludeRelations() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeRelation(Arrays.asList("project"));
        check("http://127.0.0.1/tasks?include=project", null, querySpec);
    }

    @Test
    public void testIncludeRelationsOfDifferentTypes() {
        QuerySpec querySpec = new QuerySpec(CustomPagingPojo.class);
        querySpec.includeRelation(Arrays.asList("task"));
        check("http://127.0.0.1/custom-paging?include=task", null, querySpec);
    }

    @Test
    public void testIncludeAttributes() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.includeField(Arrays.asList("name"));
        check("http://127.0.0.1/tasks?fields=name", null, querySpec);
    }

    @Test
    public void mapJsonToJavaNames() {
        QuerySpec querySpec = new QuerySpec(Schedule.class);
        querySpec.includeField(Arrays.asList("desc"));
        querySpec.includeRelation(Arrays.asList("followupProject"));
        querySpec.addSort(new SortSpec(Arrays.asList("desc"), Direction.ASC));
        querySpec.addFilter(new FilterSpec(Arrays.asList("desc"), FilterOperator.EQ, "test"));
        check("http://127.0.0.1/schedules?include=followup&filter[description]=test&sort"
                        + "=description&fields=description",
                null, querySpec);
    }

    private void check(String expectedUrl, Object id, QuerySpec querySpec) {
        RegistryEntry entry = resourceRegistry.getEntry(querySpec.getResourceClass());
        String actualUrl = urlBuilder.buildUrl(entry.getResourceInformation(), id, querySpec);
        assertEquals(expectedUrl, actualUrl);
    }
}
