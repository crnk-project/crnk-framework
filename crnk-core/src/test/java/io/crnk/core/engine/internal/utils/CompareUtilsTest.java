package io.crnk.core.engine.internal.utils;

import org.junit.Assert;
import org.junit.Test;

public class CompareUtilsTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(CompareUtils.class);
	}

	@Test
	public void test() {
		Assert.assertTrue(CompareUtils.isEquals("a", "a"));
		Assert.assertFalse(CompareUtils.isEquals(null, "a"));
		Assert.assertFalse(CompareUtils.isEquals("a", null));
		Assert.assertTrue(CompareUtils.isEquals(null, null));
		Assert.assertFalse(CompareUtils.isEquals("b", "a"));
	}

}
