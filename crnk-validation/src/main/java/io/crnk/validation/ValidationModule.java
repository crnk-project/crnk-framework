package io.crnk.validation;

import io.crnk.core.module.Module;
import io.crnk.validation.filter.ValidationRepositoryFilter;
import io.crnk.validation.internal.ConstraintViolationExceptionMapper;
import io.crnk.validation.internal.ValidationExceptionMapper;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

public class ValidationModule implements Module {

	private final boolean enableResourceValidation;

	private final ValidatorFactory validatorFactory;

	// protected for CDI
	protected ValidationModule() {
		this(true);
	}

	protected ValidationModule(boolean enableResourceValidation) {
		this(enableResourceValidation, Validation.buildDefaultValidatorFactory());
	}

	protected ValidationModule(boolean enableResourceValidation, ValidatorFactory validatorFactory) {
		this.enableResourceValidation = enableResourceValidation;
		this.validatorFactory = validatorFactory;
	}

	/**
	 * @deprecated make use of {{@link #create()}}
	 */
	@Deprecated
	public static ValidationModule newInstance() {
		return create(true);
	}

	public static ValidationModule create() {
		return create(true);
	}

	public static ValidationModule create(boolean enableResourceValidation) {
		return new ValidationModule(enableResourceValidation);
	}

	public static ValidationModule create(boolean enableResourceValidation, ValidatorFactory validatorFactory) {
		return new ValidationModule(enableResourceValidation, validatorFactory);
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
			context.addRepositoryFilter(new ValidationRepositoryFilter(validatorFactory));
		}
	}
}
