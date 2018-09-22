package io.crnk.activiti.internal.repository;


import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.queryspec.FilterSpec;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.task.TaskInfo;

import java.util.List;

public class HistoricTaskResourceRepository<T extends TaskResource> extends ActivitiRepositoryBase<T> {

	private final HistoryService historyService;

	public HistoricTaskResourceRepository(HistoryService historyService, ActivitiResourceMapper resourceMapper,
										  Class<T> resourceClass, List<FilterSpec> baseFilters) {
		super(resourceMapper, resourceClass, baseFilters);
		this.historyService = historyService;
	}

	@Override
	protected HistoricTaskInstanceQuery createQuery() {
		return historyService.createHistoricTaskInstanceQuery().includeTaskLocalVariables();
	}

	@Override
	protected T mapResult(Object result) {
		return resourceMapper.mapToResource(getResourceClass(), (TaskInfo) result);
	}

	@Override
	public <S extends T> S create(S resource) {
		throw new UnsupportedOperationException("history cannot be modified");
	}

	@Override
	public <S extends T> S save(S resource) {
		throw new UnsupportedOperationException("history cannot be modified");
	}

	@Override
	public void delete(String id) {
		throw new UnsupportedOperationException("history cannot be modified");
	}
}
