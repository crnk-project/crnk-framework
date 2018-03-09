package io.crnk.client.module;

import io.crnk.test.mock.ClientTestModule;

public class TestClientModuleFactory implements ClientModuleFactory {

	@Override
	public ClientTestModule create() {
		return new ClientTestModule();
	}
}
