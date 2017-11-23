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

	protected void apply(MetaLookup metaLookup) {
		for (MetaProvider provider : providers) {
			metaLookup.addProvider(provider);
		}
	}

	private void checkNotInitialized() {
		PreconditionUtil.assertFalse("configuration is already applied and cannot be changed anymore",
				initialized);
	}

	protected List<MetaProvider> getProviders() {
		return providers;
	}
}
