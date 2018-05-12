package io.crnk.setup.vertx.suite;

import io.crnk.test.suite.BasicRepositoryAccessTestBase;

public class RepositoryAccessVertxTest extends BasicRepositoryAccessTestBase {

	public RepositoryAccessVertxTest() {
		testContainer = new VertxTestContainer();
	}
}
