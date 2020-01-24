package io.crnk.validation;

import io.crnk.core.module.Module;
import io.crnk.validation.filter.ValidationRepositoryFilter;
import io.crnk.validation.internal.ConstraintViolationExceptionMapper;
import io.crnk.validation.internal.ValidationExceptionMapper;

import javax.validation.Validation;
import javax.validation.Validator;

public class ValidationModule implements Module {

	private final boolean enableResourceValidation;

	private final Validator validator;

	// protected for CDI
	protected ValidationModule() {
		this(true);
	}

	protected ValidationModule(boolean enableResourceValidation) {
		this(enableResourceValidation, Validation.buildDefaultValidatorFactory().getValidator());
	}

	protected ValidationModule(boolean enableResourceValidation, Validator validator) {
		this.enableResourceValidation = enableResourceValidation;
		this.validator = validator;
	}

	public static ValidationModule create() {
		return create(true);
	}

	public static ValidationModule create(boolean enableResourceValidation) {
		return new ValidationModule(enableResourceValidation);
	}

	public static ValidationModule create(boolean enableResourceValidation, Validator validator) {
		return new ValidationModule(enableResourceValidation, validator);
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
			context.addRepositoryFilter(new ValidationRepositoryFilter(validator));
		}
	}
}
