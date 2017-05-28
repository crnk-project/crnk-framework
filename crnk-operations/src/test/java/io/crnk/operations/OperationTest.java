package io.crnk.operations;

import io.crnk.core.engine.document.Resource;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

public class OperationTest {


	@Test
	public void testEquals() throws NoSuchFieldException {
		EqualsVerifier.forClass(Operation.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();

	}

	@Test
	public void testHashCode() throws NoSuchFieldException {
		Operation op1 = new Operation("a", "b", new Resource());
		Operation op2 = new Operation("a", "b", new Resource());
		Operation op3 = new Operation("x", "b", new Resource());
		Assert.assertEquals(op1, op2);
		Assert.assertNotEquals(op3.hashCode(), op2.hashCode());
	}

}
