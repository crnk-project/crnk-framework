package io.crnk.data.facet;

import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtension;
import io.crnk.data.facet.provider.FacetProvider;

import java.util.ArrayList;
import java.util.List;

public class FacetModuleExtension implements ModuleExtension {

	private List<FacetProvider> providers = new ArrayList<>();

	public List<FacetProvider> getProviders() {
		return providers;
	}

	@Override
	public Class<? extends Module> getTargetModule() {
		return FacetModule.class;
	}

	@Override
	public boolean isOptional() {
		return true;
	}

	public void addProvider(FacetProvider provider) {
		providers.add(provider);
	}
}
