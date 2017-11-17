package io.crnk.core.engine;

import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Assert;
import org.junit.Test;

public class ResourceFieldAccessTest {


	@Test
	public void testEqualsContract() throws NoSuchFieldException {
		EqualsVerifier.forClass(ResourceFieldAccess.class).usingGetClass().verify();
	}

	@Test
	public void and() {
		ResourceFieldAccess all = new ResourceFieldAccess(true, true, true, true, true);
		ResourceFieldAccess none = new ResourceFieldAccess(false, false, false, false, false);
		ResourceFieldAccess a1 = new ResourceFieldAccess(false, true, false, false, false);
		ResourceFieldAccess a2 = new ResourceFieldAccess(false, false, true, false, false);
		ResourceFieldAccess a3 = new ResourceFieldAccess(false, false, false, true, false);
		ResourceFieldAccess a4 = new ResourceFieldAccess(false, false, false, false, true);
		ResourceFieldAccess a5 = new ResourceFieldAccess(true, false, false, false, false);

		Assert.assertEquals(a1, a1.and(all));
		Assert.assertEquals(a1, all.and(a1));
		Assert.assertEquals(none, none.and(a1));

		Assert.assertEquals(a2, a2.and(all));
		Assert.assertEquals(a2, all.and(a2));
		Assert.assertEquals(none, none.and(a2));

		Assert.assertEquals(a3, a3.and(all));
		Assert.assertEquals(a3, all.and(a3));
		Assert.assertEquals(none, none.and(a3));

		Assert.assertEquals(a4, a4.and(all));
		Assert.assertEquals(a4, all.and(a4));
		Assert.assertEquals(none, none.and(a4));

		Assert.assertEquals(none, none.and(all));
		Assert.assertEquals(none, none.and(none));
		Assert.assertEquals(all, all.and(all));

		Assert.assertEquals(a5, a5.and(all));
		Assert.assertEquals(a5, all.and(a5));
		Assert.assertEquals(none, none.and(a5));
	}
}
