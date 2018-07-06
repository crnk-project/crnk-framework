package io.crnk.core.repository;

import java.io.Serializable;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

/**
 * Wraps another resource repository. In contrast to decorators, a wrapped repository is still a repository and treated as such.
 */
public abstract class WrappedResourceRepository<T, I extends Serializable> implements ResourceRepositoryV2<T, I> {

	private ResourceRepositoryV2<T, I> wrappedRepository;

	@Override
	public Class<T> getResourceClass() {
		return wrappedRepository.getResourceClass();
	}

	@Override
	public T findOne(I id, QuerySpec querySpec) {
		return wrappedRepository.findOne(id, querySpec);
	}

	@Override
	public ResourceList<T> findAll(QuerySpec querySpec) {
		return wrappedRepository.findAll(querySpec);
	}

	@Override
	public ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec) {
		return wrappedRepository.findAll(ids, querySpec);
	}

	@Override
	public <S extends T> S save(S entity) {
		return wrappedRepository.save(entity);
	}

	@Override
	public <S extends T> S create(S entity) {
		return wrappedRepository.create(entity);
	}

	@Override
	public void delete(I id) {
		wrappedRepository.delete(id);
	}

	public ResourceRepositoryV2<T, I> getWrappedRepository() {
		return wrappedRepository;
	}

	public void setWrappedRepository(ResourceRepositoryV2<T, I> wrappedRepository) {
		this.wrappedRepository = wrappedRepository;
	}
}
