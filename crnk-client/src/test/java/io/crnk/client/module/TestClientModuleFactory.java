package io.crnk.client.module;

public class TestClientModuleFactory implements ClientModuleFactory {

	@Override
	public ClientTestModule create() {
		return new ClientTestModule();
	}
}
