package io.crnk.legacy.repository;

import io.crnk.legacy.queryParams.QueryParams;

import java.io.Serializable;

public abstract class AbstractSimpleLegacyRepository<T, ID > implements LegacyResourceRepository<T, ID> {

	public T findOne(ID id) {
		throw new UnsupportedOperationException("findOne not implemented");
	}

	@Override
	public T findOne(ID id, QueryParams queryParams) {
		return findOne(id);
	}

	@Override
	public Iterable<T> findAll(QueryParams queryParams) {
		throw new UnsupportedOperationException("findAll not implemented");
	}

	@Override
	public Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
		throw new UnsupportedOperationException("findAll not supported");
	}

	@Override
	public <S extends T> S save(S entity) {
		throw new UnsupportedOperationException("save not supported on ");

	}

	@Override
	public void delete(ID id) {
		throw new UnsupportedOperationException("delete not supported on ");
	}

}