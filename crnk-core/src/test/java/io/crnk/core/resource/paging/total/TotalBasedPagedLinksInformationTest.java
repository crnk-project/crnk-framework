package io.crnk.core.resource.paging.total;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.AbstractQuerySpecTest;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingSpec;
import io.crnk.core.resource.links.PagedLinksInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TotalBasedPagedLinksInformationTest extends AbstractQuerySpecTest {

    private ResourceRepositoryAdapter adapter;

    private TotalResourceCountTestRepository repository;

    @Before
    public void setup() {
        super.setup();
        RegistryEntry registryEntry = resourceRegistry.findEntry(TotalResourceCountResource.class);

        adapter = registryEntry.getResourceRepository();

        QueryAdapter queryAdapter = container.toQueryAdapter(querySpec());
        for (long i = 0; i < 5; i++) {
            TotalResourceCountResource resource = new TotalResourceCountResource();
            resource.setId(i);
            resource.setName("myTask");
            adapter.create(resource, queryAdapter);
        }

    }

    @Override
    protected void setup(CoreTestContainer container) {
        SimpleModule module = new SimpleModule("total");
        repository = new TotalResourceCountTestRepository();

        module.addRepository(repository);
        container.addModule(module);
    }

    @Test
    public void testPaging() {
        QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(2L, 2L));

        PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).get().getLinksInformation();
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2", linksInformation.getFirst());
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2&page[offset]=4", linksInformation.getLast());
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2", linksInformation.getPrev());
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2&page[offset]=4", linksInformation.getNext());
    }

    @Test
    public void testPagingNoContents() {
        repository.clear();
        QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(0L, 2L));

        PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).get().getLinksInformation();
        Assert.assertNull(linksInformation.getFirst());
        Assert.assertNull(linksInformation.getLast());
        Assert.assertNull(linksInformation.getPrev());
        Assert.assertNull(linksInformation.getNext());
    }

    @Test
    public void testPagingFirst() {
        QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(0L, 3L));

        PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).get().getLinksInformation();
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3", linksInformation.getFirst());
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3&page[offset]=3", linksInformation.getLast());
        Assert.assertNull(linksInformation.getPrev());
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3&page[offset]=3", linksInformation.getNext());
    }

    @Test
    public void testPagingLast() {
        QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(4L, 4L));

        PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).get().getLinksInformation();
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4", linksInformation.getFirst());
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4&page[offset]=4", linksInformation.getLast());
        Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4", linksInformation.getFirst());
        Assert.assertNull(linksInformation.getNext());
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidPaging() {
        QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(1L, 3L));
        adapter.findAll(querySpec).get().getLinksInformation();
    }

    @Override
    protected QuerySpec querySpec(Long offset, Long limit) {
        QuerySpec querySpec = new QuerySpec(TotalResourceCountResource.class);
        querySpec.setPagingSpec(new OffsetLimitPagingSpec(offset, limit));
        return querySpec;
    }
}
