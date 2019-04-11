package io.crnk.core.repository;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Recommended base class to implement a resource repository making use of the
 * QuerySpec and ResourceList. Note that the former
 * {@link ResourceRepositoryBase} will be removed in the near future.
 * <p>
 * Base implements for {@link ResourceRepository} implementing most of the
 * methods. Unless {@link #save(T)} and {@link #delete(I)} get
 * overridden, this repository is read-only. Only {@link #findAll(QuerySpec)}
 * needs to be implemented to have a working repository.
 *
 * @param <T> resource type
 * @param <I> identity type
 */
public abstract class ResourceRepositoryBase<T, I extends Serializable> implements ResourceRepository<T, I>, ResourceRegistryAware {

	private Class<T> resourceClass;

	private ResourceRegistry resourceRegistry;

	public ResourceRepositoryBase(Class<T> resourceClass) {
		this.resourceClass = resourceClass;
	}

	@Override
	public Class<T> getResourceClass() {
		return resourceClass;
	}

	/**
	 * Forwards to {@link #findAll(QuerySpec)}
	 *
	 * @param id        of the resource
	 * @param querySpec for field and relation inclusion
	 * @return resource
	 */
	@Override
	public T findOne(I id, QuerySpec querySpec) {
		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
		String idName = entry.getResourceInformation().getIdField().getUnderlyingName();

		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, id));
		Iterable<T> iterable = findAll(idQuerySpec);
		Iterator<T> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			T resource = iterator.next();
			PreconditionUtil.verify(!iterator.hasNext(), "expected unique result for id=%s, querySpec=%s", id, querySpec);
			return resource;
		} else {
			throw new ResourceNotFoundException("resource not found: " + id);
		}
	}

	/**
	 * Forwards to {@link #findAll(QuerySpec)}
	 *
	 * @param ids       of the resources
	 * @param querySpec for field and relation inclusion
	 * @return resources
	 */
	@Override
	public ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
		String idName = entry.getResourceInformation().getIdField().getUnderlyingName();

		QuerySpec idQuerySpec = querySpec.duplicate();
		idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, ids));
		return findAll(idQuerySpec);
	}

	/**
	 * read-only by default
	 *
	 * @param resource to save
	 * @return saved resource
	 */
	@Override
	public <S extends T> S save(S resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends T> S create(S resource) {
		return save(resource);
	}

	/**
	 * read-only by default
	 *
	 * @param id of resource to delete
	 */
	@Override
	public void delete(I id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}
}
