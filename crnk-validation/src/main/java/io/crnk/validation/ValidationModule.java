package io.crnk.validation;

import io.crnk.core.module.Module;
import io.crnk.validation.internal.ConstraintViolationExceptionMapper;
import io.crnk.validation.internal.ValidationExceptionMapper;

public class ValidationModule implements Module {

	@Deprecated
	public ValidationModule() {
	}

	public static final ValidationModule newInstance() {
		return new ValidationModule();
	}

	@Override
	public String getModuleName() {
		return "validation";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addExceptionMapper(new ConstraintViolationExceptionMapper(context));
		context.addExceptionMapper(new ValidationExceptionMapper());
	}
}
