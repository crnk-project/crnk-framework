package io.crnk.operations;

import io.crnk.operations.internal.OperationParameterUtils;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class OperationsParameterUtilsTest {


	@Test
	public void testHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(OperationParameterUtils.class);
	}

	@Test
	public void testParseSingleParameter() {
		Map<String, Set<String>> map = OperationParameterUtils.parseParameters("test?a=b");
		Assert.assertEquals(1, map.size());
		Assert.assertEquals("b", map.get("a").iterator().next());
		Assert.assertEquals(1, map.get("a").size());
	}

	@Test
	public void testParseRepeatedParameter() {
		Map<String, Set<String>> map = OperationParameterUtils.parseParameters("test?a=b&a=c");
		Assert.assertEquals(1, map.size());
		Assert.assertEquals(2, map.get("a").size());
		Assert.assertTrue(map.get("a").contains("b"));
		Assert.assertTrue(map.get("a").contains("c"));
	}

	@Test
	public void testParseMultipleParameter() {
		Map<String, Set<String>> map = OperationParameterUtils.parseParameters("test?a=b&c=d");
		Assert.assertEquals(2, map.size());
		Assert.assertEquals(1, map.get("a").size());
		Assert.assertEquals(1, map.get("c").size());

		Assert.assertEquals("b", map.get("a").iterator().next());
		Assert.assertEquals("d", map.get("c").iterator().next());
	}

	@Test
	public void testParseNoParameter() {
		Assert.assertTrue(OperationParameterUtils.parseParameters("test").isEmpty());
		Assert.assertTrue(OperationParameterUtils.parseParameters("test?").isEmpty());
	}

	@Test
	public void testParsePath() {
		Assert.assertEquals("test", OperationParameterUtils.parsePath("test?a=b"));
		Assert.assertEquals("test", OperationParameterUtils.parsePath("test"));
	}
}

