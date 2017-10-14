package io.crnk.meta.provider.resource;

import io.crnk.meta.internal.MetaIdProvider;
import io.crnk.meta.internal.resource.ResourceMetaFilter;
import io.crnk.meta.internal.resource.ResourceMetaParitition;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.*;
import io.crnk.meta.provider.MetaFilter;
import io.crnk.meta.provider.MetaPartition;
import io.crnk.meta.provider.MetaProviderBase;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ResourceMetaProvider extends MetaProviderBase {

	public static final String DEFAULT_ID_PREFIX = "resources";

	private final ResourceMetaParitition partition;

	private MetaIdProvider idProvider = new MetaIdProvider();

	public ResourceMetaProvider() {
		this(DEFAULT_ID_PREFIX + ".");
	}

	public ResourceMetaProvider(String idPrefix) {
		this.partition = new ResourceMetaParitition(idPrefix, idProvider);
	}

	public void putIdMapping(String packageName, String idPrefix) {
		idProvider.putIdMapping(packageName, idPrefix);
	}

	@Override
	public Collection<MetaFilter> getFilters() {
		return Arrays.asList((MetaFilter) new ResourceMetaFilter(partition, context));
	}

	@Override
	public Collection<MetaPartition> getPartitions() {
		return Arrays.asList((MetaPartition) partition);
	}

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		return new HashSet<>(Arrays.asList(MetaResourceBase.class, MetaResource.class, MetaJsonObject.class, MetaResourceField
				.class, MetaResourceRepository.class, MetaResourceAction.class));
	}

	public <T extends MetaElement> T getMeta(Type type) {
		context.checkInitialized();
		return (T) partition.getMeta(type);
	}

	public <T extends MetaElement> T allocateMeta(Type type) {
		context.checkInitialized();
		return (T) partition.allocateMetaElement(type).get();
	}
}
