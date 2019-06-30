package io.crnk.client;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuerySpecUnknownAttributeClientTest extends AbstractClientTest {

    protected ResourceRepository<Task, Long> taskRepo;

    protected ResourceRepository<Project, Long> projectRepo;

    protected RelationshipRepository<Task, Long, Project, Long> relRepo;

    private CrnkBoot boot;

    @Before
    public void setup() {
        super.setup();

        DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) client.getUrlMapper();
        urlMapper.setAllowUnknownAttributes(true);

        taskRepo = client.getRepositoryForType(Task.class);
        projectRepo = client.getRepositoryForType(Project.class);
        relRepo = client.getRepositoryForType(Task.class, Project.class);
    }

    @Override
    protected TestApplication configure() {
        TestApplication app = new TestApplication();
        boot = app.getFeature().getBoot();
        boot.setAllowUnknownAttributes();
        return app;
    }

    @Test
    public void testCall() {
        QuerySpec querySpec = new QuerySpec(Task.class);
        querySpec.addSort(new SortSpec(Arrays.asList("unknownAttr"), Direction.ASC));
        querySpec.addFilter(new FilterSpec(Arrays.asList("unknownAttr"), FilterOperator.EQ, "test"));
        List<Task> tasks = taskRepo.findAll(querySpec);
        Assert.assertEquals(0, tasks.size()); // no matches naturally...
    }

    @Test
    public void testDeserialize() {
        Map<String, Set<String>> parameterMap = new HashMap<>();
        parameterMap.put("filter[unknownAttr]", Collections.singleton("test"));
        parameterMap.put("sort", Collections.singleton("-unknownAttr"));
        ResourceInformation taskInformation = client.getRegistry().getEntryForClass(Task.class).getResourceInformation();
        QuerySpec querySpec = boot.getUrlMapper().deserialize(taskInformation, parameterMap);
        Assert.assertEquals(1, querySpec.getFilters().size());
        FilterSpec filterSpec = querySpec.getFilters().get(0);
        Assert.assertEquals("unknownAttr", filterSpec.getAttributePath().get(0));
        Assert.assertEquals(FilterOperator.EQ, filterSpec.getOperator());
        Assert.assertEquals("test", filterSpec.getValue());

        Assert.assertEquals(1, querySpec.getSort().size());
        SortSpec sortSpec = querySpec.getSort().get(0);
        Assert.assertEquals("unknownAttr", sortSpec.getAttributePath().get(0));
        Assert.assertEquals(Direction.DESC, sortSpec.getDirection());
    }

}