package io.crnk.meta.provider.resource;

import io.crnk.meta.internal.JsonObjectMetaProvider;
import io.crnk.meta.internal.ResourceMetaProviderImpl;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.*;
import io.crnk.meta.provider.MetaProvider;
import io.crnk.meta.provider.MetaProviderBase;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		return new HashSet<>(Arrays.asList(MetaResource.class, MetaJsonObject.class, MetaResourceField.class, MetaResourceRepository.class, MetaResourceAction.class));
	}
}
