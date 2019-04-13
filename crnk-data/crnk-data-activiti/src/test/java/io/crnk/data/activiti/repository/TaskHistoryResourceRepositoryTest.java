package io.crnk.data.activiti.repository;

import io.crnk.data.activiti.ActivitiModule;
import io.crnk.data.activiti.ActivitiModuleConfig;
import io.crnk.data.activiti.ProcessInstanceConfig;
import io.crnk.data.activiti.TaskRepositoryConfig;
import io.crnk.data.activiti.example.model.ApproveForm;
import io.crnk.data.activiti.example.model.ApproveTask;
import io.crnk.data.activiti.example.model.HistoricApproveTask;
import io.crnk.data.activiti.example.model.HistoricScheduleApprovalProcessInstance;
import io.crnk.data.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class TaskHistoryResourceRepositoryTest extends ActivitiTestBase {

    private static final String ENFORCED_DESCRIPTION = "testDescription";

    private Task task;

    private Task isolatedTask;

    private ResourceRepository<HistoricApproveTask, String> historicTaskRepository;

    private ResourceRepository<ScheduleApprovalProcessInstance, String> processInstanceRepository;

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
        historicTaskRepository = activitiModule.getHistoricTaskRepository(HistoricApproveTask.class);
        processInstanceRepository = activitiModule.getProcessInstanceRepository(ScheduleApprovalProcessInstance.class);
    }

    @Override
    protected Module createActivitiModule() {
        ActivitiModuleConfig config = new ActivitiModuleConfig();
        ProcessInstanceConfig processConfig = config.addProcessInstance(ScheduleApprovalProcessInstance.class);
        processConfig.historic(HistoricScheduleApprovalProcessInstance.class);
        processConfig.historic(HistoricScheduleApprovalProcessInstance.class);
        processConfig.filterByProcessDefinitionKey("scheduleChange");
        processConfig.addTaskRelationship(
                "approveTask", ApproveTask.class, "approveScheduleTask"
        );

        TaskRepositoryConfig taskConfig = config.addTask(ApproveTask.class);
        taskConfig.historic(HistoricApproveTask.class);
        taskConfig.filterBy("description", ENFORCED_DESCRIPTION);
        taskConfig.historic(HistoricApproveTask.class);
        taskConfig.setForm(ApproveForm.class);

        return ActivitiModule.create(processEngine, config);
    }

    @Test
    public void checkCompletedTaskNotFoundByMainRepository() {
        QuerySpec querySpec = new QuerySpec(HistoricApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, task.getId()));
        Assert.assertEquals(0, taskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkEqualsName() {
        QuerySpec querySpec = new QuerySpec(HistoricApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, task.getId()));
        Assert.assertEquals(1, historicTaskRepository.findAll(querySpec).size());
    }

    @Test
    public void checkEqualsAssignee() {
        QuerySpec querySpec = new QuerySpec(HistoricApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, task.getId()));
        querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, "john"));
        Assert.assertEquals(1, historicTaskRepository.findAll(querySpec).size());

        querySpec = new QuerySpec(HistoricApproveTask.class);
        querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, task.getId()));
        querySpec.addFilter(new FilterSpec(Arrays.asList("assignee"), FilterOperator.EQ, "doesNotExists"));
        Assert.assertEquals(0, historicTaskRepository.findAll(querySpec).size());
    }
}
