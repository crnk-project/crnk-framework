package io.crnk.core.resource.paging.next;

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
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.meta.HasMoreResourcesMetaInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HasNextBasedPagedLinksInformationTest extends AbstractQuerySpecTest {

	private ResourceRepositoryAdapter adapter;

	@Before
	public void setup() {
		HasNextPageTestRepository.clear();

		super.setup();
		RegistryEntry registryEntry = resourceRegistry.getEntry(HasNextPageResource.class);
		HasNextPageTestRepository repo = (HasNextPageTestRepository) registryEntry.getResourceRepository()
				.getImplementation();

		repo = Mockito.spy(repo);

		adapter = registryEntry.getResourceRepository();

		QueryAdapter queryAdapter = container.toQueryAdapter(querySpec());
		for (long i = 0; i < 5; i++) {
			HasNextPageResource task = new HasNextPageResource();
			task.setId(i);
			task.setName("myHasNextPageResource");
			adapter.create(task, queryAdapter);
		}
	}

	@Override
	protected void setup(CoreTestContainer container) {
		SimpleModule module = new SimpleModule("next");
		module.addRepository(new HasNextPageTestRepository());
		container.addModule(module);
	}

	@Test
	public void testPaging() {
		QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(2L, 2L));

		JsonApiResponse results = adapter.findAll(querySpec).get();

		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertTrue(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2", linksInformation.getFirst().getHref());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2", linksInformation.getPrev().getHref());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=2&page[offset]=4", linksInformation.getNext().getHref());
	}

	@Test
	public void testPagingNoContents() {
		HasNextPageTestRepository.clear();

		QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(0L, 2L));

		JsonApiResponse results = adapter.findAll(querySpec).get();
		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertFalse(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertNull(linksInformation.getFirst());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertNull(linksInformation.getNext());
	}

	@Test
	public void testPagingFirst() {
		QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(0L, 3L));

		JsonApiResponse results = adapter.findAll(querySpec).get();

		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertTrue(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3", linksInformation.getFirst().getHref());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertNull(linksInformation.getPrev());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=3&page[offset]=3", linksInformation.getNext().getHref());
	}

	@Test
	public void testPagingLast() {
		QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(4L, 4L));

		JsonApiResponse results = adapter.findAll(querySpec).get();

		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertFalse(metaInformation.getHasMoreResources());

		PagedLinksInformation linksInformation = (PagedLinksInformation) results.getLinksInformation();
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4", linksInformation.getFirst().getHref());
		Assert.assertNull(linksInformation.getLast());
		Assert.assertEquals("http://127.0.0.1/tasks?page[limit]=4", linksInformation.getFirst().getHref());
		Assert.assertNull(linksInformation.getNext());
	}

	@Test
	public void testNoPaging() {
		QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec());
		JsonApiResponse results = adapter.findAll(querySpec).get();

		HasMoreResourcesMetaInformation metaInformation = (HasMoreResourcesMetaInformation) results.getMetaInformation();
		Assert.assertNull(metaInformation.getHasMoreResources());

		LinksInformation linksInformation = results.getLinksInformation();
		Assert.assertFalse(linksInformation instanceof PagedLinksInformation);
	}

	@Test(expected = BadRequestException.class)
	public void testInvalidPaging() {
		QuerySpecAdapter querySpec = container.toQueryAdapter(querySpec(1L, 3L));
		adapter.findAll(querySpec).get().getLinksInformation();
	}

	@Override
	protected QuerySpec querySpec(Long offset, Long limit) {
		QuerySpec querySpec = new QuerySpec(HasNextPageResource.class);
		querySpec.setPaging(new OffsetLimitPagingSpec(offset, limit));
		return querySpec;
	}
}
