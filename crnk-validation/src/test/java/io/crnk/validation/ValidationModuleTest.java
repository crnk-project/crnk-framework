package io.crnk.validation;

import org.junit.Assert;
import org.junit.Test;

public class ValidationModuleTest {


	@Test
	public void testName() {
		Assert.assertEquals("validation", ValidationModule.create().getModuleName());
	}
}
