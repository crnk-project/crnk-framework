package io.crnk.data.activiti;

import io.crnk.data.activiti.internal.repository.FormRelationshipRepository;
import io.crnk.data.activiti.internal.repository.FormResourceRepository;
import io.crnk.data.activiti.internal.repository.HistoricProcessInstanceResourceRepository;
import io.crnk.data.activiti.internal.repository.HistoricTaskResourceRepository;
import io.crnk.data.activiti.internal.repository.ProcessInstanceResourceRepository;
import io.crnk.data.activiti.internal.repository.TaskRelationshipRepository;
import io.crnk.data.activiti.internal.repository.TaskResourceRepository;
import io.crnk.data.activiti.mapper.ActivitiResourceMapper;
import io.crnk.data.activiti.resource.FormResource;
import io.crnk.data.activiti.resource.HistoricProcessInstanceResource;
import io.crnk.data.activiti.resource.HistoricTaskResource;
import io.crnk.data.activiti.resource.ProcessInstanceResource;
import io.crnk.data.activiti.resource.TaskResource;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.module.Module;
import io.crnk.core.repository.ResourceRepository;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

public class ActivitiModule implements Module {

    private ActivitiModuleConfig config;

    private ProcessEngine processEngine;

    private ActivitiResourceMapper resourceMapper;

    private ModuleContext moduleContext;


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

    public <T extends TaskResource> ResourceRepository<T, String> getTaskRepository(Class<T> resourceClass) {
        return getRepository(resourceClass);
    }

    public <T extends HistoricTaskResource> ResourceRepository<T, String> getHistoricTaskRepository(Class<T> resourceClass) {
        return getRepository(resourceClass);
    }

    public <T extends FormResource> ResourceRepository<T, String> getFormRepository(Class<T> resourceClass) {
        return getRepository(resourceClass);
    }

    public <T extends ProcessInstanceResource> ResourceRepository<T, String> getProcessInstanceRepository(
            Class<T> resourceClass) {
        return getRepository(resourceClass);
    }

    public <T extends HistoricProcessInstanceResource> ResourceRepository<T, String> getHistoricProcessInstanceRepository(
            Class<T> resourceClass) {
        return getRepository(resourceClass);
    }


    private <T> ResourceRepository<T, String> getRepository(Class<T> resourceClass) {
        ResourceRegistry resourceRegistry = moduleContext.getResourceRegistry();
        RegistryEntry entry = resourceRegistry.getEntry(resourceClass);
        if (entry == null) {
            throw new RepositoryNotFoundException(resourceClass.getName() + " not registered");
        }
        return entry.getResourceRepositoryFacade();
    }


    @Override
    public String getModuleName() {
        return "activiti";
    }

    @Override
    public void setupModule(ModuleContext context) {
        this.moduleContext = context;

        TaskService taskService = processEngine.getTaskService();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        FormService formService = processEngine.getFormService();

        resourceMapper = new ActivitiResourceMapper(context.getTypeParser(), config.getDateTimeMapper());

        HistoryService historyService = processEngine.getHistoryService();
        for (ProcessInstanceConfig processInstanceConfig : config.getProcessInstances().values()) {
            context.addRepository(
                    new ProcessInstanceResourceRepository(runtimeService, historyService, resourceMapper,
                            processInstanceConfig.getProcessInstanceClass(), processInstanceConfig.getBaseFilters()));

            Class<? extends HistoricProcessInstanceResource> historyClass = processInstanceConfig.getHistoryClass();
            if (historyClass != null) {
                context.addRepository(
                        new HistoricProcessInstanceResourceRepository(historyService, resourceMapper, historyClass,
                                processInstanceConfig.getBaseFilters())
                );
            }

            for (ProcessInstanceConfig.TaskRelationshipConfig taskRel : processInstanceConfig.getTaskRelationships().values()) {
                context.addRepository(new TaskRelationshipRepository(processInstanceConfig.getProcessInstanceClass(),
                        taskRel.getTaskClass(), taskRel.getRelationshipName(), taskRel.getTaskDefinitionKey()));
            }
        }

        for (TaskRepositoryConfig taskConfig : config.getTasks().values()) {
            context.addRepository(new TaskResourceRepository(taskService, resourceMapper,
                    taskConfig.getTaskClass(), taskConfig.getBaseFilters()));

            Class<? extends TaskResource> historyClass = taskConfig.getHistoryClass();
            if (historyClass != null) {
                context.addRepository(
                        new HistoricTaskResourceRepository(historyService, resourceMapper, historyClass,
                                taskConfig.getBaseFilters())
                );
            }

            Class<? extends FormResource> formClass = taskConfig.getFormClass();
            if (formClass != null) {
                context.addRepository(new FormResourceRepository(formService, taskService, resourceMapper, formClass));
            }
        }

        context.addRepository(new FormRelationshipRepository());
    }

    public ActivitiResourceMapper getResourceMapper() {
        return resourceMapper;
    }
}
