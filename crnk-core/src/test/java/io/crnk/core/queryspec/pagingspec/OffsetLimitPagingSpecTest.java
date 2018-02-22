package io.crnk.core.queryspec.pagingspec;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.registry.DefaultResourceRegistryPart;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.resource.links.DefaultPagedLinksInformation;
import io.crnk.core.resource.links.PagedLinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.DefaultPagedMetaInformation;

import org.junit.Test;

public class OffsetLimitPagingSpecTest {

	@Test
	public void testIsRequired() {
		assertTrue(new OffsetLimitPagingSpec(1L, null).isRequired());
		assertTrue(new OffsetLimitPagingSpec(0L, 30L).isRequired());
	}

	@Test
	public void testIsNotRequired() {
		assertFalse(new OffsetLimitPagingSpec().isRequired());
	}

	@Test
	public void testBuild() {
		OffsetLimitPagingSpec pagingSpec = new OffsetLimitPagingSpec(0L, 10L);

		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceRegistry resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);
		QuerySpec spec = new QuerySpec(Task.class);
		QuerySpecAdapter querySpecAdapter = new QuerySpecAdapter(spec, resourceRegistry);
		querySpecAdapter.setPagingSpec(pagingSpec);

		PagingSpecUrlBuilder urlBuilder = mock(PagingSpecUrlBuilder.class);
		when(urlBuilder.build(any(QuerySpecAdapter.class))).thenReturn("http://some.org");

		DefaultPagedMetaInformation pagedMetaInformation = new DefaultPagedMetaInformation();
		pagedMetaInformation.setTotalResourceCount(30L);
		ResourceList resourceList = new DefaultResourceList(pagedMetaInformation, null);
		for (int i = 0; i < 30; i++) {
			resourceList.add(new Task());
		}

		PagedLinksInformation pagedLinksInformation = new DefaultPagedLinksInformation();
		pagingSpec.build(pagedLinksInformation, resourceList, querySpecAdapter, urlBuilder);

		assertThat(pagedLinksInformation.getFirst(), equalTo("http://some.org"));
		assertThat(pagedLinksInformation.getNext(), equalTo("http://some.org"));
		assertNull(pagedLinksInformation.getPrev());
		assertThat(pagedLinksInformation.getLast(), equalTo("http://some.org"));
	}
}
