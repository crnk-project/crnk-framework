package io.crnk.operations;

import io.crnk.core.engine.document.Resource;
import org.junit.Assert;
import org.junit.Test;

public class OperationTest {


	@Test
	public void testEquals() {
		Operation left = new Operation("add", "path", new Resource());
		Operation same = new Operation("add", "path", new Resource());
		Operation different = new Operation("remove", "path", new Resource());
		Assert.assertEquals(left, same);
		Assert.assertEquals(left.hashCode(), same.hashCode());
		Assert.assertNotEquals(left, different);
	}

	@Test
	public void testHashCode() {
		Operation op1 = new Operation("a", "b", new Resource());
		Operation op2 = new Operation("a", "b", new Resource());
		Operation op3 = new Operation("x", "b", new Resource());
		Assert.assertEquals(op1, op2);
		Assert.assertNotEquals(op3.hashCode(), op2.hashCode());
	}

}
