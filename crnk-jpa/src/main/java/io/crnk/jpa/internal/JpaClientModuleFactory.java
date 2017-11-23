package io.crnk.jpa.internal;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.jpa.JpaModule;

public class JpaClientModuleFactory implements ClientModuleFactory {

	@Override
	public JpaModule create() {
		return JpaModule.newClientModule();
	}
}
