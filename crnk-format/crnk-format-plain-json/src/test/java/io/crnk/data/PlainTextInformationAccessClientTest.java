package io.crnk.data;

import io.crnk.test.suite.InformationAccessTestBase;

public class PlainTextInformationAccessClientTest extends InformationAccessTestBase {

	public PlainTextInformationAccessClientTest() {
		PlainJsonTestContainer testContainer = new PlainJsonTestContainer();
		this.testContainer = testContainer;
	}
}
