package io.crnk.validation;

import io.crnk.core.module.Module;
import io.crnk.validation.filter.ValidationRepositoryFilter;
import io.crnk.validation.internal.ConstraintViolationExceptionMapper;
import io.crnk.validation.internal.ValidationExceptionMapper;

public class ValidationModule implements Module {

	private final boolean enableResourceValidation;

	// protected for CDI
	protected ValidationModule() {
		this(true);
	}

	protected ValidationModule(boolean enableResourceValidation) {
		this.enableResourceValidation = enableResourceValidation;
	}

	/**
	 * @deprecated make use of {{@link #create()}}
	 */
	@Deprecated
	public static ValidationModule newInstance() {
		return new ValidationModule(true);
	}

	public static ValidationModule create() {
		return new ValidationModule(true);
	}

	public static ValidationModule create(boolean enableResourceValidation) {
		return new ValidationModule(enableResourceValidation);
	}

	@Override
	public String getModuleName() {
		return "validation";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addExceptionMapper(new ConstraintViolationExceptionMapper(context));
		context.addExceptionMapper(new ValidationExceptionMapper());

		if (enableResourceValidation) {
			context.addRepositoryFilter(new ValidationRepositoryFilter());
		}
	}
}
