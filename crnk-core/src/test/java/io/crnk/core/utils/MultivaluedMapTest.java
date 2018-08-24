package io.crnk.core.utils;

import java.util.ArrayList;
import java.util.List;

import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.module.TestResource;
import io.crnk.core.queryspec.PathSpec;
import org.junit.Assert;
import org.junit.Test;

public class MultivaluedMapTest {

	@Test
	public void testFromCollection() {
		TestResource t1 = new TestResource();
		t1.setId(1);

		TestResource t2 = new TestResource();
		t2.setId(2);

		List<TestResource> list = new ArrayList<>();
		list.add(t1);
		list.add(t2);

		MultivaluedMap<Object, TestResource> map = MultivaluedMap.fromCollection(list, PathSpec.of("id"));
		Assert.assertEquals(t1, map.getUnique(1));
		Assert.assertEquals(t2, map.getUnique(2));
	}
}
