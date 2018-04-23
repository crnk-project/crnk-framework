package io.crnk.setup.vertx.suite;

import io.crnk.test.suite.InformationAccessTestBase;

public class InformationVertxTest extends InformationAccessTestBase {

	public InformationVertxTest() {
		testContainer = new VertxTestContainer();
	}
}