package io.crnk.test.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.legacy.repository.annotations.*;
import io.crnk.test.mock.models.TaskSubType;

import java.util.concurrent.ConcurrentHashMap;

@JsonApiResourceRepository(TaskSubType.class)
public class TaskSubtypeRepository {

	private static final ConcurrentHashMap<Long, TaskSubType> map = new ConcurrentHashMap<>();
	private TaskRepository repo = new TaskRepository();

	public static void clear() {
		map.clear();
	}

	@JsonApiSave
	public <S extends TaskSubType> S save(S entity) {
		return repo.save(entity);
	}

	@JsonApiFindOne
	public TaskSubType findOne(Long aLong, QuerySpec querySpec) {
		return (TaskSubType) repo.findOne(aLong, querySpec);
	}

	@JsonApiFindAll
	public Iterable<TaskSubType> findAll(QuerySpec queryParams) {
		throw new UnsupportedOperationException();
	}

	@JsonApiFindAllWithIds
	public Iterable<TaskSubType> findAll(Iterable<Long> ids, QuerySpec queryParams) {
		throw new UnsupportedOperationException();
	}

	@JsonApiDelete
	public void delete(Long aLong) {
		throw new UnsupportedOperationException();
	}
}
