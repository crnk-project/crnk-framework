package io.crnk.core.engine.internal;

import io.crnk.core.engine.http.DefaultHttpStatusBehavior;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.internal.registry.DefaultRegistryEntryBuilder;
import io.crnk.core.engine.internal.repository.DefaultRepositoryAdapterFactory;
import io.crnk.core.module.Module;
import io.crnk.legacy.repository.information.DefaultRelationshipRepositoryInformationProvider;
import io.crnk.legacy.repository.information.DefaultResourceRepositoryInformationProvider;

public class CoreModule implements Module {

	public static final String NAME = "core";

	static {
		DefaultRegistryEntryBuilder.FAIL_ON_MISSING_REPOSITORY = false;

	}

	private RepositoryInformationProvider defaultRepositoryInformationProvider =
			new DefaultResourceRepositoryInformationProvider();

	@Override
	public String getModuleName() {
		return NAME;
	}

	@Override
	public void setupModule(ModuleContext context) {
		context.addHttpStatusBehavior(new DefaultHttpStatusBehavior());
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
