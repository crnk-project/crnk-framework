package io.crnk.client.module;

import io.crnk.core.module.Module;
import io.crnk.legacy.repository.information.DefaultRelationshipRepositoryInformationProvider;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationProvider;

public class ClientModule implements Module {

	@Override
	public String getModuleName() {
		return "client";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepositoryInformationBuilder(new DefaultResourceRepositoryInformationProvider());
		context.addRepositoryInformationBuilder(new DefaultRelationshipRepositoryInformationProvider());
	}

}
