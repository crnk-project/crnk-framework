package io.crnk.meta;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.meta.provider.MetaProvider;

import java.util.ArrayList;
import java.util.List;

public class MetaModuleConfig {


	private List<MetaProvider> providers = new ArrayList<>();

	private boolean initialized = false;

	public void addMetaProvider(MetaProvider provider) {
		checkNotInitialized();
		providers.add(provider);
	}

	public void apply(MetaLookup metaLookup) {
		for (MetaProvider provider : providers) {
			metaLookup.addProvider(provider);
		}
	}

	private void checkNotInitialized() {
		PreconditionUtil.verify(!initialized, "configuration is already applied and cannot be changed anymore");
	}

	protected List<MetaProvider> getProviders() {
		return providers;
	}
}
