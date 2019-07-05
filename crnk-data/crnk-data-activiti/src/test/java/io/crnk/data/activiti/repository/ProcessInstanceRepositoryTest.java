package io.crnk.data.activiti.repository;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;

import io.crnk.data.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.data.activiti.example.model.ScheduleApprovalValues;
import io.crnk.data.activiti.internal.repository.ProcessInstanceResourceRepository;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProcessInstanceRepositoryTest extends ActivitiTestBase {


	private static final String ENFORCED_DESCRIPTION = "testDescription";


	private ProcessInstanceResourceRepository<ScheduleApprovalProcessInstance> processRepository;

	private ProcessInstance processInstance;

	private RuntimeService runtimeService;

	@Before
	public void setup() {
		super.setup();

		processRepository =
				(ProcessInstanceResourceRepository<ScheduleApprovalProcessInstance>) boot.getResourceRegistry().getEntry
						(ScheduleApprovalProcessInstance.class).getResourceRepository().getImplementation();

		processInstance = addProcessInstance();
	}

	private ProcessInstance addProcessInstance() {
		ScheduleApprovalValues newValues = new ScheduleApprovalValues();
		newValues.setName("newName");

		ScheduleApprovalProcessInstance resource = new ScheduleApprovalProcessInstance();
		resource.setResourceType("schedules");
		resource.setResourceId("12");
		resource.setIntValue(13);
		resource.setStringValue("someValue");
		resource.setNewValues(newValues);
		resource.setStatus(ScheduleApprovalProcessInstance.ScheduleStatus.DONE);

		Map<String, Object> processVariables = resourceMapper.mapToVariables(resource);
		runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("scheduleChange",
				"testBusinessKey", processVariables);
		runtimeService.setProcessInstanceName(processInstance.getId(), "testName");

		processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstance.getId())
				.includeProcessVariables()
				.singleResult();

		return processInstance;
	}

	@Test
	public void checkResourceMapping() {
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);

		ScheduleApprovalProcessInstance resource = processRepository.findOne(processInstance.getId(), querySpec);
		assertEqualsNotNull(processInstance.getId(), resource.getId());
		assertEqualsNotNull(processInstance.getName(), resource.getName());
		assertEqualsNotNull(processInstance.isEnded(), resource.isEnded());
		assertEqualsNotNull(processInstance.isSuspended(), resource.isSuspended());
		assertEqualsNotNull(processInstance.getBusinessKey(), resource.getBusinessKey());
		assertEqualsNotNull(processInstance.getProcessDefinitionKey(), resource.getProcessDefinitionKey());

		Map<String, Object> variables = processInstance.getProcessVariables();
		assertEqualsNotNull(variables.get("newValues.name"), resource.getNewValues().getName());
		assertEqualsNotNull(variables.get("resourceId"), resource.getResourceId());
		assertEqualsNotNull(variables.get("resourceType"), resource.getResourceType());
	}

	private void assertEqualsNotNull(Object expected, Object actual) {
		Assert.assertEquals(expected, actual);
		Assert.assertNotNull(expected);
		Assert.assertNotNull(actual);
	}

	@Test
	public void postProcess() {
		postProcess("newSchedule", "newBusinessKey", null);
		postProcess("newSchedule", null, null);
		postProcess(null, null, null);
	}

	private ScheduleApprovalProcessInstance postProcess(String name, String businessKey, String tenantId) {
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);

		ScheduleApprovalValues newValues = new ScheduleApprovalValues();
		newValues.setName("newScheduleName");
		ScheduleApprovalProcessInstance resource = new ScheduleApprovalProcessInstance();
		resource.setName(name);
		resource.setResourceId("newScheduleId");
		resource.setResourceType("schedules");
		resource.setStringValue("someValue");
		resource.setTenantId(tenantId);
		resource.setBusinessKey(businessKey);
		resource.setProcessDefinitionKey("scheduleChange");
		resource.setNewValues(newValues);

		resource.setDescription(ENFORCED_DESCRIPTION);
		ScheduleApprovalProcessInstance createdProcessInstance = processRepository.create(resource);
		Assert.assertEquals(name, createdProcessInstance.getName());
		Assert.assertEquals("someValue", createdProcessInstance.getStringValue());
		Assert.assertNotNull(createdProcessInstance.getId());

		createdProcessInstance = processRepository.findOne(createdProcessInstance.getId(), querySpec);
		Assert.assertNotNull(createdProcessInstance.getId());
		Assert.assertEquals(name, createdProcessInstance.getName());
		Assert.assertEquals(businessKey, createdProcessInstance.getBusinessKey());
		Assert.assertEquals("newScheduleName", createdProcessInstance.getNewValues().getName());
		return createdProcessInstance;
	}

	@Test
	public void patchName() {
		ScheduleApprovalProcessInstance processInstance = postProcess(null, null, null);
		processInstance.setName("newName");
		ScheduleApprovalProcessInstance savedProcessInstance = processRepository.save(processInstance);
		Assert.assertEquals("newName", savedProcessInstance.getName());
		Assert.assertNotSame(processInstance, savedProcessInstance);
	}

	@Test
	public void patchBusinessKey() {
		ScheduleApprovalProcessInstance processInstance = postProcess(null, null, null);
		processInstance.setBusinessKey("newKey");
		ScheduleApprovalProcessInstance savedProcessInstance = processRepository.save(processInstance);
		Assert.assertEquals("newKey", savedProcessInstance.getBusinessKey());
		Assert.assertNotSame(processInstance, savedProcessInstance);
	}

	@Test
	public void patchSuspend() {
		ScheduleApprovalProcessInstance processInstance = postProcess(null, null, null);
		Assert.assertFalse(processInstance.isSuspended());
		processInstance.setSuspended(true);
		ScheduleApprovalProcessInstance savedProcessInstance = processRepository.save(processInstance);
		Assert.assertNotSame(processInstance, savedProcessInstance);
		Assert.assertTrue(savedProcessInstance.isSuspended());
		processInstance.setSuspended(false);
		savedProcessInstance = processRepository.save(processInstance);
		Assert.assertFalse(savedProcessInstance.isSuspended());
		Assert.assertNotSame(processInstance, savedProcessInstance);
	}

	@Test
	public void checkDelete() {
		ScheduleApprovalProcessInstance processInstance = postProcess(null, null, null);
		processRepository.delete(processInstance.getId());
		try {
			processRepository.findOne(processInstance.getId(), new QuerySpec(ScheduleApprovalProcessInstance.class));
			Assert.fail();
		}
		catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test(expected = BadRequestException.class)
	public void checkFilterEnforcementOnCreate() {
		ScheduleApprovalProcessInstance resource = new ScheduleApprovalProcessInstance();
		resource.setProcessDefinitionKey("invalidProcessDefinition");
		processRepository.create(resource);
	}


	@Test
	public void checkSetDefaultsOnCreate() {
		ScheduleApprovalProcessInstance resource = new ScheduleApprovalProcessInstance();
		resource.setProcessDefinitionKey(null); // will be set by default
		ScheduleApprovalProcessInstance savedResource = processRepository.create(resource);
		Assert.assertEquals("scheduleChange", savedResource.getProcessDefinitionKey());
	}


	@Test(expected = BadRequestException.class)
	public void checkFilterEnforcementOnPatch() {
		ScheduleApprovalProcessInstance processInstance = postProcess(null, null, null);

		ScheduleApprovalProcessInstance resource = new ScheduleApprovalProcessInstance();
		resource.setId(processInstance.getId());
		resource.setProcessDefinitionKey("invalidProcessDefinition");
		processRepository.save(resource);
	}


	@Test
	public void checkCustomVariable() {
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		ResourceList<ScheduleApprovalProcessInstance> test = processRepository.findAll(querySpec);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.EQ, "someValue"));
		Assert.assertEquals(1, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.LT, "someValue"));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("intValue"), FilterOperator.LT, 12));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("status"), FilterOperator.EQ, ScheduleApprovalProcessInstance.ScheduleStatus.DONE));
		Assert.assertEquals(1, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("status"), FilterOperator.EQ, ScheduleApprovalProcessInstance.ScheduleStatus.SHIPPED));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());


		// TODO GT/LT operators do not seem to work properly
		/*
		 querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("intValue"), FilterOperator.LT, 14));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.LE, "someValue"));
		Assert.assertEquals(1, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.GE, "someValue"));
		Assert.assertEquals(1, processRepository.findAll(querySpec).size());
		*/

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.EQ, "doesNotExist"));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("stringValue"), FilterOperator.NEQ, "doesNotExist"));
		Assert.assertNotEquals(0, processRepository.findAll(querySpec).size());
	}

	@Test
	public void checkEqualId() {
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, processInstance.getId()));
		Assert.assertEquals(1, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.EQ, "doesNotExists"));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());
	}

	@Test(expected = BadRequestException.class)
	public void checkNotEqualsNotSupported() {
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("id"), FilterOperator.NEQ, processInstance.getId()));
		processRepository.findAll(querySpec);
	}


	@Test
	public void checkLEStartTime() {
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("startTime"), FilterOperator.LT, OffsetDateTime.now().plusHours(1)));
		Assert.assertEquals(1, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("startTime"), FilterOperator.LT, OffsetDateTime.now().minusHours(1)));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());
	}


	@Test
	public void checkGTStartTime() {
		QuerySpec querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("startTime"), FilterOperator.GT, OffsetDateTime.now().minusHours(1)));
		Assert.assertEquals(1, processRepository.findAll(querySpec).size());

		querySpec = new QuerySpec(ScheduleApprovalProcessInstance.class);
		querySpec.addFilter(new FilterSpec(Arrays.asList("startTime"), FilterOperator.GT, OffsetDateTime.now().plusHours(1)));
		Assert.assertEquals(0, processRepository.findAll(querySpec).size());
	}
}
