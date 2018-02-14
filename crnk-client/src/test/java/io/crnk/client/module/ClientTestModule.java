package io.crnk.client.module;

import io.crnk.core.module.SimpleModule;
import io.crnk.test.mock.TestExceptionMapper;

public class ClientTestModule extends SimpleModule {

	public ClientTestModule() {
		super("test");

		addExceptionMapper(new TestExceptionMapper());
	}

}
