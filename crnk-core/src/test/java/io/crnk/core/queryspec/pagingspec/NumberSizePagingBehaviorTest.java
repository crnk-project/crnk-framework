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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.crnk.core.engine.internal.registry.ResourceRegistryImpl;
import io.crnk.core.engine.query.QueryContext;
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

public class NumberSizePagingBehaviorTest {

	@Test
	public void testSerializeDefault() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		Map<String, Set<String>> result = pagingBehavior.serialize(new NumberSizePagingSpec(), "tasks");

		assertFalse(result.containsKey("page[number]"));
		assertFalse(result.containsKey("page[size]"));
		assertEquals(0, result.size());
	}

	@Test
	public void testSerializeOffset() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		Map<String, Set<String>> result = pagingBehavior.serialize(new NumberSizePagingSpec(2, null), "tasks");

		assertEquals(ImmutableSet.of("2"), result.get("page[number]"));
		assertFalse(result.containsKey("page[size]"));
		assertEquals(1, result.size());
	}

	@Test
	public void testSerializeLimit() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		Map<String, Set<String>> result = pagingBehavior.serialize(new NumberSizePagingSpec(1, 30), "tasks");

		assertTrue(result.containsKey("page[number]"));
		assertEquals(ImmutableSet.of("30"), result.get("page[size]"));
		assertEquals(2, result.size());
	}

	@Test
	public void testSerialize() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		Map<String, Set<String>> result = pagingBehavior.serialize(new NumberSizePagingSpec(1, 30), "tasks");

		assertEquals(ImmutableSet.of("1"), result.get("page[number]"));
		assertEquals(ImmutableSet.of("30"), result.get("page[size]"));
		assertEquals(2, result.size());
	}

	@Test
	public void testDeserializeDefaultWithNoParameters() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		NumberSizePagingSpec result = pagingBehavior.deserialize(Collections.<String, Set<String>>emptyMap());

		assertEquals(new NumberSizePagingSpec(1, null), result);
	}

	@Test
	public void testDeserializeDefaultWithOffset() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		NumberSizePagingSpec result =
				pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("number", ImmutableSet.of("2")));

		assertEquals(new NumberSizePagingSpec(2, null), result);
	}

	@Test
	public void testDeserializeDefaultWithLimit() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		NumberSizePagingSpec result =
				pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("size", ImmutableSet.of("30")));

		assertEquals(new NumberSizePagingSpec(1, 30), result);
	}

	@Test
	public void testDeserializeOffsetWithNoParameters() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		pagingBehavior.setDefaultNumber(1);
		NumberSizePagingSpec result = pagingBehavior.deserialize(Collections.<String, Set<String>>emptyMap());

		assertEquals(new NumberSizePagingSpec(1, null), result);
	}

	@Test
	public void testDeserializeOffsetWithOffset() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		pagingBehavior.setDefaultNumber(1);
		NumberSizePagingSpec result =
				pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("number", ImmutableSet.of("1")));

		assertEquals(new NumberSizePagingSpec(1, null), result);
	}

	@Test
	public void testDeserializeOffsetWithLimit() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		pagingBehavior.setDefaultNumber(1);
		NumberSizePagingSpec result =
				pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("size", ImmutableSet.of("30")));

		assertEquals(new NumberSizePagingSpec(1, 30), result);
	}

	@Test
	public void testDeserializeLimitWithNoParameters() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		pagingBehavior.setDefaultLimit(30L);
		NumberSizePagingSpec result = pagingBehavior.deserialize(Collections.<String, Set<String>>emptyMap());

		assertEquals(new NumberSizePagingSpec(1, 30), result);
	}

	@Test
	public void testDeserializeLimitWithOffset() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		pagingBehavior.setDefaultLimit(30L);
		NumberSizePagingSpec result =
				pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("number", ImmutableSet.of("1")));

		assertEquals(new NumberSizePagingSpec(1, 30), result);
	}

	@Test
	public void testDeserializeLimitWithLimit() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		pagingBehavior.setDefaultLimit(30L);
		NumberSizePagingSpec result =
				pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("size", ImmutableSet.of("10")));

		assertEquals(new NumberSizePagingSpec(1, 10), result);
	}

	@Test
	public void testDeserialize() {
		NumberSizePagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		NumberSizePagingSpec result =
				pagingBehavior.deserialize(ImmutableMap.<String, Set<String>>of("number", ImmutableSet.of("2"),
						"size", ImmutableSet.of("30")));

		assertEquals(new NumberSizePagingSpec(2, 30), result);
	}

	@Test
	public void testIsPagingRequired() {
		PagingBehavior pagingBehavior = new NumberSizePagingBehavior();
		assertTrue(pagingBehavior.isRequired(new NumberSizePagingSpec(2, null)));
		assertTrue(pagingBehavior.isRequired(new NumberSizePagingSpec(1, 30)));
		assertFalse(pagingBehavior.isRequired(new NumberSizePagingSpec(1, null)));
	}

	@Test
	public void testIsNotRequired() {
		assertFalse(new NumberSizePagingBehavior().isRequired(new NumberSizePagingSpec()));
	}

	@Test
	public void testBuild() {
		PagingBehavior pagingBehavior = new NumberSizePagingBehavior();

		NumberSizePagingSpec pagingSpec = new NumberSizePagingSpec(1, 10);

		ModuleRegistry moduleRegistry = new ModuleRegistry();
		ResourceRegistry resourceRegistry = new ResourceRegistryImpl(new DefaultResourceRegistryPart(), moduleRegistry);
		QueryContext queryContext = new QueryContext();
		queryContext.setBaseUrl("http://some.org");

		QuerySpec spec = new QuerySpec(Task.class);
		QuerySpecAdapter querySpecAdapter = new QuerySpecAdapter(spec, resourceRegistry, queryContext);
		querySpecAdapter.setPagingSpec(pagingSpec);

		PagingSpecUrlBuilder urlBuilder = mock(PagingSpecUrlBuilder.class);
		when(urlBuilder.build(any(QuerySpecAdapter.class))).thenReturn(queryContext.getBaseUrl());

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
