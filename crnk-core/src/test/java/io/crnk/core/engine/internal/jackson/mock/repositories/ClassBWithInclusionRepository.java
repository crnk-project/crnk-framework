package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassBWithInclusion;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;

public class ClassBWithInclusionRepository implements ResourceRepository<ClassBWithInclusion, Long> {
	@Override
	public ClassBWithInclusion findOne(Long aLong, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassBWithInclusion> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassBWithInclusion> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends ClassBWithInclusion> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Long aLong) {

	}
}
