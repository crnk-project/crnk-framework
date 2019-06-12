package io.crnk.data;

import io.crnk.test.suite.NestedRepositoryAccessTestBase;

public class PlainTextNestedRepositoryAccessClientTest extends NestedRepositoryAccessTestBase {

	public PlainTextNestedRepositoryAccessClientTest() {
		PlainJsonTestContainer testContainer = new PlainJsonTestContainer();
		this.testContainer = testContainer;
	}
}
