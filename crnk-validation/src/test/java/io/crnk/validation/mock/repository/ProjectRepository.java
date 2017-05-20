package io.crnk.validation.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.ResourceRepository;
import io.crnk.validation.mock.models.Project;

import javax.validation.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectRepository implements ResourceRepository<Project, Long> {

	private static final ConcurrentHashMap<Long, Project> THREAD_LOCAL_REPOSITORY = new ConcurrentHashMap<>();

	@Override
	public <S extends Project> S save(S entity) {

		if (entity.getName() != null && entity.getName().equals(ValidationException.class.getSimpleName())) {
			throw new ValidationException("messageKey");
		}

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<S>> violations = validator.validate(entity);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		entity.setId((long) (THREAD_LOCAL_REPOSITORY.size() + 1));
		THREAD_LOCAL_REPOSITORY.put(entity.getId(), entity);

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
