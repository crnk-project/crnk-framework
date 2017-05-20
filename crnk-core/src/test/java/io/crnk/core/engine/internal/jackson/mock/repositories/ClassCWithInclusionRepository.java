package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassCWithInclusion;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;

public class ClassCWithInclusionRepository implements ResourceRepository<ClassCWithInclusion, Long> {
	@Override
	public ClassCWithInclusion findOne(Long aLong, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassCWithInclusion> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassCWithInclusion> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends ClassCWithInclusion> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Long aLong) {

	}
}
