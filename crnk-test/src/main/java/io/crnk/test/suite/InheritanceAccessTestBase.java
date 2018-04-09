package io.crnk.test.suite;

import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.TaskSubType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public abstract class InheritanceAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepositoryV2<Task, Long> taskRepo;

	protected ResourceRepositoryV2<Project, Long> projectRepo;

	protected RelationshipRepositoryV2<Project, Long, Task, Long> relRepo;

	@Before
	public void setup() {
		testContainer.start();
		taskRepo = testContainer.getRepositoryForType(Task.class);
		projectRepo = testContainer.getRepositoryForType(Project.class);
		relRepo = testContainer.getRepositoryForType(Project.class, Task.class);

		Task baseTask = new Task();
		baseTask.setId(Long.valueOf(1));
		baseTask.setName("baseTask");
		taskRepo.create(baseTask);

		TaskSubType taskSubType = new TaskSubType();
		taskSubType.setId(Long.valueOf(2));
		taskSubType.setName("taskSubType");
		taskSubType.setSubTypeValue(13);
		taskRepo.create(taskSubType);

		Project project = new Project();
		project.setId(1L);
		project.setName("project0");
		project.setTasks(Arrays.asList(baseTask, taskSubType));
		projectRepo.create(project);

		relRepo.addRelations(project, Arrays.asList(baseTask.getId(), taskSubType.getId()), "tasks");
	}

	@After
	public void tearDown() {
		testContainer.stop();
	}


	@Test
	public void testFindAll() {
		QuerySpec querySpec = new QuerySpec(Task.class);
		querySpec.addSort(new SortSpec(Arrays.asList("name"), Direction.ASC));
		List<Task> tasks = taskRepo.findAll(querySpec);
		Assert.assertEquals(2, tasks.size());

		Assert.assertEquals("baseTask", tasks.get(0).getName());
		Assert.assertEquals("taskSubType", tasks.get(1).getName());
	}
}
