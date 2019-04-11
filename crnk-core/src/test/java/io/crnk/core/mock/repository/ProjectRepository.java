package io.crnk.core.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectRepository implements LegacyResourceRepository<Project, Long> {

	private static final ConcurrentHashMap<Long, Project> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	public static final long RETURN_NULL_ON_CREATE_ID = 13412423;

	/**
	 * That particular ID will be mapped to a fancy project to simulated inheritance
	 */
	public static final long FANCY_PROJECT_ID = 101001;

	public static void clear() {
		THREAD_LOCAL_REPOSITORY.clear();
	}

	@Override
	public <S extends Project> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		}
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

		if (entity.getId() == RETURN_NULL_ON_CREATE_ID) {
			return null;
		}
		return entity;
	}

	@Override
	public Project findOne(Long aLong, QueryParams queryParams) {
		Project project = THREAD_LOCAL_REPOSITORY.get(aLong);
		if (project == null) {
			throw new ResourceNotFoundException(Project.class.getCanonicalName());
		}
		return project;
	}

	@Override
	public Iterable<Project> findAll(QueryParams queryParamss) {
		return THREAD_LOCAL_REPOSITORY.values();
	}


	@Override
	public Iterable<Project> findAll(Iterable<Long> ids, QueryParams queryParams) {
		List<Project> values = new LinkedList<>();
		for (Project value : THREAD_LOCAL_REPOSITORY.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(Project value, Iterable<Long> ids) {
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
