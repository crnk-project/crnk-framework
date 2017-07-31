package io.crnk.gen.typescript.model;

import io.crnk.gen.typescript.model.libraries.ExpressionLibrary;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class ExpressionLibraryTest {

	@Test
	public void checkHasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(ExpressionLibrary.class);
	}

	@Test
	public void checkGetStringExpression() {
		Assert.assertSame(ExpressionLibrary.STRING_EXPRESSION, ExpressionLibrary.getPrimitiveExpression("string"));
	}

	@Test
	public void checkGetNumberExpression() {
		Assert.assertSame(ExpressionLibrary.NUMBER_EXPRESSION, ExpressionLibrary.getPrimitiveExpression("number"));
	}

	@Test
	public void checkGetBooleanExpression() {
		Assert.assertSame(ExpressionLibrary.BOOLEAN_EXPRESSION, ExpressionLibrary.getPrimitiveExpression("boolean"));
	}

	@Test(expected = IllegalStateException.class)
	public void throwExceptionOnUnknownPrimitiveException() {
		ExpressionLibrary.getPrimitiveExpression("doesNotExist");
	}
}
