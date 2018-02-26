package io.crnk.core.queryspec.pagingspec;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class OffsetLimitPagingBehaviorTest {

	@Test
	public void testSerializeDefault() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		Map<String, Set<String>> result = pagingBehavior.serialize(new OffsetLimitPagingSpec(), "tasks");

		assertFalse(result.containsKey("page[offset]"));
		assertFalse(result.containsKey("page[limit]"));
		assertEquals(0, result.size());
	}

	@Test
	public void testSerializeOffset() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		Map<String, Set<String>> result = pagingBehavior.serialize(new OffsetLimitPagingSpec(1L, null), "tasks");

		assertEquals(ImmutableSet.of("1"), result.get("page[offset]"));
		assertFalse(result.containsKey("page[limit]"));
		assertEquals(1, result.size());
	}

	@Test
	public void testSerializeLimit() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		Map<String, Set<String>> result = pagingBehavior.serialize(new OffsetLimitPagingSpec(0L, 30L), "tasks");

		assertFalse(result.containsKey("page[offset]"));
		assertEquals(ImmutableSet.of("30"), result.get("page[limit]"));
		assertEquals(1, result.size());
	}

	@Test
	public void testSerialize() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		Map<String, Set<String>> result = pagingBehavior.serialize(new OffsetLimitPagingSpec(1L, 30L), "tasks");

		assertEquals(ImmutableSet.of("1"), result.get("page[offset]"));
		assertEquals(ImmutableSet.of("30"), result.get("page[limit]"));
		assertEquals(2, result.size());
	}

	@Test
	public void testDeserializeDefaultWithNoParameters() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(Collections.<String, Set<String>>emptyMap());

		assertEquals(new OffsetLimitPagingSpec(0L, null), result);
	}

	@Test
	public void testDeserializeDefaultWithOffset() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("offset", ImmutableSet.of("1")));

		assertEquals(new OffsetLimitPagingSpec(1L, null), result);
	}

	@Test
	public void testDeserializeDefaultWithLimit() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("limit", ImmutableSet.of("30")));

		assertEquals(new OffsetLimitPagingSpec(0L, 30L), result);
	}

	@Test
	public void testDeserializeOffsetWithNoParameters() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		pagingBehavior.setDefaultOffset(1L);
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(Collections.<String, Set<String>>emptyMap());

		assertEquals(new OffsetLimitPagingSpec(1L, null), result);
	}

	@Test
	public void testDeserializeOffsetWithOffset() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		pagingBehavior.setDefaultOffset(1L);
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("offset", ImmutableSet.of("1")));

		assertEquals(new OffsetLimitPagingSpec(1L, null), result);
	}

	@Test
	public void testDeserializeOffsetWithLimit() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		pagingBehavior.setDefaultOffset(1L);
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("limit", ImmutableSet.of("30")));

		assertEquals(new OffsetLimitPagingSpec(1L, 30L), result);
	}

	@Test
	public void testDeserializeLimitWithNoParameters() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		pagingBehavior.setDefaultLimit(30L);
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(Collections.<String, Set<String>>emptyMap());

		assertEquals(new OffsetLimitPagingSpec(0L, 30L), result);
	}

	@Test
	public void testDeserializeLimitWithOffset() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		pagingBehavior.setDefaultLimit(30L);
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("offset", ImmutableSet.of("1")));

		assertEquals(new OffsetLimitPagingSpec(1L, 30L), result);
	}

	@Test
	public void testDeserializeLimitWithLimit() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		pagingBehavior.setDefaultLimit(30L);
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("limit", ImmutableSet.of("10")));

		assertEquals(new OffsetLimitPagingSpec(0L, 10L), result);
	}

	@Test
	public void testDeserialize() {
		OffsetLimitPagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		OffsetLimitPagingSpec result = pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("offset", ImmutableSet.of("1"),
				"limit", ImmutableSet.of("30")));

		assertEquals(new OffsetLimitPagingSpec(1L, 30L), result);
	}

	@Test
	public void testIsPagingRequired() {
		PagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();
		assertTrue(pagingBehavior.isRequired(new OffsetLimitPagingSpec(1L, null)));
		assertTrue(pagingBehavior.isRequired(new OffsetLimitPagingSpec(0L, 30L)));
	}

	@Test
	public void testIsNotRequired() {
		assertFalse(new OffsetLimitPagingBehavior().isRequired(new OffsetLimitPagingSpec()));
	}

	@Test
	public void testBuild() {
		PagingBehavior pagingBehavior = new OffsetLimitPagingBehavior();

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
		pagingBehavior.build(pagedLinksInformation, resourceList, querySpecAdapter, urlBuilder);

		assertThat(pagedLinksInformation.getFirst(), equalTo("http://some.org"));
		assertThat(pagedLinksInformation.getNext(), equalTo("http://some.org"));
		assertNull(pagedLinksInformation.getPrev());
		assertThat(pagedLinksInformation.getLast(), equalTo("http://some.org"));
	}
}
