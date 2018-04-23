package io.crnk.setup.vertx.suite;

import io.crnk.test.suite.RelationIdAccessTestBase;

public class RelationIdVertxTest extends RelationIdAccessTestBase {

	public RelationIdVertxTest() {
		testContainer = new VertxTestContainer();
	}
}
