package io.crnk.validation;

import io.crnk.core.module.Module;
import io.crnk.validation.filter.ValidationRepositoryFilter;
import io.crnk.validation.internal.ConstraintViolationExceptionMapper;
import io.crnk.validation.internal.ValidationExceptionMapper;

public class ValidationModule implements Module {

	// protected for CDI
	protected ValidationModule() {
	}

	/**
	 * @deprecated make use of {{@link #create()}}
	 */
	@Deprecated
	public static ValidationModule newInstance() {
		return new ValidationModule();
	}

	public static ValidationModule create() {
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

		context.addRepositoryFilter(new ValidationRepositoryFilter());
	}
}
