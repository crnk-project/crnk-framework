package io.crnk.validation.internal;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.validation.ValidationModule;

public class ValidationClientModuleFactory implements ClientModuleFactory {

	@Override
	public ValidationModule create() {
		return ValidationModule.create();
	}
}