package io.crnk.meta.provider.resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.crnk.meta.internal.JsonObjectMetaProvider;
import io.crnk.meta.internal.ResourceMetaProviderImpl;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceAction;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.meta.model.resource.MetaResourceRepository;
import io.crnk.meta.provider.MetaProvider;
import io.crnk.meta.provider.MetaProviderBase;

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
		return new HashSet<>(Arrays.asList(MetaResourceBase.class, MetaResource.class, MetaJsonObject.class, MetaResourceField
				.class, MetaResourceRepository.class, MetaResourceAction.class));
	}
}
