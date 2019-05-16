package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassC;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;

public class ClassCRepository implements LegacyResourceRepository<ClassC, Long> {
	@Override
	public ClassC findOne(Long aLong, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassC> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassC> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends ClassC> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Long aLong) {

	}
}
