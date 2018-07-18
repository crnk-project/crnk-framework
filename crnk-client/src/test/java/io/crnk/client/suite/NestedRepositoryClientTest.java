package io.crnk.client.suite;

import io.crnk.test.suite.NestedRepositoryAccessTestBase;

public class NestedRepositoryClientTest extends NestedRepositoryAccessTestBase {

	public NestedRepositoryClientTest() {
		ClientTestContainer testContainer = new ClientTestContainer();
		this.testContainer = testContainer;
	}

}