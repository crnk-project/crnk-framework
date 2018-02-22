package io.crnk.core.queryspec.pagingspec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
}
