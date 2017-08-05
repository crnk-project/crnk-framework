package io.crnk.client.module;

public class TestClientModuleFactory implements ClientModuleFactory {

	@Override
	public TestModule create() {
		return new TestModule();
	}
}
