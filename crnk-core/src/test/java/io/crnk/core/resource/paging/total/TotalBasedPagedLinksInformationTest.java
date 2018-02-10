package io.crnk.core.resource.paging.total;

import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.AbstractQuerySpecTest;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.resource.links.PagedLinksInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TotalBasedPagedLinksInformationTest extends AbstractQuerySpecTest {

	private ResourceRepositoryAdapter<Task, Long> adapter;

	@Before
	public void setup() {
		TotalResourceCountTestRepository.clear();

		super.setup();
		RegistryEntry registryEntry = resourceRegistry.findEntry(Task.class);
		TotalResourceCountTestRepository repo = (TotalResourceCountTestRepository) registryEntry.getResourceRepository(null)
				.getResourceRepository();

		adapter = registryEntry.getResourceRepository(null);

		QueryAdapter queryAdapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		for (long i = 0; i < 5; i++) {
			Task task = new Task();
			task.setId(i);
			task.setName("myTask");
			adapter.create(task, queryAdapter);
		}

	}

	@Test
	public void testPaging() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		//todo undo / victor
//		querySpec.setOffset(2L);
//		querySpec.setLimit(2L);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2&page[offset]=4", linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2", linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2&page[offset]=4", linksInformation.getNext());
	}

	@Test
	public void testPagingNoContents() throws InstantiationException, IllegalAccessException {
		TotalResourceCountTestRepository.clear();

		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		//todo undo / victor
//		querySpec.setOffset(0L);
//		querySpec.setLimit(2L);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertNull(linksInformation.getFirst());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertNull(linksInformation.getNext());
	}

	@Test
	public void testPagingFirst() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		//todo undo / victor
//		querySpec.setOffset(0L);
//		querySpec.setLimit(3L);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3&page[offset]=3", linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3&page[offset]=3", linksInformation.getNext());
	}

	@Test
	public void testPagingLast() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		//todo undo / victor
//		querySpec.setOffset(4L);
//		querySpec.setLimit(4L);

		PagedLinksInformation linksInformation = (PagedLinksInformation) adapter.findAll(querySpec).getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4", linksInformation.getFirst());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4&page[offset]=4", linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4", linksInformation.getFirst());
		Assert.assertNull(linksInformation.getNext());
	}

	@Test(expected = BadRequestException.class)
	public void testInvalidPaging() throws InstantiationException, IllegalAccessException {
		QuerySpecAdapter querySpec = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry);
		//todo undo / victor
//		querySpec.setOffset(1L);
//		querySpec.setLimit(3L);
		adapter.findAll(querySpec).getLinksInformation();
	}
}
