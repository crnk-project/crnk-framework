package io.crnk.spring.exception;

import io.crnk.core.module.Module;

public class SpringExceptionModule implements Module {

	@Override
	public String getModuleName() {
		return "spring.exception";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addExceptionMapper(new BeanExceptionMapper(context));
	}
}
