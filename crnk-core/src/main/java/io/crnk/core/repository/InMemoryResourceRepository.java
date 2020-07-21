package io.crnk.core.repository;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.*;
import io.crnk.core.engine.internal.information.resource.DefaultResourceFieldInformationProvider;
import io.crnk.core.engine.internal.information.resource.DefaultResourceInformationProvider;
import io.crnk.core.engine.internal.information.resource.ReflectionFieldAccessor;
import io.crnk.core.engine.internal.jackson.JacksonResourceFieldInformationProvider;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.OffsetLimitPagingBehavior;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.list.ResourceList;

import java.beans.BeanInfo;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple repository implementation backed by a ConcurrentHashMap. Ideally suited for testing and mocking where the real implementation may not (yet) be available.
 */
public class InMemoryResourceRepository<T, I> extends ResourceRepositoryBase<T, I> {

	protected Map<I, T> resources = new ConcurrentHashMap<>();

	private ResourceRegistry resourceRegistry;

	private ResourceFieldAccessor idField;

	public InMemoryResourceRepository(Class<T> resourceClass) {
		super(resourceClass);
	}

	public Map<I, T> getMap() {
		return resources;
	}

	public void clear() {
		resources.clear();
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		return querySpec.apply(resources.values());
	}

	@Override
	public <S extends T> S save(S entity) {
		if (idField == null && resourceRegistry != null) {
			RegistryEntry entry = resourceRegistry.findEntry(getResourceClass());
			idField = entry.getResourceInformation().getIdField().getAccessor();
		} else if (idField == null) {
			BeanInformation beanInformation = BeanInformation.get(getResourceClass());
			Optional<BeanAttributeInformation> idAttr = beanInformation.getAttributes().stream().filter(it -> it.getAnnotation(JsonApiId.class) != null).findFirst();
			PreconditionUtil.verify(idAttr.isPresent(), "no @JsonApiId attribute found on %s", getResourceClass());
			idField = new ReflectionFieldAccessor(getResourceClass(), idAttr.get().getName(), idAttr.get().getImplementationClass());
		}
		I id = (I) idField.getValue(entity);
		PreconditionUtil.verify(id != null, "resource %s must have an identifier", resources);
		resources.put(id, entity);
		return entity;
	}

	@Override
	public void delete(I id) {
		resources.remove(id);
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
		super.setResourceRegistry(resourceRegistry);
	}
}