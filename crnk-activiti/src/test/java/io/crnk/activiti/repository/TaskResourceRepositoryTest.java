package io.crnk.activiti.repository;

import com.google.common.collect.Sets;
import io.crnk.activiti.ActivitiModule;
import io.crnk.activiti.ActivitiModuleConfig;
import io.crnk.activiti.example.model.ApproveTask;
import io.crnk.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.activiti.internal.repository.ProcessInstanceResourceRepository;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.resource.list.ResourceList;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;

public class TaskResourceRepositoryTest extends ActivitiTestBase {


	private static final String ENFORCED_DESCRIPTION = "testDescription";

	private Task task;

	private Task isolatedTask;

	protected ProcessInstanceResourceRepository<ScheduleApprovalProcessInstance> processInstanceRepository;

	@Before
	public void setup() {
		super.setup();

		task = addTask("testTask", 12);

		isolatedTask = addTask("isolatedTask", 12);
		isolatedTask.setDescription("doesNotMatchRepositoryFilter");
		processEngine.getTaskService().saveTask(isolatedTask);

		processInstanceRepository = (ProcessInstanceResourceRepository<ScheduleApprovalProcessInstance>)
				boot.getResourceRegistry().getEntry(ScheduleApprovalProcessInstance.class)
						.getResourceRepository().getResourceRepository();
	}

	@Override
	protected Module createActivitiModule() {
		ActivitiModuleConfig config = new ActivitiModuleConfig();
		config.addProcessInstance(ScheduleApprovalProcessInstance.class);

		config.addTask(ApproveTask.class).filterBy("description", ENFORCED_DESCRIPTION);
		return ActivitiModule.create(processEngine, config);
	}

	@Test
	public void checkResourceMapping() {
		QuerySpec querySpec = new QuerySpec(TaskResource.class);

		ApproveTask resource = taskRepository.findOne(task.getId(), querySpec);
		Assert.assertEquals(task.getPriority(), resource.getPriority());
		Assert.assertEquals(task.getAssignee(), resource.getAssignee());
		Assert.assertEquals(task.getCategory(), resource.getCategory());
		Assert.assertEquals(task.getName(), resource.getName());
		Assert.assertEquals(task.getOwner(), resource.getOwner());
		Assert.assertEquals(task.getDescription(), resource.getDescription());
		Assert.assertEquals(task.getTenantId(), resource.getTenantId());
		Assert.assertFalse(resource.isCompleted());
		Assert.assertEquals(task.getDueDate().toInstant(), resource.getDueDate().toInstant());
	}

	@Test
	public void updateTask() {
		QuerySpec querySpec = new QuerySpec(TaskResource.class);

		OffsetDateTime updatedDueDate = OffsetDateTime.now().plusHours(12);
		ApproveTask resource = taskRepository.findOne(task.getId(), querySpec);
		resource.setName("updatedName");
		resource.setPriority(101);
		resource.setDueDate(updatedDueDate);
		ApproveTask updatedResource = taskRepository.save(resource);
		Assert.assertEquals("updatedName", updatedResource.getName());
		Assert.assertEquals(101, updatedResource.getPriority());
		Assert.assertEquals(updatedDueDate.toInstant(), updatedResource.getDueDate().toInstant());

		updatedResource = taskRepository.findOne(task.getId(), querySpec);
		Assert.assertEquals("updatedName", updatedResource.getName());
		Assert.assertEquals(101, updatedResource.getPriority());
		Assert.assertEquals(updatedDueDate.toInstant(), updatedResource.getDueDate().toInstant());
	}

	@Test
	public void createTask() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);

		ApproveTask resource = new ApproveTask();
		resource.setName("testTask");
		resource.setDescription(ENFORCED_DESCRIPTION);
		ApproveTask createdTask = taskRepository.create(resource);
		Assert.assertEquals("testTask", createdTask.getName());

		createdTask = taskRepository.findOne(task.getId(), querySpec);
		Assert.assertNotNull(createdTask);
	}

	@Test(expected = BadRequestException.class)
	public void checkFilterEnforcementOnCreate() {
		ApproveTask resource = new ApproveTask();
		resource.setName("testTask");
		resource.setDescription("invalid"); // must be set => due to chosen setup
		taskRepository.create(resource);
	}


	@Test(expected = BadRequestException.class)
	public void checkFilterEnforcementOnSave() {
		ApproveTask resource = new ApproveTask();
		resource.setId(isolatedTask.getId()); // => will not be able to hijack isolated task
		resource.setName("testTask");
		resource.setDescription(ENFORCED_DESCRIPTION);
		taskRepository.save(resource);
	}

	@Test
	public void checkDefaultsOnEnforcedAttributeOnCreate() {
		ApproveTask resource = new ApproveTask();
		resource.setName("newTask");
		resource.setDescription(null); // default will be set
		ApproveTask savedTask = taskRepository.create(resource);
		Assert.assertEquals(ENFORCED_DESCRIPTION, savedTask.getDescription());
	}

	@Test
	public void completeTask() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);

		ApproveTask resource = taskRepository.findOne(task.getId(), querySpec);
		Assert.assertFalse(resource.isCompleted());
		resource.setCompleted(true);
		ApproveTask updatedResource = taskRepository.save(resource);
		Assert.assertTrue(updatedResource.isCompleted());
		try {
			taskRepository.findOne(task.getId(), querySpec);
			Assert.fail();
		} catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test
	public void deleteTask() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);

		ApproveTask resource = taskRepository.findOne(task.getId(), querySpec);
		taskRepository.delete(resource.getId());
		try {
			taskRepository.findOne(task.getId(), querySpec);
			Assert.fail();
		} catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test
	public void checkEqualsName() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, task.getId()));
		Assert.assertEquals(1, taskRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(TaskResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, "doesNotExists"));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}

	@Test
	public void checkFindAllByIds() {
		ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("scheduleChange");
		String id = processInstance.getId();

		// note that this is not supported for Tasks, activiti lacks API to query multiple tasks
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		Assert.assertEquals(1, processInstanceRepository.findAll(Arrays.asList(id), querySpec).size());
		Assert.assertEquals(1, processInstanceRepository.findAll(Sets.newHashSet(id), querySpec).size());
	}

	@Test(expected = BadRequestException.class)
	public void checkNotEqualsNotSupported() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.NEQ, task.getId()));
		taskRepository.findAll(querySpec).size();
	}

	@Test
	public void checkEqualsAssignee() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, "john"));
		Assert.assertEquals(1, taskRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(TaskResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, "doesNotExists"));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}

	@Test
	public void checkLikeAssignee() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.LIKE, "%oh%"));
		Assert.assertEquals(1, taskRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(TaskResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.LIKE, "%doesNotExists%"));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}

	@Test
	public void checkEqualsAssigneeList() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, Arrays.asList("john", "jane")));
		Assert.assertEquals(1, taskRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(TaskResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, Arrays.asList("jane", "other")));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}

	@Test
	public void checkLEPriority() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("priority"), FilterOperator.LE, task.getPriority() + 1));
		Assert.assertEquals(1, taskRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(TaskResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("priority"), FilterOperator.LE, task.getPriority() - 1));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}

	@Test
	public void checkLEDueDate() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("dueDate"), FilterOperator.LT, OffsetDateTime.now().plusHours(1)));
		Assert.assertEquals(1, taskRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(TaskResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("dueDate"), FilterOperator.LT, OffsetDateTime.now().minusHours(1)));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}


	@Test
	public void checkGTDueDate() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("dueDate"), FilterOperator.GT, OffsetDateTime.now().minusHours(1)));
		Assert.assertEquals(1, taskRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(TaskResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("dueDate"), FilterOperator.GT, OffsetDateTime.now().plusHours(1)));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}

	@Test
	public void checkGTPriority() {
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("priority"), FilterOperator.GE, task.getPriority() - 1));
		Assert.assertEquals(1, taskRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(TaskResource.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("priority"), FilterOperator.GE, task.getPriority() + 1));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}

	@Test
	public void checkOrderByPriorityAsc() {
		addTask("otherTask", 14);

		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addSort(new SortSpec(Arrays.asList("priority"), Direction.ASC));
		ResourceList<ApproveTask> resources = taskRepository.findAll(querySpec);
		Assert.assertEquals(2, resources.size());
		Assert.assertEquals("testTask", resources.get(0).getName());
		Assert.assertEquals("otherTask", resources.get(1).getName());
	}

	@Test
	public void checkOrderByPriorityDesc() {
		addTask("otherTask", 14);

		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addSort(new SortSpec(Arrays.asList("priority"), Direction.DESC));
		ResourceList<ApproveTask> resources = taskRepository.findAll(querySpec);
		Assert.assertEquals(2, resources.size());
		Assert.assertEquals("otherTask", resources.get(0).getName());
		Assert.assertEquals("testTask", resources.get(1).getName());

	}


	@Test
	public void checkPaging() {
		addTask("otherTask1", 14);
		addTask("otherTask2", 15);
		addTask("otherTask3", 16);

		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addSort(new SortSpec(Arrays.asList("priority"), Direction.ASC));
		querySpec.setOffset(1);
		querySpec.setLimit(2L);
		ResourceList<ApproveTask> resources = taskRepository.findAll(querySpec);
		Assert.assertEquals(2, resources.size());
		Assert.assertEquals("otherTask1", resources.get(0).getName());
		Assert.assertEquals("otherTask2", resources.get(1).getName());
	}

	@Test
	public void checkIsolation() {
		addTask("otherTask1", 14);
		addTask("otherTask2", 15);
		addTask("otherTask3", 16);

		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addSort(new SortSpec(Arrays.asList("priority"), Direction.ASC));
		querySpec.setOffset(1);
		querySpec.setLimit(2L);
		ResourceList<ApproveTask> resources = taskRepository.findAll(querySpec);
		Assert.assertEquals(2, resources.size());
		Assert.assertEquals("otherTask1", resources.get(0).getName());
		Assert.assertEquals("otherTask2", resources.get(1).getName());
	}
}
