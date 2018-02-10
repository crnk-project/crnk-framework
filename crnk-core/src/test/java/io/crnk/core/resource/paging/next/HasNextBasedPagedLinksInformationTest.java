package io.crnk.core.resource.paging.next;

import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.AbstractQuerySpecTest;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HasNextBasedPagedLinksInformationTest extends AbstractQuerySpecTest {

	private ResourceRepositoryAdapter<Task, Long> adapter;

	@Before
	public void setup() {
		HasNextPageTestRepository.clear();

		super.setup();
		RegistryEntry registryEntry = resourceRegistry.getEntry(Task.class);
		HasNextPageTestRepository repo = (HasNextPageTestRepository) registryEntry.getResourceRepository(null)
				.getResourceRepository();

		repo = Mockito.spy(repo);

		adapter = registryEntry.getResourceRepository(null);

		QueryAdapter queryAdapter = new QuerySpecAdapter(querySpec(), resourceRegistry);
		for (long i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(i);
			task.setName("myTask");
			adapter.create(task, queryAdapter);
		}

	}

	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(querySpec(2L, 2L), resourceRegistry);

		JsonApiResponse results = adapter.findAll(querySpec);

		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertTrue(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2", linksInformation.getFirst());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2", linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2&page[offset]=4", linksInformation.getNext());
	}

	@Test
	public void testPagingNoContents() throws InstantiationException, IllegalAccessException {
		HasNextPageTestRepository.clear();

		QuerySpecAdapter querySpec = new QuerySpecAdapter(querySpec(0L, 2L), resourceRegistry);

		JsonApiResponse results = adapter.findAll(querySpec);
		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertFalse(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertNull(linksInformation.getFirst());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertNull(linksInformation.getNext());
	}

	@Test
	public void testPagingFirst() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(querySpec(0L, 3L), resourceRegistry);

		JsonApiResponse results = adapter.findAll(querySpec);

		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertTrue(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3", linksInformation.getFirst());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3&page[offset]=3", linksInformation.getNext());
	}

	@Test
	public void testPagingLast() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(querySpec(4L, 4L), resourceRegistry);

		JsonApiResponse results = adapter.findAll(querySpec);

		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertFalse(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4", linksInformation.getFirst());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4", linksInformation.getFirst());
		Assert.assertNull(linksInformation.getNext());
	}

	@Test
	public void testNoPaging() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(querySpec(), resourceRegistry);
		JsonApiResponse results = adapter.findAll(querySpec);

		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertNull(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertNull(linksInformation);
	}

	@Test(expected = BadRequestException.class)
	public void testInvalidPaging() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(querySpec(1L, 3L), resourceRegistry);
		adapter.findAll(querySpec).getLinksInformation();
	}
}
