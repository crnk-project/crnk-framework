package io.crnk.core.engine.internal;

import io.crnk.core.module.Module;
import io.crnk.legacy.repository.information.DefaultRelationshipRepositoryInformationProvider;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationProvider;

public class CoreModule implements Module {

	@Override
	public String getModuleName() {
		return "core";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepositoryInformationBuilder(new DefaultResourceRepositoryInformationProvider());
		context.addRepositoryInformationBuilder(new DefaultRelationshipRepositoryInformationProvider());
	}
}
