package io.crnk.activiti.internal.repository;


import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyRelationshipRepositoryBase;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;
import java.util.Arrays;

public class TaskRelationshipRepository<P extends ProcessInstanceResource, T extends TaskResource> extends
		ReadOnlyRelationshipRepositoryBase<P,
				Serializable, T, String> implements ResourceRegistryAware {

	private static final String PROCESS_INSTANCE_ID_FIELD = "processInstanceId";

	private static final String TASK_DEFINITION_KEY_FIELD = "taskDefinitionKey";

	private final Class<P> processInstanceClass;

	private final Class<T> taskClass;

	private final String taskDefinitionId;

	private ResourceRegistry resourceRegistry;

	private String relationshipName;

	public TaskRelationshipRepository(Class<P> processInstanceClass, Class<T> taskClass, String relationshipName,
									  String taskDefinitionId) {
		this.processInstanceClass = processInstanceClass;
		this.taskClass = taskClass;
		this.relationshipName = relationshipName;
		this.taskDefinitionId = taskDefinitionId;
	}

	@Override
	public Class<P> getSourceResourceClass() {
		return processInstanceClass;
	}

	@Override
	public Class<T> getTargetResourceClass() {
		return taskClass;
	}

	@Override
	public T findOneTarget(Serializable sourceId, String fieldName, QuerySpec querySpec) {
		if (relationshipName.equals(fieldName)) {
			RegistryEntry taskEntry = resourceRegistry.getEntry(taskClass);
			ResourceRepository taskRepository =
					(ResourceRepository) taskEntry.getResourceRepository().getResourceRepository();

			QuerySpec processQuerySpec = querySpec.duplicate();

			processQuerySpec.addFilter(new FilterSpec(Arrays.asList(PROCESS_INSTANCE_ID_FIELD), FilterOperator.EQ, sourceId.toString()));
			processQuerySpec.addFilter(new FilterSpec(Arrays.asList(TASK_DEFINITION_KEY_FIELD), FilterOperator.EQ, taskDefinitionId));

			ResourceList tasks = taskRepository.findAll(processQuerySpec);
			PreconditionUtil.verify(tasks.size() <= 1, "unique result expected, got %s tasks for sourceId=%s, taskDefinitionId=%s", tasks.size(), sourceId, taskDefinitionId);
			return tasks.isEmpty() ? null : (T) tasks.get(0);
		} else {
			throw new UnsupportedOperationException("unknown fieldName '" + fieldName + "'");
		}
	}

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
	}


}
