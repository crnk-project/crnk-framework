package io.crnk.core.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.ComplexPojo;
import io.crnk.core.mock.models.ContainedPojo;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;

import java.util.concurrent.ConcurrentHashMap;

public class ComplexPojoRepository implements LegacyResourceRepository<ComplexPojo, Long> {

	private static final ConcurrentHashMap<Long, ComplexPojo> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	@Override
	public ComplexPojo findOne(Long aLong, QueryParams queryParams) {
		if (THREAD_LOCAL_REPOSITORY.size() < 1) {
			ComplexPojo complexPojo = new ComplexPojo();
			complexPojo.setContainedPojo(new ContainedPojo());
			complexPojo.getContainedPojo().setUpdateableProperty1("value from repository mock");
			complexPojo.getContainedPojo().setUpdateableProperty2("value from repository mock");
			complexPojo.setId(1l);
			THREAD_LOCAL_REPOSITORY.put(complexPojo.getId(), complexPojo);
		}
		ComplexPojo complexPojo = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (complexPojo == null) {
			throw new ResourceNotFoundException("");
		}
		return complexPojo;
	}

	@Override
	public Iterable<ComplexPojo> findAll(QueryParams queryParams) {
		return null;
	}

	@Override
	public Iterable<ComplexPojo> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public <S extends ComplexPojo> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		}
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public void delete(Long aLong) {

	}
}
