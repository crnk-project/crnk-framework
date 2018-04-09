package io.crnk.test.mock;

import io.crnk.core.module.SimpleModule;

public class ClientTestModule extends SimpleModule {

	public ClientTestModule() {
		super("test");

		addExceptionMapper(new TestExceptionMapper());
	}

}
