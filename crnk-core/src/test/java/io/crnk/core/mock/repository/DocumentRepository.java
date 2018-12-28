package io.crnk.core.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Document;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;

import java.util.concurrent.ConcurrentHashMap;

public class DocumentRepository implements LegacyResourceRepository<Document, Long> {

	private static final ConcurrentHashMap<Long, Document> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	@Override
	public <S extends Document> S save(S entity) {
		entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public Document findOne(Long aLong, QueryParams queryParams) {
		Document project = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (project == null) {
			throw new ResourceNotFoundException(Document.class.getCanonicalName());
		}
		return project;
	}

	@Override
	public Iterable<Document> findAll(QueryParams queryParams) {
		return THREAD_LOCAL_REPOSITORY.values();
	}

	@Override
	public Iterable<Document> findAll(Iterable<Long> longs, QueryParams queryParams) {
		return null;
	}

	@Override
	public void delete(Long aLong) {
		THREAD_LOCAL_REPOSITORY.remove(aLong);
	}
}
