package io.crnk.activiti.example;

import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.crnk.activiti.ActivitiModule;
import io.crnk.activiti.ActivitiModuleConfig;
import io.crnk.activiti.ProcessInstanceConfig;
import io.crnk.activiti.TaskRepositoryConfig;
import io.crnk.activiti.example.approval.ApprovalManager;
import io.crnk.activiti.example.approval.ApprovalManagerFactory;
import io.crnk.activiti.example.approval.ApprovalMapper;
import io.crnk.activiti.example.approval.ApprovalRelationshipRepository;
import io.crnk.activiti.example.approval.ApprovalRepositoryDecorator;
import io.crnk.activiti.example.model.ApproveForm;
import io.crnk.activiti.example.model.ApproveTask;
import io.crnk.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.mapper.DefaultDateTimeMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.models.Schedule;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
@Singleton
public class ApprovalTestApplication extends ResourceConfig {

	public ApprovalTestApplication() {
		property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, getClass().getPackage().getName());
		property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");

		CrnkFeature feature = new CrnkFeature();
		initObjectMapper(feature);

		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		TaskService taskService = processEngine.getTaskService();
		ModuleRegistry moduleRegistry = feature.getBoot().getModuleRegistry();

		processEngine.getRepositoryService().createDeployment()
				.addClasspathResource("approval.bpmn20.xml")
				.deploy();

		ActivitiResourceMapper resourceMapper =
				new ActivitiResourceMapper(moduleRegistry.getTypeParser(), new DefaultDateTimeMapper());
		ApprovalMapper mapper = new ApprovalMapper();
		ApprovalManager approvalManager = new ApprovalManagerFactory().getInstance();
		approvalManager.init(runtimeService, taskService, resourceMapper, mapper, moduleRegistry);
		feature.addModule(createApprovalModule(approvalManager));
		feature.addModule(createActivitiModule(processEngine));
		feature.addModule(new TestModule());

		registerInstances(feature);
	}

	private void initObjectMapper(CrnkFeature feature) {
		ObjectMapper objectMapper = feature.getObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.findAndRegisterModules();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	// tag::approvalModule[]
	public static SimpleModule createApprovalModule(ApprovalManager approvalManager) {
		FilterSpec approvalFilter = new FilterSpec(
				Arrays.asList("definitionKey"), FilterOperator.EQ, "scheduleChange"
		);
		List<FilterSpec> approvalFilters = Arrays.asList(approvalFilter);

		SimpleModule module = new SimpleModule("approval");
		module.addRepositoryDecoratorFactory(ApprovalRepositoryDecorator.createFactory(approvalManager));
		module.addRepository(new ApprovalRelationshipRepository(Schedule.class,
				ScheduleApprovalProcessInstance.class,"approval",
				"approval/schedule", approvalFilters)
		);
		return module;
	}
	// end::approvalModule[]

	// tag::activitiModule[]
	public static ActivitiModule createActivitiModule(ProcessEngine processEngine) {
		ActivitiModuleConfig config = new ActivitiModuleConfig();
		ProcessInstanceConfig processConfig = config.addProcessInstance(ScheduleApprovalProcessInstance.class);
		processConfig.filterByProcessDefinitionKey("scheduleChange");
		processConfig.addTaskRelationship(
				"approveTask", ApproveTask.class, "approveScheduleTask"
		);
		TaskRepositoryConfig taskConfig = config.addTask(ApproveTask.class);
		taskConfig.filterByTaskDefinitionKey("approveScheduleTask");
		taskConfig.setForm(ApproveForm.class);
		return ActivitiModule.create(processEngine, config);
	}
	// end::activitiModule[]
}