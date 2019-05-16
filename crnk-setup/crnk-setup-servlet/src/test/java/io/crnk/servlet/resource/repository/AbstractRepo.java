package io.crnk.servlet.resource.repository;


import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;
import io.crnk.servlet.resource.model.AbstractResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class AbstractRepo<T extends AbstractResource, ID extends Long> implements LegacyResourceRepository<T, ID> {
	@Override
	public T findOne(ID id, QueryParams queryParams) {
		return getRepo().get(id);
	}

	@Override
	public Iterable<T> findAll(QueryParams queryParams) {
		return getRepo().values();
	}

	@Override
	public Iterable<T> findAll(Iterable<ID> ids, QueryParams queryParams) {
		List<T> findAll = new ArrayList<>();
		for (ID id : ids) {
			findAll.add(getRepo().get(id));
		}
		return findAll;
	}

	@Override
	public <S extends T> S save(S entity) {
		getRepo().put(entity.getId(), entity);
		return entity;
	}

	@Override
	public void delete(ID id) {
		getRepo().remove(id);
	}

	public void clearRepo() {
		getRepo().clear();
	}

	abstract protected Map<Long, T> getRepo();
}
