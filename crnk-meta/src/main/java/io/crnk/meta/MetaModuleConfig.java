package io.crnk.meta;

import java.util.ArrayList;
import java.util.List;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaProvider;

public class MetaModuleConfig {

	private List<IdMapping> idMappings = new ArrayList<>();

	private List<MetaProvider> providers = new ArrayList<>();

	private boolean initialized = false;

	public void addIdMapping(String packageName, String idPrefix) {
		checkNotInitialized();
		idMappings.add(new IdMapping(packageName, null, idPrefix));
	}

	public void addIdMapping(String packageName, Class<? extends MetaElement> type, String idPrefix) {
		checkNotInitialized();
		idMappings.add(new IdMapping(packageName, type, idPrefix));
	}

	public void addMetaProvider(MetaProvider provider) {
		checkNotInitialized();
		providers.add(provider);
	}

	protected void apply(MetaLookup metaLookup) {
		for (IdMapping mapping : idMappings) {
			if (mapping.type != null) {
				metaLookup.putIdMapping(mapping.packageName, mapping.type, mapping.idPrefix);
			}
			else {
				metaLookup.putIdMapping(mapping.packageName, mapping.idPrefix);
			}
		}

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

	protected List<IdMapping> getIdMappings() {
		return idMappings;
	}

	protected static class IdMapping {

		private final String packageName;

		private final Class<? extends MetaElement> type;

		private final String idPrefix;

		public IdMapping(String packageName, Class<? extends MetaElement> type, String idPrefix) {
			this.packageName = packageName;
			this.type = type;
			this.idPrefix = idPrefix;
		}
	}
}
