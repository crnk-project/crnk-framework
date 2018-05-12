package io.crnk.internal.boot.cdi.model;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.legacy.repository.annotations.*;

import javax.enterprise.context.ApplicationScoped;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@JsonApiResourceRepository(Task.class)
@ApplicationScoped
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

		return entity;
	}

	@JsonApiFindOne
	public Task findOne(Long aLong, QuerySpec querySpec) {
		Task task = map.get(aLong);
		if (task == null) {
			throw new ResourceNotFoundException("");
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
