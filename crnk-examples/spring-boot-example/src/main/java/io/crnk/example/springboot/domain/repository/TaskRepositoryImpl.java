package io.crnk.example.springboot.domain.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.domain.model.Task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TaskRepositoryImpl implements TaskRepository {

	private static final Map<Long, Task> REPOSITORY = new ConcurrentHashMap<>();

	private static final AtomicLong ID_GENERATOR = new AtomicLong(4);

	private ProjectRepositoryImpl projectRepository;

	@Autowired
	public TaskRepositoryImpl(ProjectRepositoryImpl projectRepository) {
		this.projectRepository = projectRepository;
		Task task = new Task(1L, "Create tasks");
		task.setProjectId(123L);
		save(task);
		task = new Task(2L, "Make coffee");
		task.setProjectId(123L);
		save(task);
		task = new Task(3L, "Do things");
		task.setProjectId(123L);
		save(task);
	}

	@Override
	public <S extends Task> S save(S entity) {
		if (entity.getId() == null) {
			entity.setId(ID_GENERATOR.getAndIncrement());
		}
		REPOSITORY.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public <S extends Task> S create(S entity) {
		return save(entity);
	}

	@Override
	public Class<Task> getResourceClass() {
		return Task.class;
	}

	@Override
	public Task findOne(Long taskId, QuerySpec querySpec) {
		Task task = REPOSITORY.get(taskId);
		if (task == null) {
			throw new ResourceNotFoundException("Project not found!");
		}
		if (task.getProject() == null) {
			task.setProject(projectRepository.findOne(task.getProjectId(), new QuerySpec(Task.class)));
		}
		return task;
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(REPOSITORY.values());
	}

	@Override
	public ResourceList<Task> findAll(Iterable<Long> taskIds, QuerySpec querySpec) {
		List<Task> foundTasks = new ArrayList<>();
		for (Map.Entry<Long, Task> entry : REPOSITORY.entrySet()) {
			for (Long id : taskIds) {
				if (id.equals(entry.getKey())) {
					foundTasks.add(entry.getValue());
				}
			}
		}
		return querySpec.apply(foundTasks);
	}

	@Override
	public void delete(Long taskId) {
		REPOSITORY.remove(taskId);
	}
}
