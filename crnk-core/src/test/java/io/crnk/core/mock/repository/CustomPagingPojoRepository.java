package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.CustomPagingPojo;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;

public class CustomPagingPojoRepository implements LegacyResourceRepository<CustomPagingPojo, Long> {

	private static CustomPagingPojo entity;

	@Override
	public CustomPagingPojo findOne(Long aLong, QueryParams queryParams) {
		return entity;
	}

	@Override
	public Iterable<CustomPagingPojo> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<CustomPagingPojo> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends CustomPagingPojo> S save(S entity) {
		CustomPagingPojoRepository.entity = entity;
		entity.setId(1L);
		return entity;
	}

	@Override
	public void delete(Long aLong) {

	}
}
