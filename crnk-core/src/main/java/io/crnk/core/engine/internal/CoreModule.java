package io.crnk.core.engine.internal;

import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.internal.repository.DefaultRepositoryAdapterFactory;
import io.crnk.core.module.Module;
import io.crnk.legacy.repository.information.DefaultRelationshipRepositoryInformationProvider;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationProvider;

public class CoreModule implements Module {

	private RepositoryInformationProvider defaultRepositoryInformationProvider =
			new DefaultResourceRepositoryInformationProvider();

	@Override
	public String getModuleName() {
		return "core";
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addRepositoryAdapterFactory(new DefaultRepositoryAdapterFactory(context.getModuleRegistry()));
		if (defaultRepositoryInformationProvider != null) {
			context.addRepositoryInformationBuilder(defaultRepositoryInformationProvider);
		}
		context.addRepositoryInformationBuilder(new DefaultRelationshipRepositoryInformationProvider());
	}

	public void setDefaultRepositoryInformationProvider(RepositoryInformationProvider defaultRepositoryInformationProvider) {
		this.defaultRepositoryInformationProvider = defaultRepositoryInformationProvider;
	}
}
