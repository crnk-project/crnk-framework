package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Pojo;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;

public class PojoRepository implements ResourceRepository<Pojo, Long> {

	private static Pojo entity;

	@Override
	public Pojo findOne(Long aLong, QueryParams queryParams) {
		return entity;
	}

	@Override
	public Iterable<Pojo> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<Pojo> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends Pojo> S save(S entity) {
		PojoRepository.entity = entity;
		entity.setId(1L);
		return entity;
	}

	@Override
	public void delete(Long aLong) {

	}
}
