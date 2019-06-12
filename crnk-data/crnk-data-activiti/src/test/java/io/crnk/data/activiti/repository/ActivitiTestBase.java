package io.crnk.data.activiti.repository;

import io.crnk.data.activiti.example.ApprovalTestApplication;
import io.crnk.data.activiti.example.model.ApproveTask;
import io.crnk.data.activiti.internal.repository.TaskResourceRepository;
import io.crnk.data.activiti.mapper.ActivitiResourceMapper;
import io.crnk.data.activiti.mapper.DefaultDateTimeMapper;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.module.Module;
import io.crnk.test.mock.TestModule;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Before;

import java.util.Date;
import java.util.List;

public abstract class ActivitiTestBase {

    protected ProcessEngine processEngine;

    protected TaskResourceRepository<ApproveTask> taskRepository;

    protected CrnkBoot boot;

    protected ActivitiResourceMapper resourceMapper = new ActivitiResourceMapper(new TypeParser(), new DefaultDateTimeMapper());

    @Before
    public void setup() {
        processEngine = ProcessEngines.getDefaultProcessEngine();

        processEngine.getRepositoryService().createDeployment()
                .addClasspathResource("approval.bpmn20.xml")
                .deploy();

        boot = new CrnkBoot();
        boot.addModule(new TestModule());
        boot.addModule(createActivitiModule());
        boot.boot();

        taskRepository = (TaskResourceRepository<ApproveTask>) boot.getResourceRegistry().getEntry(ApproveTask.class)
                .getResourceRepository().getResourceRepository();
    }

    protected Module createActivitiModule() {
        return ApprovalTestApplication.createActivitiModule(processEngine);
    }

    @After
    public void teardown() {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
        for (ProcessInstance processInstance : processInstances) {
            runtimeService.deleteProcessInstance(processInstance.getId(), "");
        }

        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().list();
        for (Task task : tasks) {
            taskService.deleteTask(task.getId());
        }
    }

    protected Task addTask(String name, int priority) {
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.newTask();
        task.setName(name);
        task.setPriority(priority);
        task.setAssignee("john");
        task.setCategory("testCategory");
        task.setDueDate(new Date());
        task.setOwner("jane");
        task.setDescription("testDescription");
        task.setTenantId("testTenant");
        taskService.saveTask(task);
        return task;

    }
}
