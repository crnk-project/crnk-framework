package io.crnk.gen.typescript.model;

import org.junit.Assert;
import org.junit.Test;

public class TSFunctionTest {

	@Test
	public void notAField() {
		TSFunction function = new TSFunction();
		Assert.assertFalse(function.isField());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void cannotCastToField() {
		TSFunction function = new TSFunction();
		function.asField();
	}
}
