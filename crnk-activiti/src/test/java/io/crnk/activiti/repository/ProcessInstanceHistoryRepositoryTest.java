package io.crnk.activiti.repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.crnk.activiti.ActivitiModule;
import io.crnk.activiti.example.ApprovalTestApplication;
import io.crnk.activiti.example.model.HistoricScheduleApprovalProcessInstance;
import io.crnk.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.activiti.example.model.ScheduleApprovalValues;
import io.crnk.activiti.internal.repository.ProcessInstanceResourceRepository;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcessInstanceHistoryRepositoryTest extends ActivitiTestBase {


	private ProcessInstance processInstance;

	private RuntimeService runtimeService;


	private ResourceRepositoryV2<HistoricScheduleApprovalProcessInstance, String> processHistoryRepository;

	private ResourceRepositoryV2<ScheduleApprovalProcessInstance, String> processRepository;

	@Before
	public void setup() {
		super.setup();

		processRepository =
				(ProcessInstanceResourceRepository<ScheduleApprovalProcessInstance>) boot.getResourceRegistry().getEntry
						(ScheduleApprovalProcessInstance.class).getResourceRepository().getResourceRepository();


		ActivitiModule activitiModule = boot.getModuleRegistry().getModule(ActivitiModule.class).get();
		processRepository = activitiModule.getProcessInstanceRepository(ScheduleApprovalProcessInstance.class);
		processHistoryRepository =
				activitiModule.getHistoricProcessInstanceRepository(HistoricScheduleApprovalProcessInstance.class);

		processInstance = addCompletedProcessInstance();
	}

	private ProcessInstance addCompletedProcessInstance() {
		ScheduleApprovalValues newValues = new ScheduleApprovalValues();
		newValues.setName("newName");

		ScheduleApprovalProcessInstance resource = new ScheduleApprovalProcessInstance();
		resource.setResourceId("12");
		resource.setResourceType("schedules");
		resource.setNewValues(newValues);

		Map<String, Object> processVariables = resourceMapper.mapToVariables(resource);
		runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("scheduleChange",
				"testBusinessKey", processVariables);
		runtimeService.setProcessInstanceName(processInstance.getId(), "testName");


		processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstance.getId())
				.includeProcessVariables()
				.singleResult();

		// complete process
		TaskService taskService = processEngine.getTaskService();
		Task task = taskService.createTaskQuery().list().get(0);
		Assert.assertEquals(task.getProcessInstanceId(), processInstance.getId());
		Map<String, Object> variables = new HashMap<>();
		variables.put("approved", false);
		taskService.complete(task.getId(), variables);
		return processInstance;
	}

	@Override
	protected Module createActivitiModule() {
		return ApprovalTestApplication.createActivitiModule(processEngine);
	}

	@Test
	public void checkCompletedTaskNotFoundByMainRepository() {
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, processInstance.getId()));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());
	}

	@Test
	public void checkEqualId() {
		QuerySpec querySpec = new QuerySpec(HistoricScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, processInstance.getId()));
		Assert.assertEquals(1, processHistoryRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(HistoricScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, "doesNotExists"));
		Assert.assertEquals(0, processHistoryRepository.findAll(querySpec).size());
	}

	@Test
	public void checkOrderByDuration() {
		QuerySpec querySpec = new QuerySpec(HistoricScheduleApprovalProcessInstance.class);
		querySpec.addSort(new SortSpec(Arrays.asList("duration"), Direction.DESC));
		Assert.assertNotEquals(0, processHistoryRepository.findAll(querySpec).size());

	}
}
