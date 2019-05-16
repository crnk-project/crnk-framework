package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassA;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;

public class ClassARepository implements LegacyResourceRepository<ClassA, Long> {
	@Override
	public ClassA findOne(Long aLong, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassA> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassA> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends ClassA> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Long aLong) {

	}
}
