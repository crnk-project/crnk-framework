package io.crnk.core.engine.internal.jackson.mock.repositories;

import io.crnk.core.engine.internal.jackson.mock.models.ClassB;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;

public class ClassBRepository implements ResourceRepository<ClassB, Long> {
	@Override
	public ClassB findOne(Long aLong, QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassB> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ClassB> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends ClassB> S save(S entity) {
		return null;
	}

	@Override
	public void delete(Long aLong) {

	}
}
