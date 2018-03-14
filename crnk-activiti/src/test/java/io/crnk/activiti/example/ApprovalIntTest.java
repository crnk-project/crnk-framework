package io.crnk.activiti.example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.crnk.activiti.example.model.ApproveForm;
import io.crnk.activiti.example.model.ApproveTask;
import io.crnk.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.HttpAdapter;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.repository.ScheduleRepository;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ApprovalIntTest extends JerseyTest {

	protected ScheduleRepository scheduleRepo;

	private ResourceRepositoryV2<ScheduleApprovalProcessInstance, String> approvalRepo;

	private ResourceRepositoryV2<ApproveTask, Serializable> taskRepo;

	private RelationshipRepositoryV2<Schedule, Serializable, ScheduleApprovalProcessInstance, Serializable> approvalRelRepo;

	private ResourceRepositoryV2<ApproveForm, Serializable> formRepo;

	protected CrnkClient client;

	private RelationshipRepositoryV2<ScheduleApprovalProcessInstance, Serializable, ApproveTask, Serializable> processTaskRepo;

	@Override
	protected ApprovalTestApplication configure() {
		return new ApprovalTestApplication();
	}

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		client = new CrnkClient(getBaseUri().toString());
		client.getObjectMapper().registerModule(new JavaTimeModule());

		HttpAdapter httpAdapter = client.getHttpAdapter();
		httpAdapter.setReceiveTimeout(10000, TimeUnit.SECONDS);

		scheduleRepo = client.getRepositoryForInterface(ScheduleRepository.class);
		approvalRepo = client.getRepositoryForType(ScheduleApprovalProcessInstance.class);
		processTaskRepo = client.getRepositoryForType(ScheduleApprovalProcessInstance.class, ApproveTask.class);
		taskRepo = client.getRepositoryForType(ApproveTask.class);
		approvalRelRepo = client.getRepositoryForType(Schedule.class, ScheduleApprovalProcessInstance.class);
		formRepo = client.getRepositoryForType(ApproveForm.class);
	}

	@Test
	public void checkApprovalWorkflow() {
		Schedule schedule = createSchedule();
		updateToTriggerApproval(schedule);
		ApproveTask task = verifyApprovalStarted(schedule);
		approve(task);
		verifyApprovalCompleted(schedule);
		verifyScheduleUpdated();
	}

	private void verifyScheduleUpdated() {
		ResourceList<Schedule> schedules = scheduleRepo.findAll(new QuerySpec(Schedule.class));
		Assert.assertEquals(1, schedules.size());
		Schedule currentSchedule = schedules.get(0);
		Assert.assertEquals("updatedName", currentSchedule.getName());
	}

	private void verifyApprovalCompleted(Schedule schedule) {
		QuerySpec taskQuery = new QuerySpec(ApproveTask.class);
		taskQuery.includeRelation(Arrays.asList("form"));

		// check approval completed
		ResourceList<ApproveTask> tasks = taskRepo.findAll(taskQuery);
		Assert.assertEquals(0, tasks.size());

		// check no approval for schedule in progress
		ScheduleApprovalProcessInstance scheduleApproval = approvalRelRepo
				.findOneTarget(schedule.getId().toString(), "approval", new QuerySpec(ScheduleApprovalProcessInstance.class));
		Assert.assertNull(scheduleApproval); // no approval in progress
	}

	private void approve(ApproveTask task) {
		QuerySpec taskQuery = new QuerySpec(ApproveTask.class);
		taskQuery.includeRelation(Arrays.asList("form"));

		ApproveForm form = task.getForm();
		form.setApproved(true);
		form = formRepo.create(form);
		Assert.assertTrue(form.isApproved());

		Assert.assertEquals("POSTing form must close the task", 0, taskRepo.findAll(taskQuery).size());
	}

	private ApproveTask verifyApprovalStarted(Schedule schedule) {
		// check approval process started
		ResourceList<ScheduleApprovalProcessInstance> scheduleApprovals =
				approvalRepo.findAll(new QuerySpec(ScheduleApprovalProcessInstance.class));
		Assert.assertEquals(1, scheduleApprovals.size());
		checkOpenApproval(schedule, scheduleApprovals.get(0));

		// check relationship from resource to approval process
		ScheduleApprovalProcessInstance scheduleApproval = approvalRelRepo
				.findOneTarget(schedule.getId().toString(), "approval", new QuerySpec(ScheduleApprovalProcessInstance.class));
		checkOpenApproval(schedule, scheduleApproval);

		// check relationship from approval process to task
		ApproveTask approveTask =
				processTaskRepo.findOneTarget(scheduleApprovals.get(0).getId(), "approveTask", new QuerySpec(ApproveTask.class));
		Assert.assertNotNull(approveTask);

		// check task created
		QuerySpec taskQuery = new QuerySpec(ApproveTask.class);
		taskQuery.includeRelation(Arrays.asList("form"));
		ResourceList<ApproveTask> tasks = taskRepo.findAll(taskQuery);
		Assert.assertEquals(1, tasks.size());
		ApproveTask task = tasks.get(0);
		checkApproveTaskCreated(schedule, task);

		// check search for task by process
		QuerySpec querySpec = new QuerySpec(ApproveTask.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("processInstance", "id"), FilterOperator.EQ, scheduleApproval.getId()));
		ResourceList<ApproveTask> results = taskRepo.findAll(querySpec);
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(scheduleApproval.getId(), results.get(0).getProcessInstanceId());

		return task;
	}

	private void updateToTriggerApproval(Schedule schedule) {
		schedule.setName("updatedName");
		Schedule currentPerson = scheduleRepo.save(schedule);
		Assert.assertEquals("someName", currentPerson.getName());
	}

	private Schedule createSchedule() {
		Schedule schedule = newSchedule("someName", 13L);
		schedule = scheduleRepo.create(schedule);
		ResourceList<Schedule> schedules = scheduleRepo.findAll(new QuerySpec(Schedule.class));
		Assert.assertEquals(1, schedules.size());
		return schedule;
	}

	private <T> T getUnique(ResourceList<T> list) {
		Assert.assertEquals(1, list.size());
		return list.get(0);
	}

	private void checkApproveTaskCreated(Schedule schedule, ApproveTask task) {
		Assert.assertEquals("approveScheduleTask", task.getTaskDefinitionKey());
		ApproveForm form = task.getForm();
		Assert.assertNotNull(form);
		Assert.assertFalse(form.isApproved());
	}

	private void checkOpenApproval(Schedule schedule, ScheduleApprovalProcessInstance scheduleApproval) {
		Assert.assertEquals("scheduleChange", scheduleApproval.getProcessDefinitionKey());
		Assert.assertEquals("schedules", scheduleApproval.getResourceType());
		Assert.assertEquals(schedule.getId().toString(), scheduleApproval.getResourceId());
		Assert.assertEquals("updatedName", scheduleApproval.getNewValues().getName());
	}

	private Schedule newSchedule(String name, long id) {
		Schedule schedule = new Schedule();
		schedule.setId(id);
		schedule.setName(name);
		return schedule;
	}

}
