package io.crnk.meta;

import org.junit.Assert;
import org.junit.Test;

public class MetaModuleTest {


	@Test
	public void testName() {
		MetaModule module = MetaModule.create();
		Assert.assertEquals("meta", module.getModuleName());
	}
}
