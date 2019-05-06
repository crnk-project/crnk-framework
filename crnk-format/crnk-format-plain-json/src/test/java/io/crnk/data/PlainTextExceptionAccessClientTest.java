package io.crnk.data;

import io.crnk.test.suite.ExceptionAccessTestBase;

public class PlainTextExceptionAccessClientTest extends ExceptionAccessTestBase {

	public PlainTextExceptionAccessClientTest() {
		PlainJsonTestContainer testContainer = new PlainJsonTestContainer();
		this.testContainer = testContainer;
	}
}