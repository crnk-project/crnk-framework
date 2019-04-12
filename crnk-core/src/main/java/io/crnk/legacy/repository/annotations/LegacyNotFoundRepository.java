package io.crnk.legacy.repository.annotations;

import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;

import java.io.Serializable;

/**
 * Represents a non-existing resource. It is assigned to a resource class if Crnk couldn't find any resource.
 */
public class LegacyNotFoundRepository<T, ID > implements LegacyResourceRepository<T, ID> {

	private final Class<?> repositoryClass;

	public LegacyNotFoundRepository(Class<? extends T> repositoryClass) {
		this.repositoryClass = repositoryClass;
	}

	@Override
	public T findOne(ID id, QueryParams queryParams) {
		throw new RepositoryNotFoundException(repositoryClass);
	}

	@Override
	public Iterable<T> findAll(QueryParams queryParams) {
		throw new RepositoryNotFoundException(repositoryClass);
	}

	@Override
	public Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
		throw new RepositoryNotFoundException(repositoryClass);
	}

	@Override
	public void delete(ID id) {
		throw new RepositoryNotFoundException(repositoryClass);
	}

	@Override
	public <S extends T> S save(S entity) {
		throw new RepositoryNotFoundException(repositoryClass);
	}
}
