package io.crnk.activiti.repository;

import java.util.Arrays;

import io.crnk.activiti.ActivitiModule;
import io.crnk.activiti.ActivitiModuleConfig;
import io.crnk.activiti.example.model.ApproveTask;
import io.crnk.activiti.example.model.HistorizedApproveTask;
import io.crnk.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TaskHistoryResourceRepositoryTest extends ActivitiTestBase {

	private static final String ENFORCED_DESCRIPTION = "testDescription";

	private Task task;

	private Task isolatedTask;

	private ResourceRepositoryV2<HistorizedApproveTask, String> taskHistoryRepository;

	private ResourceRepositoryV2<ScheduleApprovalProcessInstance, String> processInstanceRepository;

	@Before
	public void setup() {
		super.setup();

		task = addTask("testTask", 12);

		TaskService taskService = processEngine.getTaskService();
		taskService.complete(task.getId());

		isolatedTask = addTask("isolatedTask", 12);
		isolatedTask.setDescription("doesNotMatchRepositoryFilter");
		taskService.saveTask(isolatedTask);

		ActivitiModule activitiModule = boot.getModuleRegistry().getModule(ActivitiModule.class).get();
		taskHistoryRepository = activitiModule.getTaskRepository(HistorizedApproveTask.class);
		processInstanceRepository = activitiModule.getProcessInstanceRepository(ScheduleApprovalProcessInstance.class);
	}

	@Override
	protected Module createActivitiModule() {
		ActivitiModuleConfig config = new ActivitiModuleConfig();
		config.addProcessInstance(ScheduleApprovalProcessInstance.class);

		config.addTask(ApproveTask.class).historized(HistorizedApproveTask.class).filterBy("description", ENFORCED_DESCRIPTION);
		return ActivitiModule.create(processEngine, config);
	}

	@Test
	public void checkCompletedTaskNotFoundByMainRepository() {
		QuerySpec querySpec = new QuerySpec(HistorizedApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, task.getId()));
		Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
	}

	@Test
	public void checkEqualsName() {
		QuerySpec querySpec = new QuerySpec(HistorizedApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, task.getId()));
		Assert.assertEquals(1, taskHistoryRepository.findAll(querySpec).size());
	}

	@Test
	public void checkEqualsAssignee() {
		QuerySpec querySpec = new QuerySpec(HistorizedApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, "john"));
		Assert.assertEquals(1, taskHistoryRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(HistorizedApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, "doesNotExists"));
		Assert.assertEquals(0, taskHistoryRepository.findAll(querySpec).size());
	}
}
