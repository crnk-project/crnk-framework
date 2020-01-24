package io.crnk.data;

import io.crnk.test.suite.RelationIdAccessTestBase;

public class PlainTextRelationIdAccessClientTest extends RelationIdAccessTestBase {

	public PlainTextRelationIdAccessClientTest() {
		PlainJsonTestContainer testContainer = new PlainJsonTestContainer();
		this.testContainer = testContainer;
	}
}
