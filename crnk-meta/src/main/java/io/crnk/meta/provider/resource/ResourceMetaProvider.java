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

	public static final String DEFAULT_ID_PREFIX = "resources";

	private String idPrefix;

	public ResourceMetaProvider() {
		this(DEFAULT_ID_PREFIX + ".");
	}

	public ResourceMetaProvider(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	@Override
	public Collection<MetaProvider> getDependencies() {
		ResourceMetaProviderImpl resourceMetaProvider = new ResourceMetaProviderImpl(idPrefix);
		return Arrays.asList((MetaProvider) resourceMetaProvider, new JsonObjectMetaProvider(resourceMetaProvider));
	}

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		return new HashSet<>(Arrays.asList(MetaResourceBase.class, MetaResource.class, MetaJsonObject.class, MetaResourceField
				.class, MetaResourceRepository.class, MetaResourceAction.class));
	}
}
