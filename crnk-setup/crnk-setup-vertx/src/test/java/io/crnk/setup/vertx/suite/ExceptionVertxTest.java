package io.crnk.setup.vertx.suite;

import io.crnk.test.suite.ExceptionAccessTestBase;

public class ExceptionVertxTest extends ExceptionAccessTestBase {

	public ExceptionVertxTest() {
		testContainer = new VertxTestContainer();
	}
}