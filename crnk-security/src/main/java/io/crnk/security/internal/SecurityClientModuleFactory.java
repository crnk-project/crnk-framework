package io.crnk.security.internal;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.security.SecurityModule;

public class SecurityClientModuleFactory implements ClientModuleFactory {

	@Override
	public SecurityModule create() {
		return SecurityModule.newClientModule();
	}
}