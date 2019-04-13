package io.crnk.data.jpa.internal;

import io.crnk.client.module.ClientModuleFactory;
import io.crnk.data.jpa.JpaModule;

public class JpaClientModuleFactory implements ClientModuleFactory {

	@Override
	public JpaModule create() {
		return JpaModule.newClientModule();
	}
}
