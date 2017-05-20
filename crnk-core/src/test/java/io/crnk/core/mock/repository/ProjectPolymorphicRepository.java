package io.crnk.core.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.ProjectPolymorphic;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectPolymorphicRepository implements ResourceRepository<ProjectPolymorphic, Long> {

	private static final ConcurrentHashMap<Long, ProjectPolymorphic> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	@Override
	public <S extends ProjectPolymorphic> S save(S entity) {
		entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public ProjectPolymorphic findOne(Long aLong, QueryParams queryParams) {
		ProjectPolymorphic project = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (project == null) {
			throw new ResourceNotFoundException(Project.class.getCanonicalName());
		}
		return project;
	}

	@Override
	public Iterable<ProjectPolymorphic> findAll(QueryParams queryParamss) {
		return THREAD_LOCAL_REPOSITORY.values();
	}


	@Override
	public Iterable<ProjectPolymorphic> findAll(Iterable<Long> ids, QueryParams queryParams) {
		List<ProjectPolymorphic> values = new LinkedList<>();
		for (ProjectPolymorphic value : THREAD_LOCAL_REPOSITORY.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(ProjectPolymorphic value, Iterable<Long> ids) {
		for (Long id : ids) {
			if (value.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void delete(Long aLong) {
		THREAD_LOCAL_REPOSITORY.remove(aLong);
	}
}
