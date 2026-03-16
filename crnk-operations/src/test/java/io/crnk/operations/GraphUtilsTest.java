package io.crnk.operations;

import io.crnk.operations.internal.GraphUtils;
import io.crnk.operations.internal.OperationParameterUtils;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphUtilsTest {


	@Test
	public void testNodeWithoutEdge() {
		GraphUtils.Node node = new GraphUtils.Node("a", null);
		List<GraphUtils.Node> results = GraphUtils.sort(Arrays.asList(node));
		Assert.assertSame(node, results.get(0));
	}

	@Test
	public void testTwoNodesWithoutEdge() {
		GraphUtils.Node a = new GraphUtils.Node("a", null);
		GraphUtils.Node b = new GraphUtils.Node("b", null);
		List<GraphUtils.Node> results = GraphUtils.sort(Arrays.asList(a, b));
		Assert.assertSame(a, results.get(0));
		Assert.assertSame(b, results.get(1));
	}

	@Test
	public void testHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(GraphUtils.class);
	}

	@Test
	public void testNodeEquals() {
		GraphUtils.Node left = new GraphUtils.Node("a", "left");
		GraphUtils.Node same = new GraphUtils.Node("a", "right");
		GraphUtils.Node different = new GraphUtils.Node("b", "left");
		Assert.assertEquals(left, same);
		Assert.assertEquals(left.hashCode(), same.hashCode());
		Assert.assertNotEquals(left, different);
	}


	@Test
	public void testNodeToString() {
		GraphUtils.Node node = new GraphUtils.Node("test", null);
		Assert.assertEquals("test", node.toString());
	}

	@Test
	public void testEdgeEquals() {
		GraphUtils.Edge left = new GraphUtils.Edge(new GraphUtils.Node("a", null), new GraphUtils.Node("b", null));
		GraphUtils.Edge same = new GraphUtils.Edge(new GraphUtils.Node("a", "ignored"), new GraphUtils.Node("b", "ignored"));
		GraphUtils.Edge different = new GraphUtils.Edge(new GraphUtils.Node("a", null), new GraphUtils.Node("c", null));
		Assert.assertEquals(left, same);
		Assert.assertEquals(left.hashCode(), same.hashCode());
		Assert.assertNotEquals(left, different);
	}

	@Test
	public void testParseSingleParameter() {
		Map<String, Set<String>> map = OperationParameterUtils.parseParameters("test?a=b");
		Assert.assertEquals(1, map.size());
		Assert.assertEquals("b", map.get("a").iterator().next());
		Assert.assertEquals(1, map.get("a").size());
	}

}
