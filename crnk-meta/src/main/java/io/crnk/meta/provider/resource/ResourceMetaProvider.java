package io.crnk.meta.provider.resource;

import io.crnk.meta.internal.JsonObjectMetaProvider;
import io.crnk.meta.internal.ResourceMetaProviderImpl;
import io.crnk.meta.provider.MetaProvider;
import io.crnk.meta.provider.MetaProviderBase;

import java.util.Arrays;
import java.util.Collection;

public class ResourceMetaProvider extends MetaProviderBase {

	private boolean useResourceRegistry;

	public ResourceMetaProvider() {
		this(true);
	}

	public ResourceMetaProvider(boolean useResourceRegistry) {
		this.useResourceRegistry = useResourceRegistry;
	}

	@Override
	public Collection<MetaProvider> getDependencies() {
		return Arrays.asList((MetaProvider) new ResourceMetaProviderImpl(useResourceRegistry), new JsonObjectMetaProvider());
	}
}
