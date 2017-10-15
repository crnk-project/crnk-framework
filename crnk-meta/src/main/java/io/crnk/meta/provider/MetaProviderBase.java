package io.crnk.meta.provider;

import io.crnk.meta.model.MetaElement;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class MetaProviderBase implements MetaProvider {

	protected MetaProviderContext context;

	@Override
	public Collection<MetaProvider> getDependencies() {
		return Collections.emptySet();
	}

	@Override
	public Collection<MetaPartition> getPartitions() {
		return Collections.emptySet();
	}

	@Override
	public Collection<MetaFilter> getFilters() {
		return Collections.emptySet();
	}

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		return Collections.emptySet();
	}

	@Override
	public void init(MetaProviderContext context) {
		this.context = context;
	}
}
