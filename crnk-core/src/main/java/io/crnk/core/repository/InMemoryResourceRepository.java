package io.crnk.core.repository;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple repository implementation backed by a ConcurrentHashMap. Ideally suited for testing and mocking where the real implementation may not (yet) be available.
 */
public class InMemoryResourceRepository<T, I > extends ResourceRepositoryBase<T, I> {

	private Map<I, T> resources = new ConcurrentHashMap<>();

	private ResourceRegistry resourceRegistry;

	public InMemoryResourceRepository(Class<T> resourceClass) {
		super(resourceClass);
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
		RegistryEntry entry = resourceRegistry.findEntry(getResourceClass());
		ResourceField idField = entry.getResourceInformation().getIdField();
		I id = (I) idField.getAccessor().getValue(entity);
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