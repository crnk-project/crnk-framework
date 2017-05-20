package io.crnk.client;

import io.crnk.core.module.SimpleModule;
import io.crnk.test.mock.TestExceptionMapper;

public class TestModule extends SimpleModule {

	public TestModule() {
		super("test");

		addExceptionMapper(new TestExceptionMapper());
	}

}
