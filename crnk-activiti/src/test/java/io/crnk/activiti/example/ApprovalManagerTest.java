package io.crnk.activiti.example;

import java.util.HashMap;
import java.util.Map;

import io.crnk.activiti.example.approval.ApprovalManager;
import io.crnk.activiti.example.approval.ApprovalMapper;
import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.mapper.DefaultDateTimeMapper;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.test.mock.models.Schedule;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Execution;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ApprovalManagerTest {

	private Long mockId = 13L;

	private ApprovalManager manager;

	private RuntimeService runtimeService;

	private ResourceRepositoryAdapter repositoryAdapter;

	private RegistryEntry registryEntry;

	private Schedule originalResource;

	@Before
	public void setup() {
		runtimeService = Mockito.mock(RuntimeService.class);
		TaskService taskService = Mockito.mock(TaskService.class);
		repositoryAdapter = Mockito.mock(ResourceRepositoryAdapter.class);
		ApprovalMapper approvalMapper = new ApprovalMapper();
		ActivitiResourceMapper resourceMapper = new ActivitiResourceMapper(new TypeParser(), new DefaultDateTimeMapper());

		ResourceInformation information = Mockito.mock(ResourceInformation.class);
		registryEntry = Mockito.mock(RegistryEntry.class);
		ResourceRegistry resourceRegistry = Mockito.mock(ResourceRegistry.class);
		Mockito.when(registryEntry.getResourceInformation()).thenReturn(information);
		Mockito.when(registryEntry.getResourceRepository()).thenReturn(repositoryAdapter);
		Mockito.when(information.getResourceType()).thenReturn("schedule");
		Mockito.when(information.getId(Mockito.any())).thenReturn(mockId);
		Mockito.when(resourceRegistry.getEntry(Mockito.any(Class.class))).thenReturn(registryEntry);
		Mockito.when(resourceRegistry.getEntry(Mockito.any(String.class))).thenReturn(registryEntry);
		ModuleRegistry moduleRegistry = Mockito.mock(ModuleRegistry.class);
		Mockito.when(moduleRegistry.getResourceRegistry()).thenReturn(resourceRegistry);


		originalResource = new Schedule();
		originalResource.setId(mockId);
		originalResource.setName("Jane");
		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(originalResource);
		Mockito.when(repositoryAdapter.findOne(Mockito.any(Long.class), Mockito.any(QueryAdapter.class))).thenReturn(response);

		manager = new ApprovalManager();
		manager.init(runtimeService, taskService, resourceMapper, approvalMapper, moduleRegistry);
	}

	@Test
	public void checkRequestApproval() {
		Schedule changedEntity = new Schedule();
		changedEntity.setId(mockId);
		changedEntity.setName("John");

		Assert.assertFalse(manager.needsApproval(changedEntity, HttpMethod.POST));
		Assert.assertTrue(manager.needsApproval(changedEntity, HttpMethod.PATCH));
		manager.requestApproval(changedEntity, HttpMethod.PATCH);
		ArgumentCaptor<Map> processVariablesCaptor = ArgumentCaptor.forClass(Map.class);
		Mockito.verify(runtimeService, Mockito.times(1))
				.startProcessInstanceByKey(Mockito.eq("scheduleChange"), processVariablesCaptor.capture());
		Map processVariables = processVariablesCaptor.getValue();

		Assert.assertEquals(4, processVariables.size());
		Assert.assertEquals(mockId.toString(), processVariables.get("resourceId"));
		Assert.assertEquals("schedule", processVariables.get("resourceType"));
		Assert.assertEquals("John", processVariables.get("newValues.name"));
		Assert.assertEquals("Jane", processVariables.get("previousValues.name"));
	}

	@Test
	public void checkApprovedForwardsToRepository() {
		Map processVariable = new HashMap();
		processVariable.put("resourceId", mockId.toString());
		processVariable.put("resourceType", "schedule");
		processVariable.put("newValues.name", "John");
		processVariable.put("previousValues.name", "Jane");
		Mockito.when(runtimeService.getVariables(Mockito.anyString())).thenReturn(processVariable);

		Execution execution = Mockito.mock(Execution.class);
		manager.approved(execution);

		ArgumentCaptor<Object> savedEntityCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(repositoryAdapter, Mockito.times(1)).update(savedEntityCaptor.capture(), Mockito.any(QueryAdapter.class));

		// check value updated on original resource
		Schedule savedEntity = (Schedule) savedEntityCaptor.getValue();
		Assert.assertSame(originalResource, savedEntity);
		Assert.assertEquals("John", savedEntity.getName());
	}
}
