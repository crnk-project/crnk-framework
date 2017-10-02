package io.crnk.activiti;

import io.crnk.activiti.internal.repository.FormRelationshipRepository;
import io.crnk.activiti.internal.repository.FormResourceRepository;
import io.crnk.activiti.internal.repository.ProcessInstanceResourceRepository;
import io.crnk.activiti.internal.repository.TaskRelationshipRepository;
import io.crnk.activiti.internal.repository.TaskResourceRepository;
import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.resource.FormResource;
import io.crnk.core.module.Module;
import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

public class ActivitiModule implements Module {

	private ActivitiModuleConfig config;

	private ProcessEngine processEngine;

	private ActivitiResourceMapper resourceMapper;

	public static final ActivitiModule create(ProcessEngine processEngine, ActivitiModuleConfig config) {
		return new ActivitiModule(processEngine, config);
	}

	/**
	 * contructor for CDI
	 */
	protected ActivitiModule() {
	}

	private ActivitiModule(ProcessEngine processEngine, ActivitiModuleConfig config) {
		this.processEngine = processEngine;
		this.config = config;
	}


	@Override
	public String getModuleName() {
		return "activiti";
	}

	@Override
	public void setupModule(ModuleContext context) {

		TaskService taskService = processEngine.getTaskService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		FormService formService = processEngine.getFormService();

		resourceMapper = new ActivitiResourceMapper(context.getTypeParser(), config.getDateTimeMapper());


		for (ProcessInstanceConfig processInstanceConfig : config.getProcessInstances().values()) {
			context.addRepository(
					new ProcessInstanceResourceRepository(runtimeService, resourceMapper,
							processInstanceConfig.getProcessInstanceClass(), processInstanceConfig.getBaseFilters()));

			for (ProcessInstanceConfig.TaskRelationshipConfig taskRel : processInstanceConfig.getTaskRelationships().values()) {
				context.addRepository(new TaskRelationshipRepository(processInstanceConfig.getProcessInstanceClass(),
						taskRel.getTaskClass(), taskRel.getRelationshipName(), taskRel.getTaskDefinitionKey()));
			}
		}

		for (TaskRepositoryConfig taskConfig : config.getTasks().values()) {
			context.addRepository(new TaskResourceRepository(taskService, resourceMapper,
					taskConfig.getTaskClass(), taskConfig.getBaseFilters()));

			Class<? extends FormResource> formClass = taskConfig.getFormClass();
			if (formClass != null) {
				context.addRepository(new FormResourceRepository(formService, resourceMapper, formClass));
				context.addRepository(new FormRelationshipRepository(taskConfig.getTaskClass(), formClass));
			}
		}
	}

	public ActivitiResourceMapper getResourceMapper() {
		return resourceMapper;
	}
}
