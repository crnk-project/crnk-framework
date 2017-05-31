package io.crnk.core.engine.internal.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class MultivaluedMapTest {

	@Test
	public void testBasics() {
		MultivaluedMap map = new MultivaluedMap();
		Assert.assertTrue(map.isEmpty());
		Assert.assertFalse(map.containsKey("a"));
		map.add("a", "b");
		Assert.assertTrue(map.containsKey("a"));
		map.set("a", Arrays.asList("b"));
		Assert.assertTrue(map.containsKey("a"));
		Assert.assertEquals(Arrays.asList("b"), map.getList("a"));
	}

	@Test(expected = IllegalStateException.class)
	public void getUniqueThrowsExceptionOnDuplicate() {
		MultivaluedMap map = new MultivaluedMap();
		map.set("a", Arrays.asList("b", "c"));
		map.getUnique("a");
	}

	@Test
	public void getUniqueReturnsResult() {
		MultivaluedMap map = new MultivaluedMap();
		map.set("a", Arrays.asList("b"));
		Assert.assertEquals("b", map.getUnique("a"));
	}
}
