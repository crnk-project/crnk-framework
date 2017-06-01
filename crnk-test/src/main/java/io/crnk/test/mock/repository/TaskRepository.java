package io.crnk.test.mock.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.legacy.repository.annotations.JsonApiDelete;
import io.crnk.legacy.repository.annotations.JsonApiFindAll;
import io.crnk.legacy.repository.annotations.JsonApiFindAllWithIds;
import io.crnk.legacy.repository.annotations.JsonApiFindOne;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;
import io.crnk.legacy.repository.annotations.JsonApiSave;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.UnknownException;
import io.crnk.test.mock.models.Task;

@JsonApiResourceRepository(Task.class)
public class TaskRepository {

	private static final ConcurrentHashMap<Long, Task> map = new ConcurrentHashMap<>();

	public static void clear() {
		map.clear();
	}

	@JsonApiSave
	public <S extends Task> S save(S entity) {

		if (entity.getId() == null) {
			entity.setId((long) (map.size() + 1));
		}
		map.put(entity.getId(), entity);

		if (entity.getId() == 10000) {
			throw new TestException("msg");
		}
		if (entity.getId() == 10001) {
			throw new UnknownException("msg");
		}

		return entity;
	}

	@JsonApiFindOne
	public Task findOne(Long aLong, QuerySpec querySpec) {
		if (aLong == 10000) {
			throw new TestException("msg");
		}
		if (aLong == 10001) {
			throw new UnknownException("msg");
		}

		Task task = map.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("failed to find resource with id " + aLong);
		}
		return task;
	}

	@JsonApiFindAll
	public Iterable<Task> findAll(QuerySpec queryParams) {
		return queryParams.apply(map.values());
	}

	@JsonApiFindAllWithIds
	public Iterable<Task> findAll(Iterable<Long> ids, QuerySpec queryParams) {
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

	@JsonApiDelete
	public void delete(Long aLong) {
		map.remove(aLong);
	}
}
