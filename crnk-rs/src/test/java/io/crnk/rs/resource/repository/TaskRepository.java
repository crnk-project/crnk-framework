package io.crnk.rs.resource.repository;

import io.crnk.legacy.repository.annotations.*;
import io.crnk.rs.resource.exception.ExampleException;
import io.crnk.rs.resource.model.Task;

import javax.ws.rs.HeaderParam;
import java.util.Collections;

@JsonApiResourceRepository(Task.class)
public class TaskRepository {

	@JsonApiSave
	public <S extends Task> S save(S entity) {
		return null;
	}

	@JsonApiFindOne
	public Task findOne(Long aLong, @HeaderParam("x-test") String header) {
		// Simulates error and throws an Exception to test exception handling.
		if (aLong == 5) {
			throw new ExampleException(ExampleException.ERROR_ID, ExampleException.ERROR_TITLE);
		}
		Task task = new Task(aLong, header);
		return task;
	}

	@JsonApiFindAll
	public Iterable<Task> findAll() {
		return findAll(null);
	}

	@JsonApiFindAllWithIds
	public Iterable<Task> findAll(Iterable<Long> ids) {
		return Collections.singletonList(new Task(1L, "First task"));
	}

	@JsonApiDelete
	public void delete(Long aLong) {

	}
}
