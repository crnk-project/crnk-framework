package io.crnk.gen.typescript.model;

import io.crnk.gen.typescript.model.libraries.CrnkLibrary;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class ExpressionLibraryTest {

	@Test
	public void checkHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(CrnkLibrary.class);
	}

	@Test
	public void checkGetStringExpression() {
		Assert.assertSame(CrnkLibrary.STRING_PATH, CrnkLibrary.getPrimitiveExpression("string"));
	}

	@Test
	public void checkGetNumberExpression() {
		Assert.assertSame(CrnkLibrary.NUMBER_PATH, CrnkLibrary.getPrimitiveExpression("number"));
	}

	@Test
	public void checkGetBooleanExpression() {
		Assert.assertSame(CrnkLibrary.BOOLEAN_PATH, CrnkLibrary.getPrimitiveExpression("boolean"));
	}

	@Test(expected = IllegalStateException.class)
	public void throwExceptionOnUnknownPrimitiveException() {
		CrnkLibrary.getPrimitiveExpression("doesNotExist");
	}
}
