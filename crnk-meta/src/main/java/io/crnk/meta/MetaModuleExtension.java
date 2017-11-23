package io.crnk.meta;

import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtension;
import io.crnk.meta.provider.MetaProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetaModuleExtension implements ModuleExtension {

	private List<MetaProvider> providers = new ArrayList<>();

	@Override
	public Class<? extends Module> getTargetModule() {
		return MetaModule.class;
	}

	@Override
	public boolean isOptional() {
		return true;
	}

	public void addProvider(MetaProvider provider) {
		providers.add(provider);
	}

	protected List<MetaProvider> getProviders() {
		return Collections.unmodifiableList(providers);
	}
}
