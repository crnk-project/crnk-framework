package io.crnk.validation.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.LegacyResourceRepository;
import io.crnk.validation.mock.models.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TaskRepository implements LegacyResourceRepository<Task, Long> {

	public static final ConcurrentHashMap<Long, Task> map = new ConcurrentHashMap<>();

	@Override
	public <S extends Task> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId((long) (map.size() + 1));
		}
		map.put(entity.getId(), entity);

		return entity;
	}

	@Override
	public Task findOne(Long aLong, QueryParams queryParams) {
		Task task = map.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("");
		}
		return task;
	}

	@Override
	public Iterable<Task> findAll(QueryParams queryParams) {
		return map.values();
	}

	@Override
	public Iterable<Task> findAll(Iterable<Long> ids, QueryParams queryParams) {
		List<Task> values = new LinkedList<>();
		for (Task value : map.values()) {
			if (contains(value, ids)) {
				values.add(value);
			}
		}
		return values;
	}

	private boolean contains(Task value, Iterable<Long> ids) {
		for (Long id : ids) {
			if (value.getId().equals(id)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void delete(Long aLong) {
		map.remove(aLong);
	}
}
